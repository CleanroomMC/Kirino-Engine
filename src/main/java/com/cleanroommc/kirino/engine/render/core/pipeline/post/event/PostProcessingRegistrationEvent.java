package com.cleanroommc.kirino.engine.render.core.pipeline.post.event;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingEntry;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PostProcessingRegistrationEvent extends Event {

    private final List<PostProcessingEntry> postProcessingEntries = new ArrayList<>();

    public void register(
            @NonNull String subpassName,
            @NonNull String @NonNull [] shaders,
            PostProcessingEntry.@NonNull PassConstructor ctor) {

        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(shaders);
        for (String shader : shaders) {
            Preconditions.checkNotNull(shader);
        }
        Preconditions.checkNotNull(ctor);

        PostProcessingEntry entry = new PostProcessingEntry(subpassName, shaders, ctor);
        postProcessingEntries.add(entry);
    }
}
