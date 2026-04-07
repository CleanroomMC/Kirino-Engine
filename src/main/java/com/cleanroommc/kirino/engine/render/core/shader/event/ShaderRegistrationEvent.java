package com.cleanroommc.kirino.engine.render.core.shader.event;

import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShaderRegistrationEvent extends Event {
    private final Map<ResourceLocation, Optional<ShaderCompileOptions>> rawShaders = new HashMap<>();

    public void register(@NonNull ResourceLocation resourceLocation) {
        rawShaders.put(resourceLocation, Optional.empty());
    }

    public void register(@NonNull ResourceLocation resourceLocation, int debugFlags) {
        rawShaders.put(resourceLocation, Optional.of(new ShaderCompileOptions(debugFlags)));
    }
}
