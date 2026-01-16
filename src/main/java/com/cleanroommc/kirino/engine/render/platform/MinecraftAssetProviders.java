package com.cleanroommc.kirino.engine.render.platform;

import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class MinecraftAssetProviders {
    public final ResourceSlot<BlockMeshGenerator> blockMeshGenerator;

    public MinecraftAssetProviders(ResourceSlot<BlockMeshGenerator> blockMeshGenerator) {
        this.blockMeshGenerator = blockMeshGenerator;
    }
}
