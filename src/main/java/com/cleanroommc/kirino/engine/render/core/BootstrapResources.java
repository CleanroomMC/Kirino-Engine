package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.vao.VAO;

public class BootstrapResources {
    public final ResourceSlot<FrameFinalizer> frameFinalizer;
    public final ResourceSlot<IndirectDrawBufferGenerator> idbGenerator;
    public final ResourceSlot<VAO> fullscreenTriangleVao;
    public final ResourceSlot<VAO> dummyVao;

    public BootstrapResources(
            ResourceSlot<FrameFinalizer> frameFinalizer,
            ResourceSlot<IndirectDrawBufferGenerator> idbGenerator,
            ResourceSlot<VAO> fullscreenTriangleVao,
            ResourceSlot<VAO> dummyVao) {

        this.frameFinalizer = frameFinalizer;
        this.idbGenerator = idbGenerator;
        this.fullscreenTriangleVao = fullscreenTriangleVao;
        this.dummyVao = dummyVao;
    }
}
