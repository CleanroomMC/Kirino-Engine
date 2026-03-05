package com.cleanroommc.kirino.gl.dispatch;

import java.nio.FloatBuffer;

sealed class OGL21 extends OGL20 permits OGL43 {
    @Override
    public void glUniformMatrix2x3(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix2x3(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix2x4(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix2x4(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix3x2(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix3x2(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix3x4(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix3x4(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix4x2(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix4x2(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix4x3(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix4x3(location, transpose, matrices);
    }
}
