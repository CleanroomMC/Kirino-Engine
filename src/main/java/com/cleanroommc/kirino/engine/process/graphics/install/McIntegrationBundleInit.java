package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.usage.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.render.usage.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import net.minecraft.client.Minecraft;

/**
 * @see com.cleanroommc.kirino.engine.render.usage.McIntegrationBundle
 */
public final class McIntegrationBundleInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        MinecraftCulling minecraftCulling = new MinecraftCulling();
        storage.put(context.mcib().cullingPatch, minecraftCulling);
        storage.put(context.mcib().entityRenderingPatch, new MinecraftEntityRendering(minecraftCulling));
        storage.put(context.mcib().tesrRenderingPatch, new MinecraftTESRRendering(minecraftCulling));
        storage.put(context.mcib().blockMeshGenerator, new BlockMeshGenerator(Minecraft.getMinecraft()));

        storage.sealResource(context.mcib().cullingPatch);
        storage.sealResource(context.mcib().entityRenderingPatch);
        storage.sealResource(context.mcib().tesrRenderingPatch);
        storage.sealResource(context.mcib().blockMeshGenerator);
    }
}
