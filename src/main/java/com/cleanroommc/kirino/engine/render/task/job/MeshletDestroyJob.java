package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import org.jspecify.annotations.NonNull;

public class MeshletDestroyJob implements IParallelJob {
    @JobExternalDataQuery
    public int targetChunkPosX;

    @JobExternalDataQuery
    public int targetChunkPosY;

    @JobExternalDataQuery
    public int targetChunkPosZ;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    public IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        if (chunkPosXArray.getInt(index) == targetChunkPosX &&
                chunkPosYArray.getInt(index) == targetChunkPosY &&
                chunkPosZArray.getInt(index) == targetChunkPosZ) {
            entityManager.destroyEntity(entityID);
        }
    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(MeshletComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return 1;
    }
}
