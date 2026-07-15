package com.cleanroommc.kirino.engine.render.core.pipeline;

import com.cleanroommc.kirino.engine.render.core.pipeline.state.BlendState;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.DepthState;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.RasterState;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public final class PSOPresets {

    private PSOPresets() {
    }

    @NonNull
    public static PipelineStateObject createScreenOverwritePSO(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ZERO,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        false,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        false,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    @NonNull
    public static PipelineStateObject createGizmosPSO(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        return new PipelineStateObject(
                new BlendState(
                        true,
                        false,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ZERO,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        true,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        false,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    @NonNull
    public static PipelineStateObject createOpaquePSO(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0b1111
                ),
                new DepthState(
                        true,
                        true,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        true,
                        1.0f,
                        1.0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    @NonNull
    public static PipelineStateObject createCutoutPSO(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        return new PipelineStateObject(
                new BlendState(
                        false,
                        false,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0b1111
                ),
                new DepthState(
                        true,
                        true,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        0f,
                        0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }

    @NonNull
    public static PipelineStateObject createTransparentPSO(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        return new PipelineStateObject(
                new BlendState(
                        true,
                        true,
                        GL11.GL_SRC_ALPHA,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL11.GL_ONE,
                        GL11.GL_ONE_MINUS_SRC_ALPHA,
                        GL14.GL_FUNC_ADD,
                        GL14.GL_FUNC_ADD,
                        0b1111
                ),
                new DepthState(
                        true,
                        false,
                        GL11.GL_LEQUAL
                ),
                new RasterState(
                        true,
                        GL11.GL_BACK,
                        GL11.GL_CCW,
                        false,
                        0f,
                        0f,
                        GL11.GL_FILL
                ),
                shaderProgram
        );
    }
}
