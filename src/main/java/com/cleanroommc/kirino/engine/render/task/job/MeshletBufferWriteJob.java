package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuWriter;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class MeshletBufferWriteJob implements IParallelJob {
    @JobExternalDataQuery
    MeshletGpuWriter meshletGpuWriter;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"meshletId"})
    IPrimitiveArray meshletIdArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosX"})
    IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosY"})
    IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"chunkPosZ"})
    IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"normal", "x"})
    IPrimitiveArray normalXArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"normal", "y"})
    IPrimitiveArray normalYArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"normal", "z"})
    IPrimitiveArray normalZArray;

    //<editor-fold desc="32 block entries">
    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "positionAndFaceMask"})
    IPrimitiveArray block0PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block0Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block0Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block0Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block0Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block0Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block0Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block0Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block0Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block0Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block0Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block0Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block0Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block0Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block0Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block0Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block0Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block0Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block0Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block0Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block0Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block0Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block0Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block0Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block0", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block0Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "positionAndFaceMask"})
    IPrimitiveArray block1PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block1Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block1Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block1Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block1Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block1Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block1Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block1Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block1Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block1Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block1Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block1Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block1Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block1Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block1Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block1Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block1Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block1Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block1Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block1Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block1Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block1Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block1Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block1Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block1", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block1Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "positionAndFaceMask"})
    IPrimitiveArray block2PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block2Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block2Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block2Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block2Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block2Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block2Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block2Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block2Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block2Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block2Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block2Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block2Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block2Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block2Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block2Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block2Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block2Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block2Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block2Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block2Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block2Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block2Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block2Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block2", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block2Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "positionAndFaceMask"})
    IPrimitiveArray block3PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block3Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block3Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block3Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block3Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block3Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block3Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block3Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block3Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block3Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block3Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block3Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block3Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block3Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block3Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block3Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block3Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block3Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block3Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block3Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block3Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block3Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block3Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block3Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block3", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block3Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "positionAndFaceMask"})
    IPrimitiveArray block4PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block4Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block4Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block4Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block4Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block4Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block4Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block4Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block4Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block4Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block4Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block4Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block4Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block4Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block4Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block4Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block4Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block4Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block4Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block4Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block4Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block4Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block4Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block4Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block4", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block4Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "positionAndFaceMask"})
    IPrimitiveArray block5PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block5Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block5Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block5Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block5Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block5Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block5Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block5Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block5Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block5Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block5Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block5Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block5Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block5Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block5Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block5Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block5Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block5Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block5Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block5Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block5Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block5Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block5Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block5Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block5", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block5Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "positionAndFaceMask"})
    IPrimitiveArray block6PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block6Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block6Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block6Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block6Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block6Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block6Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block6Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block6Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block6Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block6Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block6Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block6Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block6Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block6Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block6Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block6Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block6Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block6Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block6Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block6Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block6Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block6Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block6Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block6", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block6Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "positionAndFaceMask"})
    IPrimitiveArray block7PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block7Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block7Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block7Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block7Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block7Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block7Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block7Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block7Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block7Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block7Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block7Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block7Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block7Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block7Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block7Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block7Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block7Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block7Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block7Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block7Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block7Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block7Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block7Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block7", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block7Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "positionAndFaceMask"})
    IPrimitiveArray block8PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block8Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block8Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block8Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block8Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block8Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block8Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block8Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block8Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block8Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block8Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block8Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block8Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block8Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block8Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block8Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block8Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block8Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block8Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block8Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block8Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block8Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block8Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block8Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block8", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block8Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "positionAndFaceMask"})
    IPrimitiveArray block9PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block9Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block9Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block9Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block9Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block9Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block9Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block9Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block9Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block9Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block9Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block9Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block9Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block9Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block9Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block9Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block9Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block9Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block9Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block9Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block9Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block9Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block9Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block9Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block9", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block9Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "positionAndFaceMask"})
    IPrimitiveArray block10PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block10Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block10Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block10Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block10Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block10Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block10Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block10Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block10Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block10Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block10Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block10Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block10Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block10Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block10Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block10Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block10Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block10Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block10Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block10Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block10Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block10Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block10Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block10Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block10", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block10Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "positionAndFaceMask"})
    IPrimitiveArray block11PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block11Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block11Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block11Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block11Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block11Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block11Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block11Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block11Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block11Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block11Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block11Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block11Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block11Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block11Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block11Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block11Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block11Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block11Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block11Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block11Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block11Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block11Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block11Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block11", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block11Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "positionAndFaceMask"})
    IPrimitiveArray block12PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block12Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block12Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block12Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block12Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block12Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block12Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block12Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block12Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block12Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block12Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block12Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block12Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block12Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block12Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block12Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block12Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block12Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block12Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block12Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block12Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block12Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block12Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block12Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block12", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block12Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "positionAndFaceMask"})
    IPrimitiveArray block13PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block13Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block13Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block13Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block13Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block13Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block13Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block13Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block13Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block13Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block13Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block13Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block13Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block13Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block13Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block13Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block13Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block13Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block13Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block13Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block13Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block13Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block13Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block13Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block13", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block13Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "positionAndFaceMask"})
    IPrimitiveArray block14PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block14Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block14Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block14Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block14Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block14Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block14Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block14Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block14Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block14Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block14Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block14Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block14Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block14Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block14Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block14Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block14Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block14Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block14Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block14Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block14Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block14Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block14Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block14Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block14", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block14Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "positionAndFaceMask"})
    IPrimitiveArray block15PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block15Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block15Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block15Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block15Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block15Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block15Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block15Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block15Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block15Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block15Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block15Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block15Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block15Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block15Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block15Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block15Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block15Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block15Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block15Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block15Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block15Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block15Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block15Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block15", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block15Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "positionAndFaceMask"})
    IPrimitiveArray block16PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block16Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block16Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block16Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block16Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block16Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block16Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block16Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block16Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block16Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block16Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block16Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block16Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block16Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block16Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block16Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block16Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block16Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block16Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block16Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block16Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block16Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block16Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block16Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block16", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block16Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "positionAndFaceMask"})
    IPrimitiveArray block17PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block17Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block17Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block17Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block17Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block17Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block17Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block17Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block17Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block17Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block17Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block17Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block17Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block17Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block17Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block17Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block17Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block17Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block17Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block17Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block17Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block17Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block17Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block17Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block17", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block17Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "positionAndFaceMask"})
    IPrimitiveArray block18PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block18Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block18Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block18Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block18Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block18Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block18Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block18Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block18Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block18Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block18Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block18Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block18Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block18Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block18Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block18Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block18Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block18Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block18Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block18Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block18Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block18Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block18Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block18Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block18", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block18Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "positionAndFaceMask"})
    IPrimitiveArray block19PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block19Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block19Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block19Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block19Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block19Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block19Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block19Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block19Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block19Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block19Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block19Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block19Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block19Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block19Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block19Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block19Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block19Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block19Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block19Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block19Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block19Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block19Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block19Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block19", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block19Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "positionAndFaceMask"})
    IPrimitiveArray block20PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block20Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block20Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block20Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block20Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block20Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block20Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block20Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block20Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block20Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block20Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block20Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block20Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block20Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block20Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block20Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block20Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block20Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block20Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block20Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block20Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block20Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block20Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block20Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block20", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block20Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "positionAndFaceMask"})
    IPrimitiveArray block21PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block21Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block21Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block21Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block21Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block21Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block21Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block21Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block21Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block21Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block21Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block21Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block21Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block21Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block21Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block21Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block21Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block21Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block21Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block21Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block21Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block21Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block21Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block21Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block21", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block21Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "positionAndFaceMask"})
    IPrimitiveArray block22PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block22Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block22Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block22Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block22Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block22Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block22Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block22Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block22Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block22Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block22Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block22Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block22Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block22Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block22Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block22Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block22Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block22Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block22Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block22Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block22Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block22Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block22Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block22Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block22", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block22Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "positionAndFaceMask"})
    IPrimitiveArray block23PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block23Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block23Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block23Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block23Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block23Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block23Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block23Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block23Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block23Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block23Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block23Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block23Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block23Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block23Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block23Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block23Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block23Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block23Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block23Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block23Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block23Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block23Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block23Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block23", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block23Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "positionAndFaceMask"})
    IPrimitiveArray block24PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block24Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block24Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block24Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block24Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block24Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block24Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block24Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block24Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block24Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block24Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block24Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block24Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block24Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block24Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block24Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block24Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block24Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block24Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block24Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block24Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block24Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block24Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block24Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block24", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block24Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "positionAndFaceMask"})
    IPrimitiveArray block25PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block25Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block25Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block25Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block25Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block25Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block25Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block25Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block25Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block25Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block25Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block25Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block25Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block25Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block25Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block25Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block25Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block25Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block25Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block25Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block25Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block25Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block25Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block25Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block25", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block25Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "positionAndFaceMask"})
    IPrimitiveArray block26PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block26Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block26Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block26Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block26Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block26Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block26Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block26Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block26Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block26Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block26Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block26Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block26Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block26Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block26Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block26Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block26Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block26Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block26Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block26Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block26Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block26Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block26Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block26Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block26", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block26Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "positionAndFaceMask"})
    IPrimitiveArray block27PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block27Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block27Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block27Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block27Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block27Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block27Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block27Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block27Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block27Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block27Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block27Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block27Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block27Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block27Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block27Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block27Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block27Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block27Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block27Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block27Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block27Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block27Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block27Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block27", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block27Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "positionAndFaceMask"})
    IPrimitiveArray block28PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block28Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block28Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block28Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block28Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block28Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block28Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block28Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block28Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block28Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block28Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block28Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block28Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block28Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block28Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block28Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block28Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block28Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block28Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block28Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block28Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block28Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block28Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block28Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block28", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block28Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "positionAndFaceMask"})
    IPrimitiveArray block29PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block29Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block29Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block29Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block29Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block29Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block29Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block29Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block29Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block29Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block29Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block29Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block29Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block29Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block29Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block29Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block29Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block29Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block29Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block29Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block29Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block29Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block29Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block29Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block29", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block29Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "positionAndFaceMask"})
    IPrimitiveArray block30PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block30Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block30Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block30Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block30Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block30Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block30Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block30Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block30Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block30Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block30Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block30Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block30Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block30Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block30Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block30Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block30Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block30Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block30Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block30Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block30Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block30Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block30Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block30Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block30", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block30Zm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "positionAndFaceMask"})
    IPrimitiveArray block31PfArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xPlusFaceTexCoord0"})
    IPrimitiveArray block31Xp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xPlusFaceTexCoord1"})
    IPrimitiveArray block31Xp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xPlusFaceTexCoord2"})
    IPrimitiveArray block31Xp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xPlusFaceTexCoord3"})
    IPrimitiveArray block31Xp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xMinusFaceTexCoord0"})
    IPrimitiveArray block31Xm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xMinusFaceTexCoord1"})
    IPrimitiveArray block31Xm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xMinusFaceTexCoord2"})
    IPrimitiveArray block31Xm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "xMinusFaceTexCoord3"})
    IPrimitiveArray block31Xm3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yPlusFaceTexCoord0"})
    IPrimitiveArray block31Yp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yPlusFaceTexCoord1"})
    IPrimitiveArray block31Yp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yPlusFaceTexCoord2"})
    IPrimitiveArray block31Yp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yPlusFaceTexCoord3"})
    IPrimitiveArray block31Yp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yMinusFaceTexCoord0"})
    IPrimitiveArray block31Ym0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yMinusFaceTexCoord1"})
    IPrimitiveArray block31Ym1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yMinusFaceTexCoord2"})
    IPrimitiveArray block31Ym2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "yMinusFaceTexCoord3"})
    IPrimitiveArray block31Ym3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zPlusFaceTexCoord0"})
    IPrimitiveArray block31Zp0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zPlusFaceTexCoord1"})
    IPrimitiveArray block31Zp1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zPlusFaceTexCoord2"})
    IPrimitiveArray block31Zp2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zPlusFaceTexCoord3"})
    IPrimitiveArray block31Zp3Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zMinusFaceTexCoord0"})
    IPrimitiveArray block31Zm0Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zMinusFaceTexCoord1"})
    IPrimitiveArray block31Zm1Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zMinusFaceTexCoord2"})
    IPrimitiveArray block31Zm2Array;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"block31", "blockInfo", "zMinusFaceTexCoord3"})
    IPrimitiveArray block31Zm3Array;
    //</editor-fold>

    ByteBuffer byteBuffer = null;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index, int entityID, int threadOrdinal) {
        int meshletId = meshletIdArray.getInt(index);
        if (meshletId == -1) {
            return;
        }

        if (byteBuffer == null) {
            byteBuffer = meshletGpuWriter.getNewByteBufferView(); // one view per thread
        }

        meshletGpuWriter.writeMeshlet(
                byteBuffer,
                meshletId,
                chunkPosXArray.getInt(index),
                chunkPosYArray.getInt(index),
                chunkPosZArray.getInt(index),
                normalXArray.getFloat(index),
                normalYArray.getFloat(index),
                normalZArray.getFloat(index),
                block0PfArray.getInt(index),
                block0Xp0Array.getInt(index),
                block0Xp1Array.getInt(index),
                block0Xp2Array.getInt(index),
                block0Xp3Array.getInt(index),
                block0Xm0Array.getInt(index),
                block0Xm1Array.getInt(index),
                block0Xm2Array.getInt(index),
                block0Xm3Array.getInt(index),
                block0Yp0Array.getInt(index),
                block0Yp1Array.getInt(index),
                block0Yp2Array.getInt(index),
                block0Yp3Array.getInt(index),
                block0Ym0Array.getInt(index),
                block0Ym1Array.getInt(index),
                block0Ym2Array.getInt(index),
                block0Ym3Array.getInt(index),
                block0Zp0Array.getInt(index),
                block0Zp1Array.getInt(index),
                block0Zp2Array.getInt(index),
                block0Zp3Array.getInt(index),
                block0Zm0Array.getInt(index),
                block0Zm1Array.getInt(index),
                block0Zm2Array.getInt(index),
                block0Zm3Array.getInt(index),
                block1PfArray.getInt(index),
                block1Xp0Array.getInt(index),
                block1Xp1Array.getInt(index),
                block1Xp2Array.getInt(index),
                block1Xp3Array.getInt(index),
                block1Xm0Array.getInt(index),
                block1Xm1Array.getInt(index),
                block1Xm2Array.getInt(index),
                block1Xm3Array.getInt(index),
                block1Yp0Array.getInt(index),
                block1Yp1Array.getInt(index),
                block1Yp2Array.getInt(index),
                block1Yp3Array.getInt(index),
                block1Ym0Array.getInt(index),
                block1Ym1Array.getInt(index),
                block1Ym2Array.getInt(index),
                block1Ym3Array.getInt(index),
                block1Zp0Array.getInt(index),
                block1Zp1Array.getInt(index),
                block1Zp2Array.getInt(index),
                block1Zp3Array.getInt(index),
                block1Zm0Array.getInt(index),
                block1Zm1Array.getInt(index),
                block1Zm2Array.getInt(index),
                block1Zm3Array.getInt(index),
                block2PfArray.getInt(index),
                block2Xp0Array.getInt(index),
                block2Xp1Array.getInt(index),
                block2Xp2Array.getInt(index),
                block2Xp3Array.getInt(index),
                block2Xm0Array.getInt(index),
                block2Xm1Array.getInt(index),
                block2Xm2Array.getInt(index),
                block2Xm3Array.getInt(index),
                block2Yp0Array.getInt(index),
                block2Yp1Array.getInt(index),
                block2Yp2Array.getInt(index),
                block2Yp3Array.getInt(index),
                block2Ym0Array.getInt(index),
                block2Ym1Array.getInt(index),
                block2Ym2Array.getInt(index),
                block2Ym3Array.getInt(index),
                block2Zp0Array.getInt(index),
                block2Zp1Array.getInt(index),
                block2Zp2Array.getInt(index),
                block2Zp3Array.getInt(index),
                block2Zm0Array.getInt(index),
                block2Zm1Array.getInt(index),
                block2Zm2Array.getInt(index),
                block2Zm3Array.getInt(index),
                block3PfArray.getInt(index),
                block3Xp0Array.getInt(index),
                block3Xp1Array.getInt(index),
                block3Xp2Array.getInt(index),
                block3Xp3Array.getInt(index),
                block3Xm0Array.getInt(index),
                block3Xm1Array.getInt(index),
                block3Xm2Array.getInt(index),
                block3Xm3Array.getInt(index),
                block3Yp0Array.getInt(index),
                block3Yp1Array.getInt(index),
                block3Yp2Array.getInt(index),
                block3Yp3Array.getInt(index),
                block3Ym0Array.getInt(index),
                block3Ym1Array.getInt(index),
                block3Ym2Array.getInt(index),
                block3Ym3Array.getInt(index),
                block3Zp0Array.getInt(index),
                block3Zp1Array.getInt(index),
                block3Zp2Array.getInt(index),
                block3Zp3Array.getInt(index),
                block3Zm0Array.getInt(index),
                block3Zm1Array.getInt(index),
                block3Zm2Array.getInt(index),
                block3Zm3Array.getInt(index),
                block4PfArray.getInt(index),
                block4Xp0Array.getInt(index),
                block4Xp1Array.getInt(index),
                block4Xp2Array.getInt(index),
                block4Xp3Array.getInt(index),
                block4Xm0Array.getInt(index),
                block4Xm1Array.getInt(index),
                block4Xm2Array.getInt(index),
                block4Xm3Array.getInt(index),
                block4Yp0Array.getInt(index),
                block4Yp1Array.getInt(index),
                block4Yp2Array.getInt(index),
                block4Yp3Array.getInt(index),
                block4Ym0Array.getInt(index),
                block4Ym1Array.getInt(index),
                block4Ym2Array.getInt(index),
                block4Ym3Array.getInt(index),
                block4Zp0Array.getInt(index),
                block4Zp1Array.getInt(index),
                block4Zp2Array.getInt(index),
                block4Zp3Array.getInt(index),
                block4Zm0Array.getInt(index),
                block4Zm1Array.getInt(index),
                block4Zm2Array.getInt(index),
                block4Zm3Array.getInt(index),
                block5PfArray.getInt(index),
                block5Xp0Array.getInt(index),
                block5Xp1Array.getInt(index),
                block5Xp2Array.getInt(index),
                block5Xp3Array.getInt(index),
                block5Xm0Array.getInt(index),
                block5Xm1Array.getInt(index),
                block5Xm2Array.getInt(index),
                block5Xm3Array.getInt(index),
                block5Yp0Array.getInt(index),
                block5Yp1Array.getInt(index),
                block5Yp2Array.getInt(index),
                block5Yp3Array.getInt(index),
                block5Ym0Array.getInt(index),
                block5Ym1Array.getInt(index),
                block5Ym2Array.getInt(index),
                block5Ym3Array.getInt(index),
                block5Zp0Array.getInt(index),
                block5Zp1Array.getInt(index),
                block5Zp2Array.getInt(index),
                block5Zp3Array.getInt(index),
                block5Zm0Array.getInt(index),
                block5Zm1Array.getInt(index),
                block5Zm2Array.getInt(index),
                block5Zm3Array.getInt(index),
                block6PfArray.getInt(index),
                block6Xp0Array.getInt(index),
                block6Xp1Array.getInt(index),
                block6Xp2Array.getInt(index),
                block6Xp3Array.getInt(index),
                block6Xm0Array.getInt(index),
                block6Xm1Array.getInt(index),
                block6Xm2Array.getInt(index),
                block6Xm3Array.getInt(index),
                block6Yp0Array.getInt(index),
                block6Yp1Array.getInt(index),
                block6Yp2Array.getInt(index),
                block6Yp3Array.getInt(index),
                block6Ym0Array.getInt(index),
                block6Ym1Array.getInt(index),
                block6Ym2Array.getInt(index),
                block6Ym3Array.getInt(index),
                block6Zp0Array.getInt(index),
                block6Zp1Array.getInt(index),
                block6Zp2Array.getInt(index),
                block6Zp3Array.getInt(index),
                block6Zm0Array.getInt(index),
                block6Zm1Array.getInt(index),
                block6Zm2Array.getInt(index),
                block6Zm3Array.getInt(index),
                block7PfArray.getInt(index),
                block7Xp0Array.getInt(index),
                block7Xp1Array.getInt(index),
                block7Xp2Array.getInt(index),
                block7Xp3Array.getInt(index),
                block7Xm0Array.getInt(index),
                block7Xm1Array.getInt(index),
                block7Xm2Array.getInt(index),
                block7Xm3Array.getInt(index),
                block7Yp0Array.getInt(index),
                block7Yp1Array.getInt(index),
                block7Yp2Array.getInt(index),
                block7Yp3Array.getInt(index),
                block7Ym0Array.getInt(index),
                block7Ym1Array.getInt(index),
                block7Ym2Array.getInt(index),
                block7Ym3Array.getInt(index),
                block7Zp0Array.getInt(index),
                block7Zp1Array.getInt(index),
                block7Zp2Array.getInt(index),
                block7Zp3Array.getInt(index),
                block7Zm0Array.getInt(index),
                block7Zm1Array.getInt(index),
                block7Zm2Array.getInt(index),
                block7Zm3Array.getInt(index),
                block8PfArray.getInt(index),
                block8Xp0Array.getInt(index),
                block8Xp1Array.getInt(index),
                block8Xp2Array.getInt(index),
                block8Xp3Array.getInt(index),
                block8Xm0Array.getInt(index),
                block8Xm1Array.getInt(index),
                block8Xm2Array.getInt(index),
                block8Xm3Array.getInt(index),
                block8Yp0Array.getInt(index),
                block8Yp1Array.getInt(index),
                block8Yp2Array.getInt(index),
                block8Yp3Array.getInt(index),
                block8Ym0Array.getInt(index),
                block8Ym1Array.getInt(index),
                block8Ym2Array.getInt(index),
                block8Ym3Array.getInt(index),
                block8Zp0Array.getInt(index),
                block8Zp1Array.getInt(index),
                block8Zp2Array.getInt(index),
                block8Zp3Array.getInt(index),
                block8Zm0Array.getInt(index),
                block8Zm1Array.getInt(index),
                block8Zm2Array.getInt(index),
                block8Zm3Array.getInt(index),
                block9PfArray.getInt(index),
                block9Xp0Array.getInt(index),
                block9Xp1Array.getInt(index),
                block9Xp2Array.getInt(index),
                block9Xp3Array.getInt(index),
                block9Xm0Array.getInt(index),
                block9Xm1Array.getInt(index),
                block9Xm2Array.getInt(index),
                block9Xm3Array.getInt(index),
                block9Yp0Array.getInt(index),
                block9Yp1Array.getInt(index),
                block9Yp2Array.getInt(index),
                block9Yp3Array.getInt(index),
                block9Ym0Array.getInt(index),
                block9Ym1Array.getInt(index),
                block9Ym2Array.getInt(index),
                block9Ym3Array.getInt(index),
                block9Zp0Array.getInt(index),
                block9Zp1Array.getInt(index),
                block9Zp2Array.getInt(index),
                block9Zp3Array.getInt(index),
                block9Zm0Array.getInt(index),
                block9Zm1Array.getInt(index),
                block9Zm2Array.getInt(index),
                block9Zm3Array.getInt(index),
                block10PfArray.getInt(index),
                block10Xp0Array.getInt(index),
                block10Xp1Array.getInt(index),
                block10Xp2Array.getInt(index),
                block10Xp3Array.getInt(index),
                block10Xm0Array.getInt(index),
                block10Xm1Array.getInt(index),
                block10Xm2Array.getInt(index),
                block10Xm3Array.getInt(index),
                block10Yp0Array.getInt(index),
                block10Yp1Array.getInt(index),
                block10Yp2Array.getInt(index),
                block10Yp3Array.getInt(index),
                block10Ym0Array.getInt(index),
                block10Ym1Array.getInt(index),
                block10Ym2Array.getInt(index),
                block10Ym3Array.getInt(index),
                block10Zp0Array.getInt(index),
                block10Zp1Array.getInt(index),
                block10Zp2Array.getInt(index),
                block10Zp3Array.getInt(index),
                block10Zm0Array.getInt(index),
                block10Zm1Array.getInt(index),
                block10Zm2Array.getInt(index),
                block10Zm3Array.getInt(index),
                block11PfArray.getInt(index),
                block11Xp0Array.getInt(index),
                block11Xp1Array.getInt(index),
                block11Xp2Array.getInt(index),
                block11Xp3Array.getInt(index),
                block11Xm0Array.getInt(index),
                block11Xm1Array.getInt(index),
                block11Xm2Array.getInt(index),
                block11Xm3Array.getInt(index),
                block11Yp0Array.getInt(index),
                block11Yp1Array.getInt(index),
                block11Yp2Array.getInt(index),
                block11Yp3Array.getInt(index),
                block11Ym0Array.getInt(index),
                block11Ym1Array.getInt(index),
                block11Ym2Array.getInt(index),
                block11Ym3Array.getInt(index),
                block11Zp0Array.getInt(index),
                block11Zp1Array.getInt(index),
                block11Zp2Array.getInt(index),
                block11Zp3Array.getInt(index),
                block11Zm0Array.getInt(index),
                block11Zm1Array.getInt(index),
                block11Zm2Array.getInt(index),
                block11Zm3Array.getInt(index),
                block12PfArray.getInt(index),
                block12Xp0Array.getInt(index),
                block12Xp1Array.getInt(index),
                block12Xp2Array.getInt(index),
                block12Xp3Array.getInt(index),
                block12Xm0Array.getInt(index),
                block12Xm1Array.getInt(index),
                block12Xm2Array.getInt(index),
                block12Xm3Array.getInt(index),
                block12Yp0Array.getInt(index),
                block12Yp1Array.getInt(index),
                block12Yp2Array.getInt(index),
                block12Yp3Array.getInt(index),
                block12Ym0Array.getInt(index),
                block12Ym1Array.getInt(index),
                block12Ym2Array.getInt(index),
                block12Ym3Array.getInt(index),
                block12Zp0Array.getInt(index),
                block12Zp1Array.getInt(index),
                block12Zp2Array.getInt(index),
                block12Zp3Array.getInt(index),
                block12Zm0Array.getInt(index),
                block12Zm1Array.getInt(index),
                block12Zm2Array.getInt(index),
                block12Zm3Array.getInt(index),
                block13PfArray.getInt(index),
                block13Xp0Array.getInt(index),
                block13Xp1Array.getInt(index),
                block13Xp2Array.getInt(index),
                block13Xp3Array.getInt(index),
                block13Xm0Array.getInt(index),
                block13Xm1Array.getInt(index),
                block13Xm2Array.getInt(index),
                block13Xm3Array.getInt(index),
                block13Yp0Array.getInt(index),
                block13Yp1Array.getInt(index),
                block13Yp2Array.getInt(index),
                block13Yp3Array.getInt(index),
                block13Ym0Array.getInt(index),
                block13Ym1Array.getInt(index),
                block13Ym2Array.getInt(index),
                block13Ym3Array.getInt(index),
                block13Zp0Array.getInt(index),
                block13Zp1Array.getInt(index),
                block13Zp2Array.getInt(index),
                block13Zp3Array.getInt(index),
                block13Zm0Array.getInt(index),
                block13Zm1Array.getInt(index),
                block13Zm2Array.getInt(index),
                block13Zm3Array.getInt(index),
                block14PfArray.getInt(index),
                block14Xp0Array.getInt(index),
                block14Xp1Array.getInt(index),
                block14Xp2Array.getInt(index),
                block14Xp3Array.getInt(index),
                block14Xm0Array.getInt(index),
                block14Xm1Array.getInt(index),
                block14Xm2Array.getInt(index),
                block14Xm3Array.getInt(index),
                block14Yp0Array.getInt(index),
                block14Yp1Array.getInt(index),
                block14Yp2Array.getInt(index),
                block14Yp3Array.getInt(index),
                block14Ym0Array.getInt(index),
                block14Ym1Array.getInt(index),
                block14Ym2Array.getInt(index),
                block14Ym3Array.getInt(index),
                block14Zp0Array.getInt(index),
                block14Zp1Array.getInt(index),
                block14Zp2Array.getInt(index),
                block14Zp3Array.getInt(index),
                block14Zm0Array.getInt(index),
                block14Zm1Array.getInt(index),
                block14Zm2Array.getInt(index),
                block14Zm3Array.getInt(index),
                block15PfArray.getInt(index),
                block15Xp0Array.getInt(index),
                block15Xp1Array.getInt(index),
                block15Xp2Array.getInt(index),
                block15Xp3Array.getInt(index),
                block15Xm0Array.getInt(index),
                block15Xm1Array.getInt(index),
                block15Xm2Array.getInt(index),
                block15Xm3Array.getInt(index),
                block15Yp0Array.getInt(index),
                block15Yp1Array.getInt(index),
                block15Yp2Array.getInt(index),
                block15Yp3Array.getInt(index),
                block15Ym0Array.getInt(index),
                block15Ym1Array.getInt(index),
                block15Ym2Array.getInt(index),
                block15Ym3Array.getInt(index),
                block15Zp0Array.getInt(index),
                block15Zp1Array.getInt(index),
                block15Zp2Array.getInt(index),
                block15Zp3Array.getInt(index),
                block15Zm0Array.getInt(index),
                block15Zm1Array.getInt(index),
                block15Zm2Array.getInt(index),
                block15Zm3Array.getInt(index),
                block16PfArray.getInt(index),
                block16Xp0Array.getInt(index),
                block16Xp1Array.getInt(index),
                block16Xp2Array.getInt(index),
                block16Xp3Array.getInt(index),
                block16Xm0Array.getInt(index),
                block16Xm1Array.getInt(index),
                block16Xm2Array.getInt(index),
                block16Xm3Array.getInt(index),
                block16Yp0Array.getInt(index),
                block16Yp1Array.getInt(index),
                block16Yp2Array.getInt(index),
                block16Yp3Array.getInt(index),
                block16Ym0Array.getInt(index),
                block16Ym1Array.getInt(index),
                block16Ym2Array.getInt(index),
                block16Ym3Array.getInt(index),
                block16Zp0Array.getInt(index),
                block16Zp1Array.getInt(index),
                block16Zp2Array.getInt(index),
                block16Zp3Array.getInt(index),
                block16Zm0Array.getInt(index),
                block16Zm1Array.getInt(index),
                block16Zm2Array.getInt(index),
                block16Zm3Array.getInt(index),
                block17PfArray.getInt(index),
                block17Xp0Array.getInt(index),
                block17Xp1Array.getInt(index),
                block17Xp2Array.getInt(index),
                block17Xp3Array.getInt(index),
                block17Xm0Array.getInt(index),
                block17Xm1Array.getInt(index),
                block17Xm2Array.getInt(index),
                block17Xm3Array.getInt(index),
                block17Yp0Array.getInt(index),
                block17Yp1Array.getInt(index),
                block17Yp2Array.getInt(index),
                block17Yp3Array.getInt(index),
                block17Ym0Array.getInt(index),
                block17Ym1Array.getInt(index),
                block17Ym2Array.getInt(index),
                block17Ym3Array.getInt(index),
                block17Zp0Array.getInt(index),
                block17Zp1Array.getInt(index),
                block17Zp2Array.getInt(index),
                block17Zp3Array.getInt(index),
                block17Zm0Array.getInt(index),
                block17Zm1Array.getInt(index),
                block17Zm2Array.getInt(index),
                block17Zm3Array.getInt(index),
                block18PfArray.getInt(index),
                block18Xp0Array.getInt(index),
                block18Xp1Array.getInt(index),
                block18Xp2Array.getInt(index),
                block18Xp3Array.getInt(index),
                block18Xm0Array.getInt(index),
                block18Xm1Array.getInt(index),
                block18Xm2Array.getInt(index),
                block18Xm3Array.getInt(index),
                block18Yp0Array.getInt(index),
                block18Yp1Array.getInt(index),
                block18Yp2Array.getInt(index),
                block18Yp3Array.getInt(index),
                block18Ym0Array.getInt(index),
                block18Ym1Array.getInt(index),
                block18Ym2Array.getInt(index),
                block18Ym3Array.getInt(index),
                block18Zp0Array.getInt(index),
                block18Zp1Array.getInt(index),
                block18Zp2Array.getInt(index),
                block18Zp3Array.getInt(index),
                block18Zm0Array.getInt(index),
                block18Zm1Array.getInt(index),
                block18Zm2Array.getInt(index),
                block18Zm3Array.getInt(index),
                block19PfArray.getInt(index),
                block19Xp0Array.getInt(index),
                block19Xp1Array.getInt(index),
                block19Xp2Array.getInt(index),
                block19Xp3Array.getInt(index),
                block19Xm0Array.getInt(index),
                block19Xm1Array.getInt(index),
                block19Xm2Array.getInt(index),
                block19Xm3Array.getInt(index),
                block19Yp0Array.getInt(index),
                block19Yp1Array.getInt(index),
                block19Yp2Array.getInt(index),
                block19Yp3Array.getInt(index),
                block19Ym0Array.getInt(index),
                block19Ym1Array.getInt(index),
                block19Ym2Array.getInt(index),
                block19Ym3Array.getInt(index),
                block19Zp0Array.getInt(index),
                block19Zp1Array.getInt(index),
                block19Zp2Array.getInt(index),
                block19Zp3Array.getInt(index),
                block19Zm0Array.getInt(index),
                block19Zm1Array.getInt(index),
                block19Zm2Array.getInt(index),
                block19Zm3Array.getInt(index),
                block20PfArray.getInt(index),
                block20Xp0Array.getInt(index),
                block20Xp1Array.getInt(index),
                block20Xp2Array.getInt(index),
                block20Xp3Array.getInt(index),
                block20Xm0Array.getInt(index),
                block20Xm1Array.getInt(index),
                block20Xm2Array.getInt(index),
                block20Xm3Array.getInt(index),
                block20Yp0Array.getInt(index),
                block20Yp1Array.getInt(index),
                block20Yp2Array.getInt(index),
                block20Yp3Array.getInt(index),
                block20Ym0Array.getInt(index),
                block20Ym1Array.getInt(index),
                block20Ym2Array.getInt(index),
                block20Ym3Array.getInt(index),
                block20Zp0Array.getInt(index),
                block20Zp1Array.getInt(index),
                block20Zp2Array.getInt(index),
                block20Zp3Array.getInt(index),
                block20Zm0Array.getInt(index),
                block20Zm1Array.getInt(index),
                block20Zm2Array.getInt(index),
                block20Zm3Array.getInt(index),
                block21PfArray.getInt(index),
                block21Xp0Array.getInt(index),
                block21Xp1Array.getInt(index),
                block21Xp2Array.getInt(index),
                block21Xp3Array.getInt(index),
                block21Xm0Array.getInt(index),
                block21Xm1Array.getInt(index),
                block21Xm2Array.getInt(index),
                block21Xm3Array.getInt(index),
                block21Yp0Array.getInt(index),
                block21Yp1Array.getInt(index),
                block21Yp2Array.getInt(index),
                block21Yp3Array.getInt(index),
                block21Ym0Array.getInt(index),
                block21Ym1Array.getInt(index),
                block21Ym2Array.getInt(index),
                block21Ym3Array.getInt(index),
                block21Zp0Array.getInt(index),
                block21Zp1Array.getInt(index),
                block21Zp2Array.getInt(index),
                block21Zp3Array.getInt(index),
                block21Zm0Array.getInt(index),
                block21Zm1Array.getInt(index),
                block21Zm2Array.getInt(index),
                block21Zm3Array.getInt(index),
                block22PfArray.getInt(index),
                block22Xp0Array.getInt(index),
                block22Xp1Array.getInt(index),
                block22Xp2Array.getInt(index),
                block22Xp3Array.getInt(index),
                block22Xm0Array.getInt(index),
                block22Xm1Array.getInt(index),
                block22Xm2Array.getInt(index),
                block22Xm3Array.getInt(index),
                block22Yp0Array.getInt(index),
                block22Yp1Array.getInt(index),
                block22Yp2Array.getInt(index),
                block22Yp3Array.getInt(index),
                block22Ym0Array.getInt(index),
                block22Ym1Array.getInt(index),
                block22Ym2Array.getInt(index),
                block22Ym3Array.getInt(index),
                block22Zp0Array.getInt(index),
                block22Zp1Array.getInt(index),
                block22Zp2Array.getInt(index),
                block22Zp3Array.getInt(index),
                block22Zm0Array.getInt(index),
                block22Zm1Array.getInt(index),
                block22Zm2Array.getInt(index),
                block22Zm3Array.getInt(index),
                block23PfArray.getInt(index),
                block23Xp0Array.getInt(index),
                block23Xp1Array.getInt(index),
                block23Xp2Array.getInt(index),
                block23Xp3Array.getInt(index),
                block23Xm0Array.getInt(index),
                block23Xm1Array.getInt(index),
                block23Xm2Array.getInt(index),
                block23Xm3Array.getInt(index),
                block23Yp0Array.getInt(index),
                block23Yp1Array.getInt(index),
                block23Yp2Array.getInt(index),
                block23Yp3Array.getInt(index),
                block23Ym0Array.getInt(index),
                block23Ym1Array.getInt(index),
                block23Ym2Array.getInt(index),
                block23Ym3Array.getInt(index),
                block23Zp0Array.getInt(index),
                block23Zp1Array.getInt(index),
                block23Zp2Array.getInt(index),
                block23Zp3Array.getInt(index),
                block23Zm0Array.getInt(index),
                block23Zm1Array.getInt(index),
                block23Zm2Array.getInt(index),
                block23Zm3Array.getInt(index),
                block24PfArray.getInt(index),
                block24Xp0Array.getInt(index),
                block24Xp1Array.getInt(index),
                block24Xp2Array.getInt(index),
                block24Xp3Array.getInt(index),
                block24Xm0Array.getInt(index),
                block24Xm1Array.getInt(index),
                block24Xm2Array.getInt(index),
                block24Xm3Array.getInt(index),
                block24Yp0Array.getInt(index),
                block24Yp1Array.getInt(index),
                block24Yp2Array.getInt(index),
                block24Yp3Array.getInt(index),
                block24Ym0Array.getInt(index),
                block24Ym1Array.getInt(index),
                block24Ym2Array.getInt(index),
                block24Ym3Array.getInt(index),
                block24Zp0Array.getInt(index),
                block24Zp1Array.getInt(index),
                block24Zp2Array.getInt(index),
                block24Zp3Array.getInt(index),
                block24Zm0Array.getInt(index),
                block24Zm1Array.getInt(index),
                block24Zm2Array.getInt(index),
                block24Zm3Array.getInt(index),
                block25PfArray.getInt(index),
                block25Xp0Array.getInt(index),
                block25Xp1Array.getInt(index),
                block25Xp2Array.getInt(index),
                block25Xp3Array.getInt(index),
                block25Xm0Array.getInt(index),
                block25Xm1Array.getInt(index),
                block25Xm2Array.getInt(index),
                block25Xm3Array.getInt(index),
                block25Yp0Array.getInt(index),
                block25Yp1Array.getInt(index),
                block25Yp2Array.getInt(index),
                block25Yp3Array.getInt(index),
                block25Ym0Array.getInt(index),
                block25Ym1Array.getInt(index),
                block25Ym2Array.getInt(index),
                block25Ym3Array.getInt(index),
                block25Zp0Array.getInt(index),
                block25Zp1Array.getInt(index),
                block25Zp2Array.getInt(index),
                block25Zp3Array.getInt(index),
                block25Zm0Array.getInt(index),
                block25Zm1Array.getInt(index),
                block25Zm2Array.getInt(index),
                block25Zm3Array.getInt(index),
                block26PfArray.getInt(index),
                block26Xp0Array.getInt(index),
                block26Xp1Array.getInt(index),
                block26Xp2Array.getInt(index),
                block26Xp3Array.getInt(index),
                block26Xm0Array.getInt(index),
                block26Xm1Array.getInt(index),
                block26Xm2Array.getInt(index),
                block26Xm3Array.getInt(index),
                block26Yp0Array.getInt(index),
                block26Yp1Array.getInt(index),
                block26Yp2Array.getInt(index),
                block26Yp3Array.getInt(index),
                block26Ym0Array.getInt(index),
                block26Ym1Array.getInt(index),
                block26Ym2Array.getInt(index),
                block26Ym3Array.getInt(index),
                block26Zp0Array.getInt(index),
                block26Zp1Array.getInt(index),
                block26Zp2Array.getInt(index),
                block26Zp3Array.getInt(index),
                block26Zm0Array.getInt(index),
                block26Zm1Array.getInt(index),
                block26Zm2Array.getInt(index),
                block26Zm3Array.getInt(index),
                block27PfArray.getInt(index),
                block27Xp0Array.getInt(index),
                block27Xp1Array.getInt(index),
                block27Xp2Array.getInt(index),
                block27Xp3Array.getInt(index),
                block27Xm0Array.getInt(index),
                block27Xm1Array.getInt(index),
                block27Xm2Array.getInt(index),
                block27Xm3Array.getInt(index),
                block27Yp0Array.getInt(index),
                block27Yp1Array.getInt(index),
                block27Yp2Array.getInt(index),
                block27Yp3Array.getInt(index),
                block27Ym0Array.getInt(index),
                block27Ym1Array.getInt(index),
                block27Ym2Array.getInt(index),
                block27Ym3Array.getInt(index),
                block27Zp0Array.getInt(index),
                block27Zp1Array.getInt(index),
                block27Zp2Array.getInt(index),
                block27Zp3Array.getInt(index),
                block27Zm0Array.getInt(index),
                block27Zm1Array.getInt(index),
                block27Zm2Array.getInt(index),
                block27Zm3Array.getInt(index),
                block28PfArray.getInt(index),
                block28Xp0Array.getInt(index),
                block28Xp1Array.getInt(index),
                block28Xp2Array.getInt(index),
                block28Xp3Array.getInt(index),
                block28Xm0Array.getInt(index),
                block28Xm1Array.getInt(index),
                block28Xm2Array.getInt(index),
                block28Xm3Array.getInt(index),
                block28Yp0Array.getInt(index),
                block28Yp1Array.getInt(index),
                block28Yp2Array.getInt(index),
                block28Yp3Array.getInt(index),
                block28Ym0Array.getInt(index),
                block28Ym1Array.getInt(index),
                block28Ym2Array.getInt(index),
                block28Ym3Array.getInt(index),
                block28Zp0Array.getInt(index),
                block28Zp1Array.getInt(index),
                block28Zp2Array.getInt(index),
                block28Zp3Array.getInt(index),
                block28Zm0Array.getInt(index),
                block28Zm1Array.getInt(index),
                block28Zm2Array.getInt(index),
                block28Zm3Array.getInt(index),
                block29PfArray.getInt(index),
                block29Xp0Array.getInt(index),
                block29Xp1Array.getInt(index),
                block29Xp2Array.getInt(index),
                block29Xp3Array.getInt(index),
                block29Xm0Array.getInt(index),
                block29Xm1Array.getInt(index),
                block29Xm2Array.getInt(index),
                block29Xm3Array.getInt(index),
                block29Yp0Array.getInt(index),
                block29Yp1Array.getInt(index),
                block29Yp2Array.getInt(index),
                block29Yp3Array.getInt(index),
                block29Ym0Array.getInt(index),
                block29Ym1Array.getInt(index),
                block29Ym2Array.getInt(index),
                block29Ym3Array.getInt(index),
                block29Zp0Array.getInt(index),
                block29Zp1Array.getInt(index),
                block29Zp2Array.getInt(index),
                block29Zp3Array.getInt(index),
                block29Zm0Array.getInt(index),
                block29Zm1Array.getInt(index),
                block29Zm2Array.getInt(index),
                block29Zm3Array.getInt(index),
                block30PfArray.getInt(index),
                block30Xp0Array.getInt(index),
                block30Xp1Array.getInt(index),
                block30Xp2Array.getInt(index),
                block30Xp3Array.getInt(index),
                block30Xm0Array.getInt(index),
                block30Xm1Array.getInt(index),
                block30Xm2Array.getInt(index),
                block30Xm3Array.getInt(index),
                block30Yp0Array.getInt(index),
                block30Yp1Array.getInt(index),
                block30Yp2Array.getInt(index),
                block30Yp3Array.getInt(index),
                block30Ym0Array.getInt(index),
                block30Ym1Array.getInt(index),
                block30Ym2Array.getInt(index),
                block30Ym3Array.getInt(index),
                block30Zp0Array.getInt(index),
                block30Zp1Array.getInt(index),
                block30Zp2Array.getInt(index),
                block30Zp3Array.getInt(index),
                block30Zm0Array.getInt(index),
                block30Zm1Array.getInt(index),
                block30Zm2Array.getInt(index),
                block30Zm3Array.getInt(index),
                block31PfArray.getInt(index),
                block31Xp0Array.getInt(index),
                block31Xp1Array.getInt(index),
                block31Xp2Array.getInt(index),
                block31Xp3Array.getInt(index),
                block31Xm0Array.getInt(index),
                block31Xm1Array.getInt(index),
                block31Xm2Array.getInt(index),
                block31Xm3Array.getInt(index),
                block31Yp0Array.getInt(index),
                block31Yp1Array.getInt(index),
                block31Yp2Array.getInt(index),
                block31Yp3Array.getInt(index),
                block31Ym0Array.getInt(index),
                block31Ym1Array.getInt(index),
                block31Ym2Array.getInt(index),
                block31Ym3Array.getInt(index),
                block31Zp0Array.getInt(index),
                block31Zp1Array.getInt(index),
                block31Zp2Array.getInt(index),
                block31Zp3Array.getInt(index),
                block31Zm0Array.getInt(index),
                block31Zm1Array.getInt(index),
                block31Zm2Array.getInt(index),
                block31Zm3Array.getInt(index));
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
