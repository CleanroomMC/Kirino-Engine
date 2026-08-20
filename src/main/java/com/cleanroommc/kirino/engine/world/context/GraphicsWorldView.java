package com.cleanroommc.kirino.engine.world.context;

import com.cleanroommc.kirino.engine.render.core.BuiltinShaderBundle;
import com.cleanroommc.kirino.engine.render.core.GraphicsRuntimeBundle;
import com.cleanroommc.kirino.engine.render.core.RuntimeShaderBundle;
import com.cleanroommc.kirino.engine.render.core.ShaderIntrospection;
import com.cleanroommc.kirino.engine.render.usage.McIntegrationBundle;
import com.cleanroommc.kirino.engine.render.usage.McSceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import org.jspecify.annotations.NonNull;

public interface GraphicsWorldView extends WorldContext<Graphics> {
    @NonNull ResourceStorage storage();
    @NonNull ShaderIntrospection shaderi();
    @NonNull BuiltinShaderBundle shaderbb();
    @NonNull RuntimeShaderBundle shaderrb();
    @NonNull GraphicsRuntimeBundle graphicsb();
    @NonNull McIntegrationBundle mcib();
    @NonNull McSceneViewState mcscene();
}
