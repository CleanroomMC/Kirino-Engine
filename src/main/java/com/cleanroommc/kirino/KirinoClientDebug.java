package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.core.debug.data.builtin.FpsHistory;
import com.cleanroommc.kirino.engine.render.core.debug.data.builtin.RenderStatsFrame;
import com.cleanroommc.kirino.engine.render.platform.debug.data.impl.MeshletGpuTimeline;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * This helper class is made possible by {@link KirinoClientCore#DEBUG_SERVICE}, and
 * the purpose is to make {@link KirinoClientCore#DEBUG_SERVICE} calls shorter and simpler.
 * You are free to access {@link KirinoClientCore#DEBUG_SERVICE} directly as an alternative route.
 *
 * <p>Note: This class is client side only! You can access the class anywhere but only
 * access the methods on client side.</p>
 *
 * <p>Note: Must access the methods after {@link KirinoClientCore#init()} where
 * debug services are registered. Accessing methods too early will cause NPE.</p>
 */
public final class KirinoClientDebug {
    private KirinoClientDebug() {
    }

    //<editor-fold desc="RenderStatsFrame">
    private static DebugDataHandle<RenderStatsFrame> renderStatsFrame = null;

    @NonNull
    private static DebugDataHandle<RenderStatsFrame> getRenderStatsFrame() {
        if (renderStatsFrame == null) {
            renderStatsFrame = KirinoClientCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
            Preconditions.checkNotNull(renderStatsFrame);
        }
        return renderStatsFrame;
    }

    /**
     * This method belongs to {@link RenderStatsFrame}.
     */
    public static void RenderStatsFrame$resetDrawCalls() {
        var temp = getRenderStatsFrame().fetch();
        if (temp != null) {
            temp.setDrawCalls(0);
        }
    }

    /**
     * This method belongs to {@link RenderStatsFrame}.
     */
    public static void RenderStatsFrame$incrementDrawCalls() {
        var temp = getRenderStatsFrame().fetch();
        if (temp != null) {
            temp.incrementDrawCalls();
        }
    }
    //</editor-fold>

    //<editor-fold desc="FpsHistory">
    private static DebugDataHandle<FpsHistory> fpsHistory = null;

    @NonNull
    private static DebugDataHandle<FpsHistory> getFpsHistory() {
        if (fpsHistory == null) {
            fpsHistory = KirinoClientCore.DEBUG_SERVICE.get(FpsHistory.class);
            Preconditions.checkNotNull(fpsHistory);
        }
        return fpsHistory;
    }

    /**
     * This method belongs to {@link FpsHistory}.
     */
    public static void FpsHistory$recordFps(int fps) {
        var temp = getFpsHistory().fetch();
        if (temp != null) {
            temp.record(fps);
        }
    }
    //</editor-fold>

    //<editor-fold desc="MeshletGpuTimeline">
    private static DebugDataHandle<MeshletGpuTimeline> meshletGpuTimeline = null;

    @NonNull
    private static DebugDataHandle<MeshletGpuTimeline> getMeshletGpuTimeline() {
        if (meshletGpuTimeline == null) {
            meshletGpuTimeline = KirinoClientCore.DEBUG_SERVICE.get(MeshletGpuTimeline.class);
            Preconditions.checkNotNull(meshletGpuTimeline);
        }
        return meshletGpuTimeline;
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$loadInNewWorld() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.loadInNewWorld();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$worldTick() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.worldTick();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$hasMeshletUpdate() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.hasMeshletUpdate();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State state) {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.pushFrameState(state);
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$beginWriting() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.beginWriting();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$finishWriting() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.finishWriting();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$beginComputing() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.beginComputing();
        }
    }

    /**
     * This method belongs to {@link MeshletGpuTimeline}.
     */
    public static void MeshletGpuTimeline$finishComputing() {
        var temp = getMeshletGpuTimeline().fetch();
        if (temp != null) {
            temp.finishComputing();
        }
    }
    //</editor-fold>
}
