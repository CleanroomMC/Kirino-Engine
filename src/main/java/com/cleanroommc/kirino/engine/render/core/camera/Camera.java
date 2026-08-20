package com.cleanroommc.kirino.engine.render.core.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.nio.FloatBuffer;

public interface Camera {
    @NonNull Matrix4f getProjectionMatrix();
    @NonNull FloatBuffer getProjectionBuffer();
    @NonNull Matrix4f getViewRotationMatrix();
    @NonNull FloatBuffer getViewRotationBuffer();
    @NonNull Vector3f getWorldOffset();
}
