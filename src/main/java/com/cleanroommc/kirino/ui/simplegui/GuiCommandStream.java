package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.kirino.engine.ShutdownManager;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GuiCommandStream {

    private static final float MEM_GROW_FACTOR = 2f;
    private static final int MAX_SIZE = 1024 * 1024 * 16; // 16 MB

    private int capacity;
    private ByteBuffer buffer;

    GuiCommandStream(int capacity) {
        this.capacity = capacity;
        buffer = MemoryUtil.memAlloc(capacity);

        ShutdownManager.registerAsync(() -> {
            MemoryUtil.memFree(buffer);
        });
    }

    @NonNull
    ByteBuffer view() {
        ByteBuffer dup = buffer.duplicate();
        dup.order(buffer.order()).flip();
        return dup;
    }

    void reset() {
        buffer.clear();
    }

    private void grow(int target) {
        ByteBuffer oldBuffer = view();
        int newSize = (int) Math.max(target * MEM_GROW_FACTOR, oldBuffer.capacity() * MEM_GROW_FACTOR);

        if (newSize > MAX_SIZE) {
            throw new RuntimeException(String.format(
                    "GuiCommandStream failed to grow: want=%d, max=%d",
                    newSize, MAX_SIZE));
        }

        ByteBuffer newBuffer = MemoryUtil.memAlloc(newSize);
        newBuffer.put(oldBuffer);

        MemoryUtil.memFree(buffer);
        buffer = newBuffer;
        capacity = newSize;
    }

    /**
     * <p>Note: It assumes no arithmetic overflow regarding alignment calculation,
     * and doesn't handle such situation.</p>
     */
    private int begin(int op, int flags, int payload, int reserve) {
        int start = buffer.position();

        int used = SG_CmdHeader.HEADER_SIZE + payload;
        int size = (used + reserve + 15) & ~15;

        if (start + size > capacity) {
            grow(start + size);
        }

        buffer.putInt(op);
        buffer.putInt(flags);
        buffer.putInt(size);
        buffer.putInt(used);

        return start;
    }

    private void end(int start) {
        int size = buffer.getInt(start + SG_CmdHeader.SIZE);
        int end = start + size;

        while (buffer.position() < end) {
            buffer.put((byte) 0);
        }
    }

    public void rect(float x, float y, float width, float height, int color) {
        int start = begin(SG_GuiOp.DRAW_RECT, 0, 20, SG_CmdHeader.TAIL_SIZE);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(width);
        buffer.putFloat(height);
        buffer.putInt(color);

        end(start);
    }

    @NonNull
    public CmdRectBuilder rectEx(float x, float y, float width, float height, int color) {
        return new CmdRectBuilder(this, x, y, width, height, color);
    }

    void writeRectEx(float x, float y,
                     float width, float height,
                     int color,
                     int flags,
                     float radius,
                     int cornerType,
                     float borderWidth,
                     int borderColor,
                     float shadowBlur,
                     float shadowX,
                     float shadowY,
                     int shadowColor) {

        int payload = 20;

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            payload += 8;
        }
        if ((flags & SG_GuiOp.FLAG_BORDER) != 0) {
            payload += 8;
        }
        if ((flags & SG_GuiOp.FLAG_SHADOW) != 0) {
            payload += 16;
        }

        boolean needsMesh = (flags & SG_GuiOp.FLAG_RADIUS) != 0;

        // plus 2 ints (meshOffset + vertexCount) if needsMesh
        int start = begin(SG_GuiOp.DRAW_RECT, flags, payload, SG_CmdHeader.TAIL_SIZE + (needsMesh ? 8 : 0));

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(width);
        buffer.putFloat(height);
        buffer.putInt(color);

        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            buffer.putFloat(radius);
            buffer.putInt(cornerType);
        }

        if ((flags & SG_GuiOp.FLAG_BORDER) != 0) {
            buffer.putFloat(borderWidth);
            buffer.putInt(borderColor);
        }

        if ((flags & SG_GuiOp.FLAG_SHADOW) != 0) {
            buffer.putFloat(shadowBlur);
            buffer.putFloat(shadowX);
            buffer.putFloat(shadowY);
            buffer.putInt(shadowColor);
        }

        end(start);
    }

    @NonNull
    public CmdLinesBuilder lines(int vertexNum, float lineWidth, boolean formsLoop, int color) {
        return new CmdLinesBuilder(this, vertexNum, lineWidth, formsLoop, color);
    }

    void writeLines(
            int vertexNum,
            float lineWidth,
            float[] vertices,
            boolean formsLoop,
            int flags,
            int color0,
            int color1,
            int color2) {

        // color0 + vertexNum + lineWidth + vertices + formsLoop
        int payload = 12 + vertices.length * 4 + 1;

        if ((flags & SG_GuiOp.FLAG_COLOR1) != 0) {
            payload += 4;
        }
        if ((flags & SG_GuiOp.FLAG_COLOR2) != 0) {
            payload += 4;
        }

        // plus 2 ints (meshOffset0 + meshOffset1)
        int start = begin(SG_GuiOp.DRAW_LINES, flags, payload, SG_CmdHeader.TAIL_SIZE + 8);

        buffer.putInt(color0);

        if ((flags & SG_GuiOp.FLAG_COLOR1) != 0) {
            buffer.putInt(color1);
        }

        if ((flags & SG_GuiOp.FLAG_COLOR2) != 0) {
            buffer.putInt(color2);
        }

        buffer.putInt(vertexNum);
        buffer.putFloat(lineWidth);
        for (float f : vertices) {
            buffer.putFloat(f);
        }
        buffer.put((byte) (formsLoop ? 1 : 0));

        end(start);
    }
}
