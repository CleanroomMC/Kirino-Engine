package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import org.jspecify.annotations.NonNull;

public class MeshletBufferWriteJob implements IParallelJob {
    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {

    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(MeshletComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return 0;
    }
}
