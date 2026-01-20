package com.cleanroommc.kirino.engine.analysis.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.RenderExtensions;
import com.cleanroommc.kirino.engine.render.core.RenderStructure;
import com.cleanroommc.kirino.engine.render.core.ShaderIntrospection;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AnalyticalWorldViewImpl implements AnalyticalWorldView {
    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final RenderExtensions extensions;
    private final EventBus eventBus;
    private final Logger logger;
    private final ShaderIntrospection shaderIntrospection;

    private final Map<FramePhase,
            Map<FramePhaseTiming, List<Consumer<WorldContext<Headless>>>>> callbacks =
            new Object2ObjectOpenHashMap<>();

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

    @NonNull
    @Override
    public ShaderIntrospection shaderIntrospection() {
        return shaderIntrospection;
    }

    @Override
    public void run(@NonNull FramePhase phase) {
        Map<FramePhaseTiming, List<Consumer<WorldContext<Headless>>>> map = callbacks.get(phase);
        if (map != null) {
            List<Consumer<WorldContext<Headless>>> list = map.get(FramePhaseTiming.BEFORE);
            if (list != null) {
                for (Consumer<WorldContext<Headless>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }

        if (map != null) {
            List<Consumer<WorldContext<Headless>>> list = map.get(FramePhaseTiming.AFTER);
            if (list != null) {
                for (Consumer<WorldContext<Headless>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }
    }

    @Override
    public void on(@NonNull FramePhase phase, @NonNull FramePhaseTiming timing, @NonNull Consumer<WorldContext<Headless>> consumer) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkNotNull(timing);
        Preconditions.checkNotNull(consumer);

        Map<FramePhaseTiming, List<Consumer<WorldContext<Headless>>>> map = callbacks.computeIfAbsent(phase, k -> new Object2ObjectOpenHashMap<>());
        List<Consumer<WorldContext<Headless>>> list = map.computeIfAbsent(timing, k -> new ArrayList<>());
        list.add(consumer);
    }
}
