package com.cleanroommc.kirino.ui.transform;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

public final class UiCameraSnapshot {

    private final Matrix4f projection;
    private final Matrix4f view;
    final double originX;
    final double originY;
    final double originZ;

    private UiCameraSnapshot(
            Matrix4fc projection,
            Matrix4fc view,
            double originX,
            double originY,
            double originZ) {

        UiTransformChecks.matrix(projection);
        UiTransformChecks.matrix(view);
        UiTransformChecks.position(originX, originY, originZ);

        this.projection = new Matrix4f(projection);
        this.view = new Matrix4f(view);
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
    }

    /**
     * No camera origin is subtracted separately.
     */
    public static @NonNull UiCameraSnapshot of(@NonNull Matrix4fc projection, @NonNull Matrix4fc view) {
        return new UiCameraSnapshot(projection, view, 0, 0, 0);
    }

    /**
     * The mapping is <code>projection * viewRot * vec4(worldPosition - worldOffset, 1)</code>.
     */
    public static @NonNull UiCameraSnapshot relative(
            @NonNull Matrix4fc projection,
            @NonNull Matrix4fc relativeView,
            double originX,
            double originY,
            double originZ) {

        return new UiCameraSnapshot(projection, relativeView, originX, originY, originZ);
    }

    /**
     * Projects a placement already expressed relative to this camera's world offset.
     *
     * <p>Note: The world offset must already be subtracted from the placement.
     * This method does not subtract it again.</p>
     */
    Matrix4f project(Matrix4fc relativePlacement, Matrix4f destination) {
        return projection.mul(view, destination).mul(relativePlacement);
    }
}
