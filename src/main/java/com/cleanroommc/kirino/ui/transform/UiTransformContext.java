package com.cleanroommc.kirino.ui.transform;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.joml.Matrix4dc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class UiTransformContext {

    public enum Mode {
        NDC,
        LEGACY_GL,
        MATRIX
    }

    private static final UiTransformContext NDC = new UiTransformContext(Mode.NDC, null, 0, 0);
    private static final UiTransformContext LEGACY_GUI = new UiTransformContext(Mode.LEGACY_GL, null, 0, 0);

    private final Mode mode;
    private final @Nullable Matrix4f matrix;
    private final float logicalWidth;
    private final float logicalHeight;

    private UiTransformContext(Mode mode, @Nullable Matrix4fc matrix, float logicalWidth, float logicalHeight) {
        if (matrix != null) {
            UiTransformChecks.matrix(matrix);
        }

        this.mode = mode;
        this.matrix = matrix == null ? null : new Matrix4f(matrix);
        this.logicalWidth = logicalWidth;
        this.logicalHeight = logicalHeight;
    }

    @NonNull
    public static UiTransformContext ndc() {
        return NDC;
    }

    @NonNull
    public static UiTransformContext legacyGui() {
        return LEGACY_GUI;
    }

    @NonNull
    public static UiTransformContext legacy(@NonNull Matrix4fc ndcToLegacyLocal) {
        Preconditions.checkNotNull(ndcToLegacyLocal);

        return new UiTransformContext(Mode.LEGACY_GL, ndcToLegacyLocal, 0, 0);
    }

    @NonNull
    public static UiTransformContext ndcToClip(@NonNull Matrix4fc ndcToClip) {
        Preconditions.checkNotNull(ndcToClip);

        return new UiTransformContext(Mode.MATRIX, ndcToClip, 0, 0);
    }

    @NonNull
    public static UiTransformContext hologram(@NonNull UiCameraSnapshot camera, @NonNull UiWorldCanvas canvas) {
        Preconditions.checkNotNull(camera);
        Preconditions.checkNotNull(canvas);

        Matrix4f placement = canvas.relativePlacement(camera, new Matrix4f());
        return ndcToClip(camera.project(placement, new Matrix4f())).withLogicalSize(canvas.logicalWidth(), canvas.logicalHeight());
    }

    @NonNull
    public static UiTransformContext scene(@NonNull UiCameraSnapshot camera, @NonNull Matrix4dc ndcToWorld) {
        Preconditions.checkNotNull(camera);
        Preconditions.checkNotNull(ndcToWorld);
        Preconditions.checkArgument(ndcToWorld.m03() == 0 && ndcToWorld.m13() == 0 && ndcToWorld.m23() == 0 && ndcToWorld.m33() == 1,
                "NDC-to-world placement must be affine.");

        Matrix4f placement = new Matrix4f(ndcToWorld)
                .m30((float) (ndcToWorld.m30() - camera.originX))
                .m31((float) (ndcToWorld.m31() - camera.originY))
                .m32((float) (ndcToWorld.m32() - camera.originZ));

        UiTransformChecks.matrix(placement);

        return ndcToClip(camera.project(placement, new Matrix4f()));
    }

    @NonNull
    public UiTransformContext withLogicalSize(float logicalWidth, float logicalHeight) {
        UiTransformChecks.positive(logicalWidth, "logicalWidth");
        UiTransformChecks.positive(logicalHeight, "logicalHeight");

        return new UiTransformContext(mode, matrix, logicalWidth, logicalHeight);
    }

    @NonNull
    public Mode mode() {
        return mode;
    }

    public boolean isDefault() {
        return mode == Mode.NDC && logicalWidth == 0;
    }

    public boolean acceptsLegacyTransformation() {
        return mode == Mode.LEGACY_GL;
    }

    public float logicalWidthOr(float fallback) {
        return logicalWidth == 0 ? fallback : logicalWidth;
    }

    public float logicalHeightOr(float fallback) {
        return logicalHeight == 0 ? fallback : logicalHeight;
    }

    @CanIgnoreReturnValue
    @NonNull
    public Matrix4f resolveMatrix(float logicalWidth, float logicalHeight, @NonNull Matrix4f destination) {
        Preconditions.checkNotNull(destination);
        UiTransformChecks.positive(logicalWidth, "logicalWidth");
        UiTransformChecks.positive(logicalHeight, "logicalHeight");

        if (matrix != null) {
            return destination.set(matrix);
        }

        destination.identity();
        if (mode == Mode.LEGACY_GL) {
            destination
                    .m00(logicalWidth * 0.5f)
                    .m11(-logicalHeight * 0.5f)
                    .m30(logicalWidth * 0.5f)
                    .m31(logicalHeight * 0.5f);
        }

        return destination;
    }
}
