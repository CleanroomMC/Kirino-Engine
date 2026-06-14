package com.cleanroommc.kirino.ui.simpletext.backend;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.*;
import com.cleanroommc.kirino.ui.simpletext.ST_Bitmap;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextConsumer;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.AbstractPagedAtlas;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGenerator;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DebugTextRenderer implements SimpleTextConsumer {

    private final SimpleTextRuntime context;
    private final SDFGenerator generator;
    private final Tex2DGlyphAtlas glyphAtlas;

    // key: glyph index
    private final Map<Integer, AbstractPagedAtlas.SlotHandle<Texture2DAccessor>> glyphSlotCache = new HashMap<>();
    private final List<Integer> failedGlyphHistory = new ArrayList<>();

    private final VBOView instanceVbo;
    private final VAO vao;
    private final ShaderProgram program;

    private static final AttributeLayout attributeLayout;

    static {
        attributeLayout = new AttributeLayout();
        attributeLayout.push(new Stride(48)
                .push(new Slot(Type.FLOAT, 4).setDivisor(1)) // uv 16
                .push(new Slot(Type.FLOAT, 4).setDivisor(1)) // rect 16
                .push(new Slot(Type.FLOAT, 1).setDivisor(1)) // size 4
                .push(new Slot(Type.INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND)) // color 4
                .push(new Slot(Type.UNSIGNED_INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND)) // page 4
                .push(new Slot(Type.INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND))); // hint 4
    }

    public DebugTextRenderer(
            SimpleTextRuntime context,
            SDFGenerator generator,
            Tex2DGlyphAtlas glyphAtlas,
            ImmediateShaderAccess shaderAccess) {

        this.context = context;
        this.generator = generator;
        this.glyphAtlas = glyphAtlas;
        instanceVbo = new VBOView(new GLBuffer());
        vao = new VAO(attributeLayout, null, instanceVbo);

        Shader vert = shaderAccess.makeShader(new ResourceLocation("forge:shaders/simpletext_font_test.vert"));
        Shader frag = shaderAccess.makeShader(new ResourceLocation("forge:shaders/simpletext_font_test.frag"));
        shaderAccess.submitToGL(vert, frag);
        program = shaderAccess.makeProgram(vert, frag);
    }

    private void genGlyphs(TextCommandList commandList) {
        for (int i = 0; i < commandList.size(); i++) {
            int glyph = commandList.glyphIndex(i);
            if (glyph == 0) {
                continue;
            }
            if (glyphSlotCache.containsKey(glyph)) {
                continue;
            }
            if (failedGlyphHistory.contains(glyph)) {
                continue;
            }

            ST_Bitmap bitmap = context.getFont().loadGlyph(glyph, context.getConfig().payload(), null);

            if (bitmap == null || bitmap.byteBuffer() == null) {
                failedGlyphHistory.add(glyph);
            } else {
                try (SDFBitmap sdfBitmap = generator.compute(bitmap)) {
                    var handle = glyphAtlas.allocate(sdfBitmap);
                    glyphSlotCache.put(glyph, handle);
                }
            }
        }
    }

    private void uploadGlyphs(TextCommandList commandList) {
        int bufferSize = attributeLayout.getFirstStride().getSize() * commandList.size();

        instanceVbo.bind();
        // orphaning
        instanceVbo.alloc(bufferSize, BufferUploadHint.STREAM_DRAW);

        ByteBuffer buffer = MemoryUtil.memAlloc(bufferSize);

        for (int i = 0; i < commandList.size(); i++) {
            int glyph = commandList.glyphIndex(i);
            var handle = glyphSlotCache.get(glyph);

            if (glyph == 0 || handle == null) {
                buffer
                        .putFloat(0f)
                        .putFloat(0f)
                        .putFloat(0f)
                        .putFloat(0f)
                        .putFloat(commandList.x(i))
                        .putFloat(commandList.y(i))
                        .putFloat(commandList.width(i))
                        .putFloat(commandList.height(i))
                        .putFloat(commandList.size(i))
                        .putInt(commandList.color(i))
                        .putInt(0)
                        .putInt(commandList.hint(i));
                continue;
            }

            Preconditions.checkNotNull(handle);

            buffer
                    .putFloat(handle.u0(glyphAtlas.getPageWidth(), 0))
                    .putFloat(handle.v0(glyphAtlas.getPageHeight(), 0))
                    .putFloat(handle.u1(glyphAtlas.getPageWidth(), 0))
                    .putFloat(handle.v1(glyphAtlas.getPageHeight(), 0))
                    .putFloat(commandList.x(i))
                    .putFloat(commandList.y(i))
                    .putFloat(commandList.width(i))
                    .putFloat(commandList.height(i))
                    .putFloat(commandList.size(i))
                    .putInt(commandList.color(i))
                    .putInt(handle.getPageIndex())
                    .putInt(commandList.hint(i));
        }

        instanceVbo.uploadBySubData(0, buffer.flip());
        MemoryUtil.memFree(buffer);
        instanceVbo.bind(0);
    }

    @Override
    public void consume(TextCommandList commandList) {
        genGlyphs(commandList);
        uploadGlyphs(commandList);

        program.use();

        int scaledResLoc = GL20.glGetUniformLocation(program.getProgramID(), "scaledRes");
        int atlasLoc = GL20.glGetUniformLocation(program.getProgramID(), "atlas");

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        float screenWidth = (float) resolution.getScaledWidth_double();
        float screenHeight = (float) resolution.getScaledHeight_double();

        GL20.glUniform2f(scaledResLoc, screenWidth, screenHeight);
        GL20.glUniform1i(atlasLoc, 6);

        GL13.glActiveTexture(GL13.GL_TEXTURE6);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glyphAtlas.getPage(0).textureID());

        vao.bind();
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, commandList.size());
        VAO.bind(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        program.use0();
    }
}
