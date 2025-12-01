package com.cleanroommc.kirino;

public final class KirinoConfigHub {
    KirinoConfigHub() {
    }

    public boolean enable = true;
    public boolean enableRenderDelegate = true;
    public boolean enableHDR = true;
    public boolean enablePostProcessing = true;

    public int targetWorkloadPerThread = 5000;

    public int maxMultiDrawIndirectUnitCount = 5000;

    public int highLevelDrawCommandPoolSize = 2000;
    public int lowLevelDrawCommandPoolSize = 2000;

    public int worldInitFrames = 5;
    public float chunkUpdateDisplacement = 8f;
}
