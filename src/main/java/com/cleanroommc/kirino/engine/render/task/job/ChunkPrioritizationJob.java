package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.ecs.component.ChunkComponent;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

public class ChunkPrioritizationJob implements IParallelJob {
    @JobExternalDataQuery
    AtomicInteger maxLodCounter;

    @JobExternalDataQuery
    public ICamera camera;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosY"})
    public IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"lod"})
    public IPrimitiveArray lodArray;

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
