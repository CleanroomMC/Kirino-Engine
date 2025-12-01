package com.cleanroommc.kirino.engine.render.ecs.component;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponent;

@CleanComponent
public class ChunkComponent implements ICleanComponent {
    /**
     * X-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosX;

    /**
     * Y-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosY;

    /**
     * Z-coordinate under chunk coordinate system. (* 16 = world coordinate)
     */
    public int chunkPosZ;

    /**
     * Whether the chunk is modified so meshlet-gen task has to run on this chunk.
     */
    public boolean isDirty = true;

    /**
     * The closer to the camera, smaller the number.
     * <p>Domain: [0, inf]</p>
     */
    public int lod = 0x7fffffff;
}
