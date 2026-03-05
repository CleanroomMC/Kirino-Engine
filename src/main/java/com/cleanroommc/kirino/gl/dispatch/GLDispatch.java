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
        else if (cp.OpenGL30)
            gl = new OGL30();
        else if (cp.OpenGL21)
            gl = new OGL21();
        else if (cp.OpenGL20)
            gl = new OGL20();
        else if (cp.OpenGL15)
            gl = new OGL15();
        else if (cp.OpenGL14)
            gl = new OGL14();
        else if (cp.OpenGL13)
            gl = new OGL13();
        else if (cp.OpenGL12)
            gl = new OGL12();
        else if (cp.OpenGL11)
            gl = new OGL11();
        else
            throw new Error("How?");
    }
}
