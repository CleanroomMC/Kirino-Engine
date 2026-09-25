package com.cleanroommc.kirino.config;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingSchedule;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import org.jspecify.annotations.NonNull;

@Config(modid = "kirino_engine")
public class KirinoConfig {

    @Config.RequiresMcRestart
    public static final NeedsRestart NEEDS_RESTART = new NeedsRestart();

    public static boolean isEnabled() {
        return NEEDS_RESTART.enable && NEEDS_RESTART.enableRenderDelegate;
    }

    public static class NeedsRestart {

        public boolean enable = isRunKirinoClient();
        public boolean enableRenderDelegate = enable;
        public boolean enableHDR = enable;
        public boolean enablePostProcessing = false;
        public boolean enableKhrDebug = false;
        public boolean enableShaderDebug = false;

        @NonNull
        @Config.Ignore
        public PostProcessingSchedule postProcessingSchedule = PostProcessingSchedule.EMPTY;

        public int targetWorkloadPerThread = 5000;

        public boolean compileToMdiCommands = true;
        public int maxMultiDrawIndirectUnitCount = 5000;

        public int highLevelDrawCommandPoolSize = 2000;
        public int lowLevelDrawCommandPoolSize = 2000;

        public int worldInitFrames = 5;
        public int meshletInitFrames = 5;
        public float chunkUpdateDisplacement = 8f;

        public int foregroundRenderDistance = 8;

        private static boolean isRunKirinoClient() {
            return Boolean.getBoolean("crl.dev.kirino");
        }

    }

    static {
        ConfigManager.register(KirinoConfig.class);
    }

}
