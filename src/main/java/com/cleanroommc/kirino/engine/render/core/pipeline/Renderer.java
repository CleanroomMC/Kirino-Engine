package com.cleanroommc.kirino.engine.render.core.pipeline;

import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.engine.render.core.gl.semantic.GLKnowledgeKeys;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.BlendState;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.DepthState;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.RasterState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.KnowledgeCheckpoint;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

public final class Renderer {
    private final ResourceStorage resourceStorage;
    private final VAO dummyVao;

    public Renderer(ResourceStorage resourceStorage, VAO dummyVao) {
        this.resourceStorage = resourceStorage;
        this.dummyVao = dummyVao;
    }

    //<editor-fold desc="submit pso">
    public void bindPipeline(
            @NonNull PipelineStateObject pso,
            @NonNull KnowledgeRuntime glKnowledge) {

        Preconditions.checkNotNull(pso);
        Preconditions.checkNotNull(glKnowledge);

        applyBlend(pso.blendState());
        applyDepth(pso.depthState());
        applyRaster(pso.rasterState());

        int program = resourceStorage.get(pso.shaderProgram()).getProgramID();

        GL20.glUseProgram(program);

        glKnowledge.commit(cp -> commitPipeline(cp, pso, program));
    }

    private static void commitPipeline(KnowledgeCheckpoint cp, PipelineStateObject pso, int program) {
        commitBlend(cp, pso.blendState());
        commitDepth(cp, pso.depthState());
        commitRaster(cp, pso.rasterState());

        cp.know(GLKnowledgeKeys.SHADER_PROGRAM, program);
    }

    private static void commitBlend(KnowledgeCheckpoint cp, BlendState b) {
        cp.know(GLKnowledgeKeys.BLEND, b.enabled());

        if (b.separate()) {
            cp.know(GLKnowledgeKeys.BLEND_FUNC_SRC_RGB, b.srcRGB());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_DST_RGB, b.dstRGB());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_SRC_ALPHA, b.srcAlpha());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_DST_ALPHA, b.dstAlpha());
        } else {
            cp.know(GLKnowledgeKeys.BLEND_FUNC_SRC_RGB, b.srcRGB());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_DST_RGB, b.dstRGB());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_SRC_ALPHA, b.srcRGB());
            cp.know(GLKnowledgeKeys.BLEND_FUNC_DST_ALPHA, b.dstRGB());
        }

        cp.know(GLKnowledgeKeys.BLEND_EQ_RGB, b.eqRGB());
        cp.know(GLKnowledgeKeys.BLEND_EQ_ALPHA, b.eqAlpha());

        int mask = b.colorWriteMask();

        cp.know(GLKnowledgeKeys.COLOR_MASK_R, (mask & 0b0001) != 0);
        cp.know(GLKnowledgeKeys.COLOR_MASK_G, (mask & 0b0010) != 0);
        cp.know(GLKnowledgeKeys.COLOR_MASK_B, (mask & 0b0100) != 0);
        cp.know(GLKnowledgeKeys.COLOR_MASK_A, (mask & 0b1000) != 0);
    }

    private static void commitDepth(KnowledgeCheckpoint cp, DepthState d) {
        cp.know(GLKnowledgeKeys.DEPTH, d.depthTest());
        if (d.depthTest()) {
            cp.know(GLKnowledgeKeys.DEPTH_FUNC, d.depthFunc());
        }
        cp.know(GLKnowledgeKeys.DEPTH_MASK, d.depthWrite());
    }

    private static void commitRaster(KnowledgeCheckpoint cp, RasterState r) {
        cp.know(GLKnowledgeKeys.CULL, r.cullEnable());
        if (r.cullEnable()) {
            cp.know(GLKnowledgeKeys.CULL_FACE, r.cullFace());
        }
        cp.know(GLKnowledgeKeys.FRONT_FACE, r.frontFace());
        cp.know(GLKnowledgeKeys.POLYGON_MODE_FRONT_N_BACK, r.polygonMode());
        cp.know(GLKnowledgeKeys.POLYGON_OFFSET, r.polygonOffset());
        if (r.polygonOffset()) {
            cp.know(GLKnowledgeKeys.POLYGON_OFFSET_FACTOR, r.polygonOffsetFactor());
            cp.know(GLKnowledgeKeys.POLYGON_OFFSET_UNITS, r.polygonOffsetUnits());
        }
    }

    private static void applyBlend(BlendState b) {
        if (!b.enabled()) {
            GL11.glDisable(GL11.GL_BLEND);
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        if (b.separate()) {
            GL14.glBlendFuncSeparate(b.srcRGB(), b.dstRGB(), b.srcAlpha(), b.dstAlpha());
            GL20.glBlendEquationSeparate(b.eqRGB(), b.eqAlpha());
        } else {
            GL11.glBlendFunc(b.srcRGB(), b.dstRGB());
            GL14.glBlendEquation(b.eqRGB());
        }
        int mask = b.colorWriteMask();
        boolean r = (mask & 0b0001) != 0;
        boolean g = (mask & 0b0010) != 0;
        boolean _b = (mask & 0b0100) != 0;
        boolean a = (mask & 0b1000) != 0;
        GL11.glColorMask(r, g, _b, a);
    }

    private static void applyDepth(DepthState d) {
        if (d.depthTest()) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthFunc(d.depthFunc());
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }
        GL11.glDepthMask(d.depthWrite());
    }

    private static void applyRaster(RasterState r) {
        if (r.cullEnable()) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(r.cullFace());
        } else {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
        GL11.glFrontFace(r.frontFace());
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, r.polygonMode());
        if (r.polygonOffset()) {
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(r.polygonOffsetFactor(), r.polygonOffsetUnits());
        } else {
            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }
    //</editor-fold>

    /**
     * Accepts a low-level draw command and submits the corresponding OpenGL command to GPU.
     * Notice, <code>command</code> with type <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> is an auxiliary command and will be ignored here.
     * By the way, <code>command</code> will be recycled automatically here.
     *
     * @param command The low-level draw command
     */
    public void draw(@NonNull LowLevelDC command) {
        Preconditions.checkNotNull(command);

        if (command.type == LowLevelDC.DrawType.ELEMENTS ||
                command.type == LowLevelDC.DrawType.ELEMENTS_INSTANCED ||
                command.type == LowLevelDC.DrawType.MULTI_ELEMENTS_INDIRECT) {

            GL30.glBindVertexArray(command.vao);

            switch (command.type) {
                case ELEMENTS -> {
                    GL11.glDrawElements(command.mode, command.indicesCount, command.elementType, command.eboOffset);
                }
                case ELEMENTS_INSTANCED -> {
                    GL31.glDrawElementsInstanced(command.mode, command.indicesCount, command.elementType, command.eboOffset, command.instanceCount);
                }
                case MULTI_ELEMENTS_INDIRECT -> {
                    GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, command.idb);
                    GL43.glMultiDrawElementsIndirect(command.mode, command.elementType, command.idbOffset, command.instanceCount, command.idbStride);
                }
            }

            KirinoClientDebug.RenderStatsFrame$incrementDrawCalls();
        }

        command.recycle();
    }

    /**
     * Trigger a shader without binding any actual data.
     */
    public void dummyDraw(int mode, int first, int count) {
        dummyVao.bind();
        GL11.glDrawArrays(mode, first, count);

        KirinoClientDebug.RenderStatsFrame$incrementDrawCalls();
    }
}
