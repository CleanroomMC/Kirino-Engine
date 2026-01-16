package com.cleanroommc.kirino.engine.render.platform.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MeshletDebugJob implements IParallelJob {
    @JobExternalDataQuery
    ResourceStorage storage;

    @JobExternalDataQuery
    ResourceSlot<GizmosManager> gizmosManager;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"blockCount"})
    IPrimitiveArray blockCountArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "positionAndFaceMask"})
    IPrimitiveArray block0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "positionAndFaceMask"})
    IPrimitiveArray block1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "positionAndFaceMask"})
    IPrimitiveArray block2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "positionAndFaceMask"})
    IPrimitiveArray block3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "positionAndFaceMask"})
    IPrimitiveArray block4Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "positionAndFaceMask"})
    IPrimitiveArray block5Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "positionAndFaceMask"})
    IPrimitiveArray block6Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "positionAndFaceMask"})
    IPrimitiveArray block7Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "positionAndFaceMask"})
    IPrimitiveArray block8Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "positionAndFaceMask"})
    IPrimitiveArray block9Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "positionAndFaceMask"})
    IPrimitiveArray block10Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "positionAndFaceMask"})
    IPrimitiveArray block11Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "positionAndFaceMask"})
    IPrimitiveArray block12Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "positionAndFaceMask"})
    IPrimitiveArray block13Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "positionAndFaceMask"})
    IPrimitiveArray block14Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "positionAndFaceMask"})
    IPrimitiveArray block15Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "positionAndFaceMask"})
    IPrimitiveArray block16Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "positionAndFaceMask"})
    IPrimitiveArray block17Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "positionAndFaceMask"})
    IPrimitiveArray block18Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "positionAndFaceMask"})
    IPrimitiveArray block19Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "positionAndFaceMask"})
    IPrimitiveArray block20Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "positionAndFaceMask"})
    IPrimitiveArray block21Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "positionAndFaceMask"})
    IPrimitiveArray block22Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "positionAndFaceMask"})
    IPrimitiveArray block23Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "positionAndFaceMask"})
    IPrimitiveArray block24Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "positionAndFaceMask"})
    IPrimitiveArray block25Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "positionAndFaceMask"})
    IPrimitiveArray block26Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "positionAndFaceMask"})
    IPrimitiveArray block27Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "positionAndFaceMask"})
    IPrimitiveArray block28Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "positionAndFaceMask"})
    IPrimitiveArray block29Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "positionAndFaceMask"})
    IPrimitiveArray block30Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "positionAndFaceMask"})
    IPrimitiveArray block31Array;

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

        storage.get(gizmosManager).addMeshlet(chunkPosX * 16, chunkPosY * 16, chunkPosZ * 16, blocks);
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
