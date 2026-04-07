package com.cleanroommc.kirino.engine.render.platform.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.ChunkPosKey;
import com.cleanroommc.kirino.engine.render.platform.scene.callback.CallbackDrivenChunkDelta;
import org.jspecify.annotations.NonNull;


public class MeshletDestroyJob implements ParallelJob {
    @JobExternalDataQuery
    CallbackDrivenChunkDelta chunkDelta;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    PrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    PrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    PrimitiveArray chunkPosZArray;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        int chunkPosX = chunkPosXArray.getInt(index);
        int chunkPosY = chunkPosYArray.getInt(index);
        int chunkPosZ = chunkPosZArray.getInt(index);
        for (ChunkPosKey chunkPos : chunkDelta.chunksDestroyedLastFrame) {
            if (chunkPosX == chunkPos.x() && chunkPosY == chunkPos.y() && chunkPosZ == chunkPos.z()) {
                entityManager.destroyEntity(entityID);
                break;
            }
        }
    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(MeshletComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return chunkDelta.chunksDestroyedLastFrame.size();
    }
}
