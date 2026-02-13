package com.cleanroommc.kirino.engine.render.platform.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.ChunkComponent;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public class ChunkPrioritizationJob implements ParallelJob {
    @JobExternalDataQuery
    AtomicInteger maxLodCounter;

    @JobExternalDataQuery
    Camera camera;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    PrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosY"})
    PrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    PrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"lod"})
    PrimitiveArray lodArray;

    Vector3f worldOffset = null;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        float chunkWorldX = (float) chunkPosXArray.getInt(index) * 16 + 8f;
        float chunkWorldY = (float) chunkPosYArray.getInt(index) * 16 + 8f;
        float chunkWorldZ = (float) chunkPosZArray.getInt(index) * 16 + 8f;

        if (worldOffset == null) {
            worldOffset = camera.getWorldOffset();
        }

        float dis = (float) Math.sqrt((chunkWorldX - worldOffset.x) * (chunkWorldX - worldOffset.x) +
                (chunkWorldY - worldOffset.y) * (chunkWorldY - worldOffset.y) +
                (chunkWorldZ - worldOffset.z) * (chunkWorldZ - worldOffset.z));

        // lod fallout distance = 16
        int lod = (int) (dis / 16f);

        lodArray.setInt(index, lod);

        maxLodCounter.updateAndGet(prev -> Math.max(prev, lod));
    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return 3;
    }
}
