package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.core.debug.data.impl.FpsHistory;
import com.cleanroommc.kirino.engine.render.core.debug.data.impl.RenderStatsFrame;

/**
 * This helper class is made possible by {@link KirinoClientCore#DEBUG_SERVICE}, and
 * the purpose is to make {@link KirinoClientCore#DEBUG_SERVICE} calls shorter and simpler.
 * You are free to access {@link KirinoClientCore#DEBUG_SERVICE} directly as an alternative route.
 */
public final class KirinoDebug {
    private KirinoDebug() {
    }

    private static DebugDataHandle<RenderStatsFrame> renderStatsFrame = null;

    private static DebugDataHandle<RenderStatsFrame> getRenderStatsFrame() {
        if (renderStatsFrame == null) {
            renderStatsFrame = KirinoClientCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        }
        return renderStatsFrame;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void resetDrawCalls() {
        if (getRenderStatsFrame().fetch() != null) {
            getRenderStatsFrame().fetch().setDrawCalls(0);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void incrementDrawCalls() {
        if (getRenderStatsFrame().fetch() != null) {
            getRenderStatsFrame().fetch().incrementDrawCalls();
        }
    }

    private static DebugDataHandle<FpsHistory> fpsHistory = null;

    private static DebugDataHandle<FpsHistory> getFpsHistory() {
        if (fpsHistory == null) {
            fpsHistory = KirinoClientCore.DEBUG_SERVICE.get(FpsHistory.class);
        }
        return fpsHistory;
    }

    @SuppressWarnings("DataFlowIssue")
    public static void recordFps(int fps) {
        if (getFpsHistory().fetch() != null) {
            getFpsHistory().fetch().record(fps);
        }
    }
}
