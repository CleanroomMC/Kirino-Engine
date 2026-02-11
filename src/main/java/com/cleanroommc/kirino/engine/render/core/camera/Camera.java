package com.cleanroommc.kirino.engine.render.core.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;

public interface Camera {
    Matrix4f getProjectionMatrix();
    FloatBuffer getProjectionBuffer();
    Matrix4f getViewRotationMatrix();
    FloatBuffer getViewRotationBuffer();
    Vector3f getWorldOffset();
}
