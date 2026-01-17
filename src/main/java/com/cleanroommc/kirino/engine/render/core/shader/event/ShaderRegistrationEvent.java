package com.cleanroommc.kirino.engine.render.core.shader.event;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ShaderRegistrationEvent extends Event {
    private final List<ResourceLocation> shaderResourceLocations = new ArrayList<>();

    public void register(@NonNull ResourceLocation resourceLocation) {
        shaderResourceLocations.add(resourceLocation);
    }
}
