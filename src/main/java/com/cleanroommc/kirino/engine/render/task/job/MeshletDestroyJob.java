package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MeshletDestroyJob implements IParallelJob {
    @JobExternalDataQuery
    List<MinecraftScene.ChunkPosKey> chunksDestroyedLastFrame;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    IPrimitiveArray chunkPosZArray;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        int chunkPosX = chunkPosXArray.getInt(index);
        int chunkPosY = chunkPosYArray.getInt(index);
        int chunkPosZ = chunkPosZArray.getInt(index);
        for (MinecraftScene.ChunkPosKey chunkPos : chunksDestroyedLastFrame) {
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
        return chunksDestroyedLastFrame.size();
    }
}
