package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuWriter;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class MeshletBufferWriteJob implements IParallelJob {
    @JobExternalDataQuery
    MeshletGpuWriter meshletGpuWriter;

    ByteBuffer byteBuffer = null;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        if (byteBuffer == null) {
            byteBuffer = meshletGpuWriter.getNewByteBufferView(); // one view per thread
        }
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
