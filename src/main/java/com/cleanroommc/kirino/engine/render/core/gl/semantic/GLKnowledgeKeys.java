package com.cleanroommc.kirino.engine.render.core.gl.semantic;

import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.semantic.KnowledgeKey;
import org.joml.Vector4i;

/**
 * This is meant to be only a subset of OpenGL, but this is strictly a superset of {@link PipelineStateObject}.
 */
public final class GLKnowledgeKeys {

    private GLKnowledgeKeys() {
    }

    public static final KnowledgeKey<Integer> SHADER_PROGRAM = KnowledgeKey.of(
            "gl",
            "shader_program",
            Integer.class);

    public static final KnowledgeKey<Integer> VAO = KnowledgeKey.of(
            "gl",
            "vao",
            Integer.class);

    public static final KnowledgeKey<Integer> VBO = KnowledgeKey.of(
            "gl",
            "buffer.array",
            Integer.class);

    public static final KnowledgeKey<Integer> EBO = KnowledgeKey.of(
            "gl",
            "buffer.element_array",
            Integer.class);

    public static final KnowledgeKey<Integer> IDB = KnowledgeKey.of(
            "gl",
            "buffer.draw_indirect",
            Integer.class);

    public static final KnowledgeKey<Integer> FBO_DRAW = KnowledgeKey.of(
            "gl",
            "framebuffer.draw",
            Integer.class);

    public static final KnowledgeKey<Integer> FBO_READ = KnowledgeKey.of(
            "gl",
            "framebuffer.read",
            Integer.class);

    public static final KnowledgeKey<Vector4i> VIEWPORT = KnowledgeKey.of(
            "gl",
            "viewport",
            Vector4i.class);

    public static final KnowledgeKey<Boolean> SCISSOR = KnowledgeKey.of(
            "gl",
            "scissor.enable",
            Boolean.class);

    public static final KnowledgeKey<Vector4i> SCISSOR_BOX = KnowledgeKey.of(
            "gl",
            "scissor.box",
            Vector4i.class);

    public static final KnowledgeKey<Boolean> DEPTH = KnowledgeKey.of(
            "gl",
            "depth.enable",
            Boolean.class);

    public static final KnowledgeKey<Integer> DEPTH_FUNC = KnowledgeKey.of(
            "gl",
            "depth.func",
            Integer.class);

    public static final KnowledgeKey<Boolean> DEPTH_MASK = KnowledgeKey.of(
            "gl",
            "depth.mask",
            Boolean.class);

    public static final KnowledgeKey<Boolean> BLEND = KnowledgeKey.of(
            "gl",
            "blend.enable",
            Boolean.class);

    public static final KnowledgeKey<Integer> BLEND_FUNC_SRC_RGB = KnowledgeKey.of(
            "gl",
            "blend.func.src_rgb",
            Integer.class);

    public static final KnowledgeKey<Integer> BLEND_FUNC_DST_RGB = KnowledgeKey.of(
            "gl",
            "blend.func.dst_rgb",
            Integer.class);

    public static final KnowledgeKey<Integer> BLEND_FUNC_SRC_ALPHA = KnowledgeKey.of(
            "gl",
            "blend.func.src_alpha",
            Integer.class);

    public static final KnowledgeKey<Integer> BLEND_FUNC_DST_ALPHA = KnowledgeKey.of(
            "gl",
            "blend.func.dst_alpha",
            Integer.class);

    public static final KnowledgeKey<Integer> BLEND_EQ_RGB = KnowledgeKey.of(
            "gl",
            "blend.eq.rgb",
            Integer.class);

    public static final KnowledgeKey<Integer> BLEND_EQ_ALPHA = KnowledgeKey.of(
            "gl",
            "blend.eq.alpha",
            Integer.class);

    public static final KnowledgeKey<Boolean> COLOR_MASK_R = KnowledgeKey.of(
            "gl",
            "color.mask.r",
            Boolean.class);

    public static final KnowledgeKey<Boolean> COLOR_MASK_G = KnowledgeKey.of(
            "gl",
            "color.mask.g",
            Boolean.class);

    public static final KnowledgeKey<Boolean> COLOR_MASK_B = KnowledgeKey.of(
            "gl",
            "color.mask.b",
            Boolean.class);

    public static final KnowledgeKey<Boolean> COLOR_MASK_A = KnowledgeKey.of(
            "gl",
            "color.mask.a",
            Boolean.class);

    public static final KnowledgeKey<Boolean> CULL = KnowledgeKey.of(
            "gl",
            "cull.enable",
            Boolean.class);

    public static final KnowledgeKey<Integer> CULL_FACE = KnowledgeKey.of(
            "gl",
            "cull.face",
            Integer.class);

    public static final KnowledgeKey<Integer> FRONT_FACE = KnowledgeKey.of(
            "gl",
            "front_face",
            Integer.class);

    public static final KnowledgeKey<Integer> POLYGON_MODE_FRONT_N_BACK = KnowledgeKey.of(
            "gl",
            "polygon.mode",
            Integer.class);

    public static final KnowledgeKey<Boolean> POLYGON_OFFSET = KnowledgeKey.of(
            "gl",
            "polygon.offset",
            Boolean.class);

    public static final KnowledgeKey<Float> POLYGON_OFFSET_FACTOR = KnowledgeKey.of(
            "gl",
            "polygon.offset.factor",
            Float.class);

    public static final KnowledgeKey<Float> POLYGON_OFFSET_UNITS = KnowledgeKey.of(
            "gl",
            "polygon.offset.units",
            Float.class);

    public static final KnowledgeKey<Boolean> STENCIL = KnowledgeKey.of(
            "gl",
            "stencil.enable",
            Boolean.class);

    public static final KnowledgeKey<Integer> STENCIL_MASK = KnowledgeKey.of(
            "gl",
            "stencil.mask",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_FUNC = KnowledgeKey.of(
            "gl",
            "stencil.func",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_FUNC_FACE = KnowledgeKey.of(
            "gl",
            "stencil.func.face",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_FUNC_REF = KnowledgeKey.of(
            "gl",
            "stencil.func.ref",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_FUNC_MASK = KnowledgeKey.of(
            "gl",
            "stencil.func.mask",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_OP_SFAIL = KnowledgeKey.of(
            "gl",
            "stencil.op.sfail",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_OP_DPFAIL = KnowledgeKey.of(
            "gl",
            "stencil.op.dpfail",
            Integer.class);

    public static final KnowledgeKey<Integer> STENCIL_OP_DPPASS = KnowledgeKey.of(
            "gl",
            "stencil.op.dppass",
            Integer.class);

    public static final KnowledgeKey<Integer> TEXTURE_ACTIVE_UNIT = KnowledgeKey.of(
            "gl",
            "texture.active_unit",
            Integer.class);
}
