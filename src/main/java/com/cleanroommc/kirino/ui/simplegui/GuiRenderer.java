package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GuiRenderer {

    private static final float MEM_ALLOC_FACTOR = 1.5f;

    private static final int DRAW_INFO_STRIDE = 16;
    private static final int IDB_STRIDE = 16;
    private static final int RECT_PAYLOAD_STRIDE = 64;
    private static final int LINES_PAYLOAD_STRIDE = 1;
    private static final int BEZIER_PAYLOAD_STRIDE = 1;

    private final ShaderProgram program;
    private final VAO dummyVao;

    // stride=8
    // vec2 pos
    private final TransientArena arena;
    private final SSBOView arenaSsbo;

    // stride=16
    // int drawType (implies mode)
    // int flags
    // int payloadIndex
    // float depth: [0, 1]
    private ByteBuffer drawInfoWorkspace = null;
    private final SSBOView drawInfo;

    // stride=64
    // vec4 rect
    // vec2 shadow
    // int meshOffset (for rounded rect; refers to transient arena)
    // int vertexCount (for rounded rect; refers to transient arena)
    // int color
    // float borderWidth
    // int borderColor
    // float shadowBlur
    // int shadowColor
    // float radius
    private ByteBuffer rectPayloadWorkspace = null;
    private final SSBOView rectPayload;

    private ByteBuffer linesPayloadWorkspace = null;
    private final SSBOView linesPayload;

    private ByteBuffer bezierPayloadWorkspace = null;
    private final SSBOView bezierPayload;

    // stride=16
    // int count: actual vert count (= TRIANGLES mode vert count. != vert count from the input stream.
    //     every type uses diff mode. normalize in vert shader)
    // int instanceCount = 1
    // int first = 0
    // int baseInstance: drawInfo SSBO index
    private ByteBuffer idbWorkspace = null;
    private final IDBView idb;

    GuiRenderer(ImmediateShaderAccess shaderAccess, VAO dummyVao, TransientArena arena) {
        this.dummyVao = dummyVao;
        this.arena = arena;

        arenaSsbo = new SSBOView(new GLBuffer());
        drawInfo = new SSBOView(new GLBuffer());
        rectPayload = new SSBOView(new GLBuffer());
        linesPayload = new SSBOView(new GLBuffer());
        bezierPayload = new SSBOView(new GLBuffer());
        idb = new IDBView(new GLBuffer());

        ShutdownManager.registerAsync(() -> {
            MemoryUtil.memFree(drawInfoWorkspace);
            MemoryUtil.memFree(rectPayloadWorkspace);
            MemoryUtil.memFree(linesPayloadWorkspace);
            MemoryUtil.memFree(bezierPayloadWorkspace);
            MemoryUtil.memFree(idbWorkspace);
        });

        Shader vert = shaderAccess.makeShader(new ResourceLocation("forge:shaders/simplegui_renderer.vert"));
        Shader frag = shaderAccess.makeShader(new ResourceLocation("forge:shaders/simplegui_renderer.frag"));
        shaderAccess.submitToGL(vert, frag);
        program = shaderAccess.makeProgram(vert, frag);
    }

    /**
     * Arbitrary alignment implementation (not necessarily power-of-two).
     *
     * <p>Note: It assumes no arithmetic overflow and doesn't handle such situation.</p>
     */
    private static int alignUp(int pos, int align) {
        if (align <= 1) {
            return pos;
        }
        if (Integer.bitCount(align) == 1) {
            return (pos + align - 1) & ~(align - 1);
        } else {
            int r = pos % align;
            return r == 0 ? pos : pos + align - r;
        }
    }

    private static final int[] OUT = {0, 0, 0};

    private static int calcMaxCommandBatch(ByteBuffer buffer, int[] out) {
        Preconditions.checkArgument(out.length == 3);

        int pos = 0;
        int end = buffer.limit();

        int max0 = 0, max1 = 0, max2 = 0;
        int count0 = 0, count1 = 0, count2 = 0;

        int maxCount = 0;
        int count = 0;

        while (pos < end) {
            int op = buffer.getInt(pos + SG_CmdHeader.OP);
            int size = buffer.getInt(pos + SG_CmdHeader.SIZE);

            if (op == SG_GuiOp.DRAW_RECT || op == SG_GuiOp.DRAW_LINES || op == SG_GuiOp.DRAW_BEZIER) {
                count++;

                if (op == SG_GuiOp.DRAW_RECT) {
                    count0++;
                } else if (op == SG_GuiOp.DRAW_LINES) {
                    count1++;
                } else {
                    count2++;
                }
            } else if (op == SG_GuiOp.PUSH_CLIP || op == SG_GuiOp.POP_CLIP) {
                if (count > 0) {
                    count = 0;
                }
                if (count0 > 0) {
                    count0 = 0;
                }
                if (count1 > 0) {
                    count1 = 0;
                }
                if (count2 > 0) {
                    count2 = 0;
                }
            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            max0 = Math.max(max0, count0);
            max1 = Math.max(max1, count1);
            max2 = Math.max(max2, count2);

            maxCount = Math.max(maxCount, count);
            pos += size;
        }

        out[0] = max0;
        out[1] = max1;
        out[2] = max2;

        return maxCount;
    }

    /**
     * It resets all buffer workspaces: {@link #drawInfoWorkspace}, {@link #rectPayloadWorkspace},
     * {@link #linesPayloadWorkspace}, {@link #bezierPayloadWorkspace}, {@link #idbWorkspace}.
     * Calling {@link ByteBuffer#clear()} to be exact.
     */
    private void resetBufferWorkspaces() {
        if (drawInfoWorkspace != null) {
            drawInfoWorkspace.clear();
        }
        if (rectPayloadWorkspace != null) {
            rectPayloadWorkspace.clear();
        }
        if (linesPayloadWorkspace != null) {
            linesPayloadWorkspace.clear();
        }
        if (bezierPayloadWorkspace != null) {
            bezierPayloadWorkspace.clear();
        }
        if (idbWorkspace != null) {
            idbWorkspace.clear();
        }
    }

    /**
     * It initializes and resets all buffer workspaces: {@link #drawInfoWorkspace}, {@link #rectPayloadWorkspace},
     * {@link #linesPayloadWorkspace}, {@link #bezierPayloadWorkspace}, {@link #idbWorkspace}
     *
     * <p>Note: Some buffer may still remain <code>null</code> and unallocated after this call.</p>
     */
    private void initBufferWorkspaces(int commandCount, int rectCount, int linesCount, int bezierCount) {
        int drawInfoSize = (int) (commandCount * DRAW_INFO_STRIDE * MEM_ALLOC_FACTOR);
        if (drawInfoSize > 0) {
            if (drawInfoWorkspace == null) {
                drawInfoWorkspace = MemoryUtil.memAlloc(drawInfoSize);
            } else if (drawInfoWorkspace.capacity() < drawInfoSize) {
                MemoryUtil.memFree(drawInfoWorkspace);
                drawInfoWorkspace = MemoryUtil.memAlloc(drawInfoSize);
            }
            drawInfoWorkspace.clear();
        }

        int rectPayloadSize = (int) (rectCount * RECT_PAYLOAD_STRIDE * MEM_ALLOC_FACTOR);
        if (rectPayloadSize > 0) {
            if (rectPayloadWorkspace == null) {
                rectPayloadWorkspace = MemoryUtil.memAlloc(rectPayloadSize);
            } else if (rectPayloadWorkspace.capacity() < rectPayloadSize) {
                MemoryUtil.memFree(rectPayloadWorkspace);
                rectPayloadWorkspace = MemoryUtil.memAlloc(rectPayloadSize);
            }
            rectPayloadWorkspace.clear();
        }

        int linesPayloadSize = (int) (linesCount * LINES_PAYLOAD_STRIDE * MEM_ALLOC_FACTOR);
        if (linesPayloadSize > 0) {
            if (linesPayloadWorkspace == null) {
                linesPayloadWorkspace = MemoryUtil.memAlloc(linesPayloadSize);
            } else if (linesPayloadWorkspace.capacity() < linesPayloadSize) {
                MemoryUtil.memFree(linesPayloadWorkspace);
                linesPayloadWorkspace = MemoryUtil.memAlloc(linesPayloadSize);
            }
            linesPayloadWorkspace.clear();
        }

        int bezierPayloadSize = (int) (bezierCount * BEZIER_PAYLOAD_STRIDE * MEM_ALLOC_FACTOR);
        if (bezierPayloadSize > 0) {
            if (bezierPayloadWorkspace == null) {
                bezierPayloadWorkspace = MemoryUtil.memAlloc(bezierPayloadSize);
            } else if (bezierPayloadWorkspace.capacity() < bezierPayloadSize) {
                MemoryUtil.memFree(bezierPayloadWorkspace);
                bezierPayloadWorkspace = MemoryUtil.memAlloc(bezierPayloadSize);
            }
            bezierPayloadWorkspace.clear();
        }

        int idbSize = (int) (commandCount * IDB_STRIDE * MEM_ALLOC_FACTOR);
        if (idbSize > 0) {
            if (idbWorkspace == null) {
                idbWorkspace = MemoryUtil.memAlloc(idbSize);
            } else if (idbWorkspace.capacity() < idbSize) {
                MemoryUtil.memFree(idbWorkspace);
                idbWorkspace = MemoryUtil.memAlloc(idbSize);
            }
            idbWorkspace.clear();
        }
    }

    private void writeIdb(ByteBuffer buffer, int op, int pos, int flags, int used, int drawInfoPos) {
        // int count
        if (op == SG_GuiOp.DRAW_RECT) {
            boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;
            if (needsMesh) {
                int vertexCountPos = pos + used + SG_CmdHeader.TAIL_SIZE + 4;
                // the first vertex is the center
                // minus one to get the actual vertex count
                int vertexCount = buffer.getInt(vertexCountPos) - 1; // fan vert count

                boolean hasBorder = (flags & SG_GuiOp.FLAG_BORDER) != 0;
                boolean hasShadow = (flags & SG_GuiOp.FLAG_SHADOW) != 0;

                if (!hasBorder && !hasShadow) {
                    vertexCount *= 3; // inner fan
                } else if (hasBorder && !hasShadow) {
                    vertexCount *= 9; // inner fan + outer ring
                } else if (!hasBorder && hasShadow) {
                    vertexCount *= 9; // inner fan + outer ring
                } else if (hasBorder && hasShadow) {
                    vertexCount *= 15; // todo: inner fan + first outer ring + sec outer ring
                }

                idbWorkspace.putInt(vertexCount);
            } else {
                idbWorkspace.putInt(6);
            }
        } else if (op == SG_GuiOp.DRAW_LINES) {
            idbWorkspace.putInt(0); // todo
        } else if (op == SG_GuiOp.DRAW_BEZIER) {
            idbWorkspace.putInt(0); // todo
        } else {
            throw new IllegalStateException("Invalid SG_GuiOp: " + op);
        }

        // int instanceCount
        idbWorkspace.putInt(1);

        // int first
        idbWorkspace.putInt(0);

        // int baseInstance
        idbWorkspace.putInt(drawInfoPos / DRAW_INFO_STRIDE);
    }

    private int writeDrawInfo(ByteBuffer buffer, int op, int pos, int flags, int used, int payloadPos) {
        int drawInfoPos = drawInfoWorkspace.position();

        // int drawType
        drawInfoWorkspace.putInt(op);

        // int flags
        drawInfoWorkspace.putInt(flags);

        // int payloadIndex
        if (op == SG_GuiOp.DRAW_RECT) {
            drawInfoWorkspace.putInt(payloadPos / RECT_PAYLOAD_STRIDE);
        } else if (op == SG_GuiOp.DRAW_LINES) {
            drawInfoWorkspace.putInt(payloadPos / LINES_PAYLOAD_STRIDE);
        } else if (op == SG_GuiOp.DRAW_BEZIER) {
            drawInfoWorkspace.putInt(payloadPos / BEZIER_PAYLOAD_STRIDE);
        } else {
            throw new IllegalStateException("Invalid SG_GuiOp: " + op);
        }

        // float depth
        int depthPos = pos + used + 4;
        float depth = buffer.getFloat(depthPos);
        drawInfoWorkspace.putFloat(depth);

        return drawInfoPos;
    }

    private int writeRectPayload(ByteBuffer buffer, int pos, int flags, int used) {
        int posPointer = pos + SG_CmdHeader.HEADER_SIZE;

        float x = buffer.getFloat(posPointer); posPointer += 4;
        float y = buffer.getFloat(posPointer); posPointer += 4;
        float width = buffer.getFloat(posPointer); posPointer += 4;
        float height = buffer.getFloat(posPointer); posPointer += 4;
        int color = buffer.getInt(posPointer); posPointer += 4;

        boolean hasRadius = (flags & SG_GuiOp.FLAG_RADIUS) != 0;
        float radius = 0f;
        if (hasRadius) {
            radius = buffer.getFloat(posPointer);
            posPointer += 8;
        }

        boolean hasBorder = (flags & SG_GuiOp.FLAG_BORDER) != 0;
        float borderWidth = 0f;
        int borderColor = 0;
        if (hasBorder) {
            borderWidth = buffer.getFloat(posPointer);
            posPointer += 4;
            borderColor = buffer.getInt(posPointer);
            posPointer += 4;
        }

        boolean hasShadow = (flags & SG_GuiOp.FLAG_SHADOW) != 0;
        float shadowBlur = 0f;
        float shadowX = 0f;
        float shadowY = 0f;
        int shadowColor = 0;
        if (hasShadow) {
            shadowBlur = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowX = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowY = buffer.getFloat(posPointer);
            posPointer += 4;
            shadowColor = buffer.getInt(posPointer);
            posPointer += 4;
        }

        int meshOffset = 0;
        int vertexCount = 0;
        // needs mesh
        if (hasRadius) {
            int meshOffsetPos = pos + used + SG_CmdHeader.TAIL_SIZE;
            int vertexCountPos = pos + used + SG_CmdHeader.TAIL_SIZE + 4;
            meshOffset = buffer.getInt(meshOffsetPos);
            vertexCount = buffer.getInt(vertexCountPos);
        }

        int payloadPos = rectPayloadWorkspace.position();

        // vec4 rect
        rectPayloadWorkspace.putFloat(x);
        rectPayloadWorkspace.putFloat(y);
        rectPayloadWorkspace.putFloat(width);
        rectPayloadWorkspace.putFloat(height);

        // vec2 shadow
        rectPayloadWorkspace.putFloat(shadowX);
        rectPayloadWorkspace.putFloat(shadowY);

        // int meshOffset
        rectPayloadWorkspace.putInt(meshOffset);

        // int vertexCount
        rectPayloadWorkspace.putInt(vertexCount);

        // int color
        rectPayloadWorkspace.putInt(color);

        // float borderWidth
        rectPayloadWorkspace.putFloat(borderWidth);

        // int borderColor
        rectPayloadWorkspace.putInt(borderColor);

        // float shadowBlur
        rectPayloadWorkspace.putFloat(shadowBlur);

        // int shadowColor
        rectPayloadWorkspace.putInt(shadowColor);

        // float radius
        rectPayloadWorkspace.putFloat(radius);

        int next = alignUp(rectPayloadWorkspace.position(), RECT_PAYLOAD_STRIDE);
        while (rectPayloadWorkspace.position() < next) {
            rectPayloadWorkspace.put((byte) 0);
        }

        return payloadPos;
    }

    private int writeLinesPayload(ByteBuffer buffer, int pos, int flags) {
        return 0;
    }

    private int writeBezierPayload(ByteBuffer buffer, int pos, int flags) {
        return 0;
    }

    public void render(@NonNull GuiCommandStream stream) {
        Preconditions.checkNotNull(stream);

        int commandCount = calcMaxCommandBatch(stream.view(), OUT);
        int rectCount = OUT[0];
        int linesCount = OUT[1];
        int bezierCount = OUT[2];
        initBufferWorkspaces(commandCount, rectCount, linesCount, bezierCount);

        ByteBuffer view = stream.view();

        int pos = 0;
        int end = view.limit();

        int count = 0;

        while (pos < end) {
            int op = view.getInt(pos + SG_CmdHeader.OP);
            int flags = view.getInt(pos + SG_CmdHeader.FLAGS);
            int size = view.getInt(pos + SG_CmdHeader.SIZE);
            int used = view.getInt(pos + SG_CmdHeader.USED);

            Preconditions.checkState((flags & SG_GuiOp.FLAG_COMPILED) != 0,
                    "Command (pos=%s, op=%s) must be compiled already.", pos, op);

            if (op == SG_GuiOp.DRAW_RECT) {
                count++;
                int _pos = writeRectPayload(view, pos, flags, used);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.DRAW_LINES) {
                count++;
                int _pos = writeLinesPayload(view, pos, flags);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.DRAW_BEZIER) {
                count++;
                int _pos = writeBezierPayload(view, pos, flags);
                int __pos = writeDrawInfo(view, op, pos, flags, used, _pos);
                writeIdb(view, op, pos, flags, used, __pos);

            } else if (op == SG_GuiOp.PUSH_CLIP) {
                if (count > 0) {
                    flush(count);
                    count = 0;
                }
                pushClip();

            } else if (op == SG_GuiOp.POP_CLIP) {
                if (count > 0) {
                    flush(count);
                    count = 0;
                }
                popClip();

            } else {
                throw new IllegalStateException("Unknown SG_GuiOp: " + op);
            }

            pos += size;
        }

        if (count > 0) {
            flush(count);
            count = 0;
        }
    }

    private void pushClip() {

    }

    private void popClip() {

    }

    private void flush(int count) {
        if (drawInfoWorkspace != null) {
            drawInfoWorkspace.flip();
            drawInfo.bind();
            // orphaning
            drawInfo.alloc(drawInfoWorkspace.remaining(), BufferUploadHint.STREAM_DRAW);
            drawInfo.uploadBySubData(0, drawInfoWorkspace);
        }

        if (rectPayloadWorkspace != null) {
            rectPayloadWorkspace.flip();
            rectPayload.bind();
            // orphaning
            rectPayload.alloc(rectPayloadWorkspace.remaining(), BufferUploadHint.STREAM_DRAW);
            rectPayload.uploadBySubData(0, rectPayloadWorkspace);
        }

        if (linesPayloadWorkspace != null) {
            linesPayloadWorkspace.flip();
            linesPayload.bind();
            // orphaning
            linesPayload.alloc(linesPayloadWorkspace.remaining(), BufferUploadHint.STREAM_DRAW);
            linesPayload.uploadBySubData(0, linesPayloadWorkspace);
        }

        if (bezierPayloadWorkspace != null) {
            bezierPayloadWorkspace.flip();
            bezierPayload.bind();
            // orphaning
            bezierPayload.alloc(bezierPayloadWorkspace.remaining(), BufferUploadHint.STREAM_DRAW);
            bezierPayload.uploadBySubData(0, bezierPayloadWorkspace);
        }

        if (idbWorkspace != null) {
            idbWorkspace.flip();
            idb.bind();
            // orphaning
            idb.alloc(idbWorkspace.remaining(), BufferUploadHint.STREAM_DRAW);
            idb.uploadBySubData(0, idbWorkspace);
        }

        resetBufferWorkspaces();

        arenaSsbo.bind();
        // orphaning
        arenaSsbo.alloc(arena.view().remaining(), BufferUploadHint.STREAM_DRAW);
        arenaSsbo.uploadBySubData(0, arena.view());

        GL30.glBindBufferBase(drawInfo.target(), 0, drawInfo.bufferID);
        GL30.glBindBufferBase(rectPayload.target(), 1, rectPayload.bufferID);
        GL30.glBindBufferBase(arenaSsbo.target(), 2, arenaSsbo.bufferID);

        program.use();

        int scaledResLoc = GL20.glGetUniformLocation(program.getProgramID(), "scaledRes");
        int useDepthLoc = GL20.glGetUniformLocation(program.getProgramID(), "useDepth");

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        float screenWidth = (float) resolution.getScaledWidth_double();
        float screenHeight = (float) resolution.getScaledHeight_double();

        GL20.glUniform2f(scaledResLoc, screenWidth, screenHeight);
        GL20.glUniform1i(useDepthLoc, 1);

        idb.bind();
        dummyVao.bind();
        GL43.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0, count, IDB_STRIDE);
        VAO.bind(0);
        SSBOView.bindRaw(0);
        IDBView.bindRaw(0);

        program.use0();
    }
}
