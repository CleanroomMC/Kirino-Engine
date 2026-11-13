package com.cleanroommc.kirino.engine.render.usage;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.BooleanSupplier;

final class SceneMenuTicker {
    private final BooleanSupplier noWorld;
    private final Runnable update;

    SceneMenuTicker(BooleanSupplier noWorld, Runnable update) {
        this.noWorld = noWorld;
        this.update = update;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && noWorld.getAsBoolean()) {
            update.run();
        }
    }
}
