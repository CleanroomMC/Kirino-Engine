package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.debug.data.impl.RenderStatsFrame;

/**
 * This helper class is made possible by {@link KirinoCore#DEBUG_SERVICE}, and
 * the purpose is to make {@link KirinoCore#DEBUG_SERVICE} calls shorter and simpler.
 * You are free to access {@link KirinoCore#DEBUG_SERVICE} directly as an alternative route.
 */
public final class KirinoDebug {
    private KirinoDebug() {
    }

    @SuppressWarnings("DataFlowIssue")
    public static void resetDrawCalls() {
        var renderStatsFrame = KirinoCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        if (renderStatsFrame.fetch() != null) {
            renderStatsFrame.fetch().setDrawCalls(0);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static void incrementDrawCalls() {
        var renderStatsFrame = KirinoCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        if (renderStatsFrame.fetch() != null) {
            renderStatsFrame.fetch().incrementDrawCall();
        }
    }
}
