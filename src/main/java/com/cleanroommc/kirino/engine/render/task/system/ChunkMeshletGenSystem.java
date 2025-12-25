package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;
import com.cleanroommc.kirino.utils.Reference;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ChunkMeshletGenSystem extends CleanSystem {
    private final Map<String, Object> externalData;

    private final Executor executor;

    public ChunkMeshletGenSystem(GizmosManager gizmosManager, Reference<BlockMeshGenerator> blockMeshGenerator, MeshletGpuRegistry meshletGpuRegistry, MinecraftScene.MeshletDestroyCallback meshletDestroyCallback, Executor executor) {
        externalData = new HashMap<>();
        externalData.put("gizmosManager", gizmosManager);
        externalData.put("blockMeshGenerator", blockMeshGenerator);
        externalData.put("tempBuffers", new ConcurrentHashMap<Integer, BufferBuilder>());
        externalData.put("meshletGpuRegistry", meshletGpuRegistry);
        externalData.put("meshletDestroyCallback", meshletDestroyCallback);
        this.executor = executor;
    }

    private int lod = 0;
    private ChunkProviderClient chunkProvider = null;
    private WorldClient world = null;

    public void setLod(int lod) {
        this.lod = lod;
    }

    public void setChunkProvider(ChunkProviderClient chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    public void setWorld(WorldClient world) {
        this.world = world;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        externalData.put("lod", lod);
        externalData.put("chunkProvider", chunkProvider);
        externalData.put("world", world);
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(
                entityManager,
                ChunkMeshletGenJob.class,
                externalData,
                executor);
        execution.updateExecutions(handle);
    }
}
