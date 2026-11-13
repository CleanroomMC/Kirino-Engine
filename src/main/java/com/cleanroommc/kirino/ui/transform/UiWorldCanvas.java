package com.cleanroommc.kirino.ui.transform;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.google.common.base.Preconditions;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.NonNull;

public final class UiWorldCanvas {

    private final float logicalWidth;
    private final float logicalHeight;
    private final float worldWidth;
    private final float worldHeight;
    private final double x;
    private final double y;
    private final double z;
    private final Quaternionf rotation;

    private UiWorldCanvas(Builder builder) {
        logicalWidth = builder.logicalWidth;
        logicalHeight = builder.logicalHeight;
        worldWidth = builder.worldWidth;
        worldHeight = builder.worldHeight;
        x = builder.x;
        y = builder.y;
        z = builder.z;
        rotation = new Quaternionf(builder.rotation);
    }

    @NonNull
    public static Builder builder(float logicalWidth, float logicalHeight) {
        return new Builder(logicalWidth, logicalHeight);
    }

    public float logicalWidth() {
        return logicalWidth;
    }

    public float logicalHeight() {
        return logicalHeight;
    }

    public float worldWidth() {
        return worldWidth;
    }

    public float worldHeight() {
        return worldHeight;
    }

    Matrix4f relativePlacement(UiCameraSnapshot camera, Matrix4f destination) {
        return destination
                .translation((float) (x - camera.originX), (float) (y - camera.originY), (float) (z - camera.originZ))
                .rotate(rotation)
                .translate(worldWidth * 0.5f, -worldHeight * 0.5f, 0)
                .scale(worldWidth * 0.5f, worldHeight * 0.5f, 1);
    }

    public static final class Builder {

        private final float logicalWidth;
        private final float logicalHeight;
        private float worldWidth;
        private float worldHeight;
        private double x;
        private double y;
        private double z;
        private final Quaternionf rotation = new Quaternionf();

        private Builder(float logicalWidth, float logicalHeight) {
            UiTransformChecks.positive(logicalWidth, "logicalWidth");
            UiTransformChecks.positive(logicalHeight, "logicalHeight");

            this.logicalWidth = logicalWidth;
            this.logicalHeight = logicalHeight;
        }

        /**
         * Sets the top-left pivot, in world units.
         */
        @CanIgnoreReturnValue
        @NonNull
        public Builder position(double x, double y, double z) {
            UiTransformChecks.position(x, y, z);
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        /**
         * No need to match the logical aspect ratio.
         */
        @CanIgnoreReturnValue
        @NonNull
        public Builder worldSize(float worldWidth, float worldHeight) {
            UiTransformChecks.positive(worldWidth, "worldWidth");
            UiTransformChecks.positive(worldHeight, "worldHeight");

            this.worldWidth = worldWidth;
            this.worldHeight = worldHeight;
            return this;
        }

        @CanIgnoreReturnValue
        @NonNull
        public Builder unitsPerWorldUnit(float logicalUnits) {
            UiTransformChecks.positive(logicalUnits, "logicalUnits");

            return worldSize(logicalWidth / logicalUnits, logicalHeight / logicalUnits);
        }

        /**
         * Rotation is about the top-left pivot.
         */
        @CanIgnoreReturnValue
        @NonNull
        public Builder rotation(@NonNull Quaternionfc rotation) {
            Preconditions.checkNotNull(rotation);
            float lengthSquared = rotation.lengthSquared();
            Preconditions.checkArgument(Float.isFinite(lengthSquared) && lengthSquared > 0,
                    "Rotation must be finite and nonzero.");

            this.rotation.set(rotation).normalize();
            return this;
        }

        @NonNull
        public UiWorldCanvas build() {
            UiTransformChecks.positive(worldWidth, "worldWidth");
            UiTransformChecks.positive(worldHeight, "worldHeight");

            return new UiWorldCanvas(this);
        }
    }
}
