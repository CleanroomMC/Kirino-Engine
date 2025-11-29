package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class ChunkMeshletGenSystem extends CleanSystem {
    private final Map<String, Object> externalData;

    public ChunkMeshletGenSystem(ChunkProviderClient chunkClient, GizmosManager gizmosManager) {
        externalData = new HashMap<>();
        externalData.put("chunkProvider", chunkClient);
        externalData.put("gizmosManager", gizmosManager);
    }

    private int lod = 0;

    public void setLod(int lod) {
        this.lod = lod;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        externalData.put("lod", lod);
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(entityManager, ChunkMeshletGenJob.class, externalData, ForkJoinPool.commonPool());
        if (handle.async()) {
            handle.future().join();
        }
        execution.updateExecutions(handle);
    }
}
