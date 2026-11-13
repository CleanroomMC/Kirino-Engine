package com.cleanroommc.kirino.engine.render.usage;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SceneMenuTickerTest {
    @Test
    void renderTick_onlyStartsMenuUpdatesAndKeepsPollingUntilWorldReturns() {
        AtomicBoolean noWorld = new AtomicBoolean();
        AtomicInteger updates = new AtomicInteger();
        SceneMenuTicker ticker = new SceneMenuTicker(noWorld::get, updates::incrementAndGet);
        ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.START, 0));
        assertEquals(0, updates.get());
        noWorld.set(true);
        ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.END, 0));
        assertEquals(0, updates.get());
        ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.START, 0));
        ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.START, 0));
        assertEquals(2, updates.get());
        noWorld.set(false);
        ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.START, 0));
        assertEquals(2, updates.get());
    }

    @Test
    void menuUpdate_failurePropagatesToTheRenderTickCaller() {
        IllegalStateException failure = new IllegalStateException("retirement failed");
        SceneMenuTicker ticker = new SceneMenuTicker(() -> true, () -> { throw failure; });
        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> ticker.onRenderTick(new TickEvent.RenderTickEvent(TickEvent.Phase.START, 0))));
    }
}
