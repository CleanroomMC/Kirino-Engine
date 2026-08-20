package com.cleanroommc.kirino.engine.render.core.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.nio.FloatBuffer;

public class FreeCamera implements Camera{

    @NonNull
    @Override
    public Matrix4f getProjectionMatrix() {
        return null;
    }

    @NonNull
    @Override
    public FloatBuffer getProjectionBuffer() {
        return null;
    }

    @NonNull
    @Override
    public Matrix4f getViewRotationMatrix() {
        return null;
    }

    @NonNull
    @Override
    public FloatBuffer getViewRotationBuffer() {
        return null;
    }

    @NonNull
    @Override
    public Vector3f getWorldOffset() {
        return null;
    }
}
