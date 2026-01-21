package com.cleanroommc.kirino.engine.render.platform.minecraft.patch;

import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinecraftCulling {
    public final Set<Entity> entitiesInView = new HashSet<>();
    public final List<TileEntity> tileEntitiesInView = new ArrayList<>();

    // todo: integrate ECS-world and jobs
    public void collectEntitiesInView(Entity renderViewEntity, ICamera camera, ChunkProviderClient chunkProvider, float partialTicks) {
        entitiesInView.clear();
        tileEntitiesInView.clear();

        List<Chunk> chunksInView = new ArrayList<>();

        chunksInView.addAll(MethodHolder.getLoadedChunks(chunkProvider).values());

        for (Chunk chunk : chunksInView) {
            tileEntitiesInView.addAll(chunk.getTileEntityMap().values());
            for (ClassInheritanceMultiMap<Entity> entitiesInCubicChunk: chunk.getEntityLists()) {
                entitiesInView.addAll(entitiesInCubicChunk);
            }
        }
    }

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadedChunks", "field_73236_b", Long2ObjectMap.class));

            Preconditions.checkNotNull(DELEGATE.loadedChunksGetter);
        }

        @SuppressWarnings("unchecked")
        static Long2ObjectMap<Chunk> getLoadedChunks(ChunkProviderClient chunkProvider) {
            Long2ObjectMap<Chunk> result;
            try {
                result = (Long2ObjectMap<Chunk>) DELEGATE.loadedChunksGetter.invokeExact(chunkProvider);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(MethodHandle loadedChunksGetter) {
        }
    }
}
