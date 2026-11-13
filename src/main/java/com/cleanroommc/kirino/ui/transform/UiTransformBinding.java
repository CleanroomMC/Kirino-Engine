package com.cleanroommc.kirino.ui.transform;

import com.google.common.base.Preconditions;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL41;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

/**
 * <p>Note: This is not GL agnostic. It requires a GL context.</p>
 * <p>Note: Uniform locations are queried once. No GL state is read at draw time.</p>
 */
public final class UiTransformBinding {

    private final int programId;
    private final int modeLocation;
    private final int matrixLocation;
    private final Matrix4f workspace = new Matrix4f();

    /**
     * It immediately queries the program uniform locations through GL.
     */
    public UiTransformBinding(int programId) {
        this.programId = programId;
        modeLocation = GL20.glGetUniformLocation(programId, "KirinoEngine_UI_transformMode");
        matrixLocation = GL20.glGetUniformLocation(programId, "KirinoEngine_UI_postNdcTransform");
    }

    public void validate(@NonNull UiTransformContext context) {
        Preconditions.checkNotNull(context);

        if (context.mode() != UiTransformContext.Mode.NDC && (modeLocation < 0 || matrixLocation < 0)) {
            throw new UnsupportedOperationException("This UI shader does not implement the post-NDC transform ABI.");
        }
    }

    /**
     * Uploads the transform. The owning shader program must already be current.
     */
    public void bind(@NonNull UiTransformContext context, float logicalWidth, float logicalHeight) {
        bind(context, logicalWidth, logicalHeight, false);
    }

    /**
     * Uploads the transform.
     *
     * @param dsa When <code>true</code>, uses DSA path thus not requiring a bound program.
     *            When <code>false</code>, the owning program must already be current
     */
    public void bind(
            @NonNull UiTransformContext context,
            float logicalWidth,
            float logicalHeight,
            boolean dsa) {

        validate(context);

        int mode = switch (context.mode()) {
            case NDC -> 0;
            case LEGACY_GL -> 1;
            case MATRIX -> 2;
        };

        if (modeLocation >= 0) {
            if (dsa) {
                GL41.glProgramUniform1i(programId, modeLocation, mode);
            } else {
                GL20.glUniform1i(modeLocation, mode);
            }
        }

        if (mode != 0) {
            context.resolveMatrix(logicalWidth, logicalHeight, workspace);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer matrix = workspace.get(stack.mallocFloat(16));
                if (dsa) {
                    GL41.glProgramUniformMatrix4fv(programId, matrixLocation, false, matrix);
                } else {
                    GL20.glUniformMatrix4fv(matrixLocation, false, matrix);
                }
            }
        }
    }
}
