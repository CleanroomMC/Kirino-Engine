package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

public class GLDispatch {

    private OpenGL gl;

    public GLDispatch() {
        GLCapabilities cp = GL.getCapabilities();
        if (cp.OpenGL45)
            gl = new OGL45();
        else if (cp.OpenGL44)
            gl = new OGL44();
        else if (cp.OpenGL43)
            gl = new OGL43();
    }
}
