package com.cleanroommc.kirino.engine.render.usage;

import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class MinecraftIntegration {
    public final ResourceSlot<MinecraftCulling> cullingPatch;
    public final ResourceSlot<MinecraftEntityRendering> entityRenderingPatch;
    public final ResourceSlot<MinecraftTESRRendering> tesrRenderingPatch;

    public MinecraftIntegration(
            ResourceSlot<MinecraftCulling> cullingPatch,
            ResourceSlot<MinecraftEntityRendering> entityRenderingPatch,
            ResourceSlot<MinecraftTESRRendering> tesrRenderingPatch) {

        this.cullingPatch = cullingPatch;
        this.entityRenderingPatch = entityRenderingPatch;
        this.tesrRenderingPatch = tesrRenderingPatch;
    }
}
