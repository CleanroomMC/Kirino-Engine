package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MeshletDebugJob implements IParallelJob {
    @JobExternalDataQuery
    public GizmosManager gizmosManager;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"blockCount"})
    public IPrimitiveArray blockCountArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    public IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "positionAndFaceMask"})
    public IPrimitiveArray block0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "positionAndFaceMask"})
    public IPrimitiveArray block1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "positionAndFaceMask"})
    public IPrimitiveArray block2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "positionAndFaceMask"})
    public IPrimitiveArray block3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "positionAndFaceMask"})
    public IPrimitiveArray block4Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "positionAndFaceMask"})
    public IPrimitiveArray block5Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "positionAndFaceMask"})
    public IPrimitiveArray block6Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "positionAndFaceMask"})
    public IPrimitiveArray block7Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "positionAndFaceMask"})
    public IPrimitiveArray block8Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "positionAndFaceMask"})
    public IPrimitiveArray block9Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "positionAndFaceMask"})
    public IPrimitiveArray block10Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "positionAndFaceMask"})
    public IPrimitiveArray block11Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "positionAndFaceMask"})
    public IPrimitiveArray block12Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "positionAndFaceMask"})
    public IPrimitiveArray block13Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "positionAndFaceMask"})
    public IPrimitiveArray block14Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "positionAndFaceMask"})
    public IPrimitiveArray block15Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "positionAndFaceMask"})
    public IPrimitiveArray block16Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "positionAndFaceMask"})
    public IPrimitiveArray block17Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "positionAndFaceMask"})
    public IPrimitiveArray block18Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "positionAndFaceMask"})
    public IPrimitiveArray block19Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "positionAndFaceMask"})
    public IPrimitiveArray block20Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "positionAndFaceMask"})
    public IPrimitiveArray block21Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "positionAndFaceMask"})
    public IPrimitiveArray block22Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "positionAndFaceMask"})
    public IPrimitiveArray block23Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "positionAndFaceMask"})
    public IPrimitiveArray block24Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "positionAndFaceMask"})
    public IPrimitiveArray block25Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "positionAndFaceMask"})
    public IPrimitiveArray block26Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "positionAndFaceMask"})
    public IPrimitiveArray block27Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "positionAndFaceMask"})
    public IPrimitiveArray block28Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "positionAndFaceMask"})
    public IPrimitiveArray block29Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "positionAndFaceMask"})
    public IPrimitiveArray block30Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "positionAndFaceMask"})
    public IPrimitiveArray block31Array;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        int blockCount = blockCountArray.getInt(index);
        List<Integer> blocks = new ArrayList<>();

        int i = 0;
        if (i < blockCount) {
            blocks.add(block0Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block1Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block2Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block3Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block4Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block5Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block6Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block7Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block8Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block9Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block10Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block11Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block12Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block13Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block14Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block15Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block16Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block17Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block18Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block19Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block20Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block21Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block22Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block23Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block24Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block25Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block26Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block27Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block28Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block29Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block30Array.getInt(index));
            i++;
        }
        if (i < blockCount) {
            blocks.add(block31Array.getInt(index));
            i++;
        }

        int chunkPosX = chunkPosXArray.getInt(index);
        int chunkPosY = chunkPosYArray.getInt(index);
        int chunkPosZ = chunkPosZArray.getInt(index);

        gizmosManager.addMeshlet(chunkPosX * 16, chunkPosY * 16, chunkPosZ * 16, blocks);
    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(MeshletComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return 32;
    }
}
