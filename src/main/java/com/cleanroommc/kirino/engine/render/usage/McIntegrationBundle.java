package com.cleanroommc.kirino.engine.render.usage;

import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.render.usage.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.McIntegrationBundleInit
 */
public final class McIntegrationBundle {

    public final ResourceSlot<MinecraftCulling> cullingPatch;
    public final ResourceSlot<MinecraftEntityRendering> entityRenderingPatch;
    public final ResourceSlot<MinecraftTESRRendering> tesrRenderingPatch;
    public final ResourceSlot<BlockMeshGenerator> blockMeshGenerator;

    public McIntegrationBundle(@NonNull ResourceLayout resourceLayout) {
        Preconditions.checkNotNull(resourceLayout);

        cullingPatch = resourceLayout.slot(MinecraftCulling.class, "culling_patch");
        entityRenderingPatch = resourceLayout.slot(MinecraftEntityRendering.class, "entity_rendering_patch");
        tesrRenderingPatch = resourceLayout.slot(MinecraftTESRRendering.class, "tesr_rendering_patch");
        blockMeshGenerator = resourceLayout.slot(BlockMeshGenerator.class, "block_mesh_generator");
    }
}
