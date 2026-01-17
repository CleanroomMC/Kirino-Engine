package com.cleanroommc.kirino.config;

public final class KirinoConfigHub {
    KirinoConfigHub() {
    }

    public static class RequiresRestart {
        private RequiresRestart() {
        }

        public boolean enable = true;
        public boolean enableRenderDelegate = true;
        public boolean enableHDR = true;
        public boolean enablePostProcessing = false;

        public int targetWorkloadPerThread = 5000;

        public int maxMultiDrawIndirectUnitCount = 5000;

        public int highLevelDrawCommandPoolSize = 2000;
        public int lowLevelDrawCommandPoolSize = 2000;

        public int worldInitFrames = 5;
        public float chunkUpdateDisplacement = 8f;
    }

    public static class Runtime {
        private Runtime() {
        }
    }

    private final RequiresRestart requiresRestart = new RequiresRestart();
    public final Runtime runtime = new Runtime();

    public boolean isEnable() {
        return requiresRestart.enable;
    }

    public boolean isEnableRenderDelegate() {
        return requiresRestart.enableRenderDelegate;
    }

    public boolean isEnableHDR() {
        return requiresRestart.enableHDR;
    }

    public boolean isEnablePostProcessing() {
        return requiresRestart.enablePostProcessing;
    }

    public int getTargetWorkloadPerThread() {
        return requiresRestart.targetWorkloadPerThread;
    }

    public int getMaxMultiDrawIndirectUnitCount() {
        return requiresRestart.maxMultiDrawIndirectUnitCount;
    }

    public int getHighLevelDrawCommandPoolSize() {
        return requiresRestart.highLevelDrawCommandPoolSize;
    }

    public int getLowLevelDrawCommandPoolSize() {
        return requiresRestart.lowLevelDrawCommandPoolSize;
    }

    public int getWorldInitFrames() {
        return requiresRestart.worldInitFrames;
    }

    public float getChunkUpdateDisplacement() {
        return requiresRestart.chunkUpdateDisplacement;
    }
}
