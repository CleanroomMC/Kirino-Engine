package com.cleanroommc.kirino.engine.world;

import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.world.type.WorldKind;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public interface ModuleInstaller<W extends WorldKind> {
    void install(@NonNull WorldContext<W> context, @NonNull ResourceLayout layout);

    @NonNull
    default AnalyticalWorldView castHeadless(@NonNull WorldContext<Headless> context) {
        Preconditions.checkNotNull(context);

        if (!(context instanceof AnalyticalWorldView analyticalWorldView)) {
            throw new RuntimeException("WorldContext is not an instance of AnalyticalWorldViewImpl.");
        }

        return analyticalWorldView;
    }

    @NonNull
    default GraphicsWorldView castGraphics(@NonNull WorldContext<Graphics> context) {
        Preconditions.checkNotNull(context);

        if (!(context instanceof GraphicsWorldView graphicsWorldView)) {
            throw new RuntimeException("WorldContext is not an instance of GraphicsWorldView.");
        }

        return graphicsWorldView;
    }
}
