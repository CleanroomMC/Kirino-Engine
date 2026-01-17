package com.cleanroommc.kirino.engine.render.core.pipeline.post.event;

import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.vao.VAO;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PostProcessingRegistrationEvent extends Event {
    private final List<Triple<String, String[], TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass>>> postProcessingEntries = new ArrayList<>();

    public void register(@NonNull String subpassName, @NonNull String @NonNull [] shaderProgram, @NonNull TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass> subpassCtor) {
        postProcessingEntries.add(Triple.of(subpassName, shaderProgram, subpassCtor));
    }
}
