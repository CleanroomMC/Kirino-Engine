package com.cleanroommc.kirino.engine.analysis.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.render.core.RenderExtensions;
import com.cleanroommc.kirino.engine.render.core.RenderStructure;
import com.cleanroommc.kirino.engine.render.core.ShaderIntrospection;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Headless;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AnalyticalWorldViewImpl implements AnalyticalWorldView {
    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final RenderExtensions extensions;
    private final EventBus eventBus;
    private final Logger logger;

    public final ShaderIntrospection shaderIntrospection;

    private final Map<FramePhase, Consumer<WorldContext<Headless>>> callbacks = new HashMap<>();

    public AnalyticalWorldViewImpl(
            CleanECSRuntime ecs,
            RenderStructure render,
            RenderExtensions extensions,
            EventBus eventBus,
            Logger logger,
            ShaderIntrospection shaderIntrospection) {
        this.ecs = ecs;
        this.render = render;
        this.extensions = extensions;
        this.eventBus = eventBus;
        this.logger = logger;

        this.shaderIntrospection = shaderIntrospection;
    }

    @NonNull
    @Override
    public CleanECSRuntime ecs() {
        return ecs;
    }

    @NonNull
    @Override
    public RenderStructure rs() {
        return render;
    }

    @NonNull
    @Override
    public RenderExtensions ext() {
        return extensions;
    }

    @NonNull
    @Override
    public Logger logger() {
        return logger;
    }

    @NonNull
    @Override
    public EventBus bus() {
        return eventBus;
    }

    @Override
    public void run(@NonNull FramePhase phase) {
        Consumer<WorldContext<Headless>> consumer = callbacks.get(phase);
        if (consumer != null) {
            consumer.accept(this);
        }

    }

    @Override
    public void on(@NonNull FramePhase phase, @NonNull Consumer<WorldContext<Headless>> consumer) {
        callbacks.put(phase, consumer);
    }
}
