package com.cleanroommc.kirino.engine.world.context;

import com.cleanroommc.kirino.engine.render.core.ShaderIntrospection;
import com.cleanroommc.kirino.engine.world.type.Headless;
import org.jspecify.annotations.NonNull;

public interface AnalyticalWorldView extends WorldContext<Headless> {
    @NonNull ShaderIntrospection shaderIntrospection();
}
