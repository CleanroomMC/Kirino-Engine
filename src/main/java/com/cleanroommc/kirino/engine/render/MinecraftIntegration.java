package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftTESRRendering;

public class MinecraftIntegration {
    public final MinecraftCulling cullingPatch;
    public final MinecraftEntityRendering entityRenderingPatch;
    public final MinecraftTESRRendering tesrRenderingPatch;

    public MinecraftIntegration(
            MinecraftCulling cullingPatch,
            MinecraftEntityRendering entityRenderingPatch,
            MinecraftTESRRendering tesrRenderingPatch) {

        this.cullingPatch = cullingPatch;
        this.entityRenderingPatch = entityRenderingPatch;
        this.tesrRenderingPatch = tesrRenderingPatch;
    }
}
