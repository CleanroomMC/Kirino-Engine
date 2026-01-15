package com.cleanroommc.kirino.engine.analysis.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.render.RenderStructure;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;

import java.util.HashMap;
import java.util.Map;

public class AnalyticalWorldViewImpl implements AnalyticalWorldView {
    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final Map<FramePhase, Runnable> callbacks = new HashMap<>();

    public AnalyticalWorldViewImpl(CleanECSRuntime ecs, RenderStructure render) {
        this.ecs = ecs;
        this.render = render;
    }

    @Override
    public CleanECSRuntime ecs() {
        return ecs;
    }

    @Override
    public RenderStructure render() {
        return render;
    }

    @Override
    public void run(FramePhase phase) {
        Runnable runnable = callbacks.get(phase);
        if (runnable != null) {
            runnable.run();
        }

    }

    @Override
    public void on(FramePhase phase, Runnable runnable) {
        callbacks.put(phase, runnable);
    }
}
