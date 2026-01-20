package com.cleanroommc.kirino.engine.world.context;

import com.cleanroommc.kirino.engine.render.core.BootstrapResources;
import com.cleanroommc.kirino.engine.render.core.GraphicsRuntimeServices;
import com.cleanroommc.kirino.engine.render.core.ShaderIntrospection;
import com.cleanroommc.kirino.engine.render.platform.MinecraftAssetProviders;
import com.cleanroommc.kirino.engine.render.platform.MinecraftIntegration;
import com.cleanroommc.kirino.engine.render.platform.SceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import org.jspecify.annotations.NonNull;

public interface GraphicsWorldView extends WorldContext<Graphics> {
    @NonNull ResourceStorage storage();
    @NonNull ShaderIntrospection shaderIntrospection();
    @NonNull BootstrapResources bootstrapResources();
    @NonNull GraphicsRuntimeServices graphicsRuntimeServices();
    @NonNull MinecraftIntegration minecraftIntegration();
    @NonNull MinecraftAssetProviders minecraftAssetProviders();
    @NonNull SceneViewState sceneViewState();
}
