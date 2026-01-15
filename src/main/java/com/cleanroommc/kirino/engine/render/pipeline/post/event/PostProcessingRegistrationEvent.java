package com.cleanroommc.kirino.engine.render.pipeline.post.event;

import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.vao.VAO;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class PostProcessingRegistrationEvent extends Event {
    private final List<Triple<String, String[], TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass>>> postProcessingEntries = new ArrayList<>();

    public void register(String subpassName, String[] shaderProgram, TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass> subpassCtor) {
        postProcessingEntries.add(Triple.of(subpassName, shaderProgram, subpassCtor));
    }
}
