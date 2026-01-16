package com.cleanroommc.kirino.engine.render.platform.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuWriterContext;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class MeshletBufferWriteJob implements IParallelJob {
    @JobExternalDataQuery
    MeshletGpuWriterContext meshletGpuWriterContext;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"meshletId"})
    IPrimitiveArray meshletIdArray;

    @JobDataQuery(componentClass = MeshletComponent.class, fieldAccessChain = {"blockCount"})
    IPrimitiveArray blockCountArray;

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
            byteBuffer = meshletGpuWriterContext.getNewByteBufferView(); // one view per thread
        }

        byteBuffer.position(meshletGpuWriterContext.getByteBufferPosition(meshletId));

        int blockCount = blockCountArray.getInt(index);
        int chunkPosX = chunkPosXArray.getInt(index);
        int chunkPosY = chunkPosYArray.getInt(index);
        int chunkPosZ = chunkPosZArray.getInt(index);
        float normalX = normalXArray.getFloat(index);
        float normalY = normalYArray.getFloat(index);
        float normalZ = normalZArray.getFloat(index);

        int block0PositionAndFaceMask = block0PfArray.getInt(index);
        int block0Xp0 = block0Xp0Array.getInt(index);
        int block0Xp1 = block0Xp1Array.getInt(index);
        int block0Xp2 = block0Xp2Array.getInt(index);
        int block0Xp3 = block0Xp3Array.getInt(index);
        int block0Xm0 = block0Xm0Array.getInt(index);
        int block0Xm1 = block0Xm1Array.getInt(index);
        int block0Xm2 = block0Xm2Array.getInt(index);
        int block0Xm3 = block0Xm3Array.getInt(index);
        int block0Yp0 = block0Yp0Array.getInt(index);
        int block0Yp1 = block0Yp1Array.getInt(index);
        int block0Yp2 = block0Yp2Array.getInt(index);
        int block0Yp3 = block0Yp3Array.getInt(index);
        int block0Ym0 = block0Ym0Array.getInt(index);
        int block0Ym1 = block0Ym1Array.getInt(index);
        int block0Ym2 = block0Ym2Array.getInt(index);
        int block0Ym3 = block0Ym3Array.getInt(index);
        int block0Zp0 = block0Zp0Array.getInt(index);
        int block0Zp1 = block0Zp1Array.getInt(index);
        int block0Zp2 = block0Zp2Array.getInt(index);
        int block0Zp3 = block0Zp3Array.getInt(index);
        int block0Zm0 = block0Zm0Array.getInt(index);
        int block0Zm1 = block0Zm1Array.getInt(index);
        int block0Zm2 = block0Zm2Array.getInt(index);
        int block0Zm3 = block0Zm3Array.getInt(index);

        int block1PositionAndFaceMask = block1PfArray.getInt(index);
        int block1Xp0 = block1Xp0Array.getInt(index);
        int block1Xp1 = block1Xp1Array.getInt(index);
        int block1Xp2 = block1Xp2Array.getInt(index);
        int block1Xp3 = block1Xp3Array.getInt(index);
        int block1Xm0 = block1Xm0Array.getInt(index);
        int block1Xm1 = block1Xm1Array.getInt(index);
        int block1Xm2 = block1Xm2Array.getInt(index);
        int block1Xm3 = block1Xm3Array.getInt(index);
        int block1Yp0 = block1Yp0Array.getInt(index);
        int block1Yp1 = block1Yp1Array.getInt(index);
        int block1Yp2 = block1Yp2Array.getInt(index);
        int block1Yp3 = block1Yp3Array.getInt(index);
        int block1Ym0 = block1Ym0Array.getInt(index);
        int block1Ym1 = block1Ym1Array.getInt(index);
        int block1Ym2 = block1Ym2Array.getInt(index);
        int block1Ym3 = block1Ym3Array.getInt(index);
        int block1Zp0 = block1Zp0Array.getInt(index);
        int block1Zp1 = block1Zp1Array.getInt(index);
        int block1Zp2 = block1Zp2Array.getInt(index);
        int block1Zp3 = block1Zp3Array.getInt(index);
        int block1Zm0 = block1Zm0Array.getInt(index);
        int block1Zm1 = block1Zm1Array.getInt(index);
        int block1Zm2 = block1Zm2Array.getInt(index);
        int block1Zm3 = block1Zm3Array.getInt(index);

        int block2PositionAndFaceMask = block2PfArray.getInt(index);
        int block2Xp0 = block2Xp0Array.getInt(index);
        int block2Xp1 = block2Xp1Array.getInt(index);
        int block2Xp2 = block2Xp2Array.getInt(index);
        int block2Xp3 = block2Xp3Array.getInt(index);
        int block2Xm0 = block2Xm0Array.getInt(index);
        int block2Xm1 = block2Xm1Array.getInt(index);
        int block2Xm2 = block2Xm2Array.getInt(index);
        int block2Xm3 = block2Xm3Array.getInt(index);
        int block2Yp0 = block2Yp0Array.getInt(index);
        int block2Yp1 = block2Yp1Array.getInt(index);
        int block2Yp2 = block2Yp2Array.getInt(index);
        int block2Yp3 = block2Yp3Array.getInt(index);
        int block2Ym0 = block2Ym0Array.getInt(index);
        int block2Ym1 = block2Ym1Array.getInt(index);
        int block2Ym2 = block2Ym2Array.getInt(index);
        int block2Ym3 = block2Ym3Array.getInt(index);
        int block2Zp0 = block2Zp0Array.getInt(index);
        int block2Zp1 = block2Zp1Array.getInt(index);
        int block2Zp2 = block2Zp2Array.getInt(index);
        int block2Zp3 = block2Zp3Array.getInt(index);
        int block2Zm0 = block2Zm0Array.getInt(index);
        int block2Zm1 = block2Zm1Array.getInt(index);
        int block2Zm2 = block2Zm2Array.getInt(index);
        int block2Zm3 = block2Zm3Array.getInt(index);

        int block3PositionAndFaceMask = block3PfArray.getInt(index);
        int block3Xp0 = block3Xp0Array.getInt(index);
        int block3Xp1 = block3Xp1Array.getInt(index);
        int block3Xp2 = block3Xp2Array.getInt(index);
        int block3Xp3 = block3Xp3Array.getInt(index);
        int block3Xm0 = block3Xm0Array.getInt(index);
        int block3Xm1 = block3Xm1Array.getInt(index);
        int block3Xm2 = block3Xm2Array.getInt(index);
        int block3Xm3 = block3Xm3Array.getInt(index);
        int block3Yp0 = block3Yp0Array.getInt(index);
        int block3Yp1 = block3Yp1Array.getInt(index);
        int block3Yp2 = block3Yp2Array.getInt(index);
        int block3Yp3 = block3Yp3Array.getInt(index);
        int block3Ym0 = block3Ym0Array.getInt(index);
        int block3Ym1 = block3Ym1Array.getInt(index);
        int block3Ym2 = block3Ym2Array.getInt(index);
        int block3Ym3 = block3Ym3Array.getInt(index);
        int block3Zp0 = block3Zp0Array.getInt(index);
        int block3Zp1 = block3Zp1Array.getInt(index);
        int block3Zp2 = block3Zp2Array.getInt(index);
        int block3Zp3 = block3Zp3Array.getInt(index);
        int block3Zm0 = block3Zm0Array.getInt(index);
        int block3Zm1 = block3Zm1Array.getInt(index);
        int block3Zm2 = block3Zm2Array.getInt(index);
        int block3Zm3 = block3Zm3Array.getInt(index);

        int block4PositionAndFaceMask = block4PfArray.getInt(index);
        int block4Xp0 = block4Xp0Array.getInt(index);
        int block4Xp1 = block4Xp1Array.getInt(index);
        int block4Xp2 = block4Xp2Array.getInt(index);
        int block4Xp3 = block4Xp3Array.getInt(index);
        int block4Xm0 = block4Xm0Array.getInt(index);
        int block4Xm1 = block4Xm1Array.getInt(index);
        int block4Xm2 = block4Xm2Array.getInt(index);
        int block4Xm3 = block4Xm3Array.getInt(index);
        int block4Yp0 = block4Yp0Array.getInt(index);
        int block4Yp1 = block4Yp1Array.getInt(index);
        int block4Yp2 = block4Yp2Array.getInt(index);
        int block4Yp3 = block4Yp3Array.getInt(index);
        int block4Ym0 = block4Ym0Array.getInt(index);
        int block4Ym1 = block4Ym1Array.getInt(index);
        int block4Ym2 = block4Ym2Array.getInt(index);
        int block4Ym3 = block4Ym3Array.getInt(index);
        int block4Zp0 = block4Zp0Array.getInt(index);
        int block4Zp1 = block4Zp1Array.getInt(index);
        int block4Zp2 = block4Zp2Array.getInt(index);
        int block4Zp3 = block4Zp3Array.getInt(index);
        int block4Zm0 = block4Zm0Array.getInt(index);
        int block4Zm1 = block4Zm1Array.getInt(index);
        int block4Zm2 = block4Zm2Array.getInt(index);
        int block4Zm3 = block4Zm3Array.getInt(index);

        int block5PositionAndFaceMask = block5PfArray.getInt(index);
        int block5Xp0 = block5Xp0Array.getInt(index);
        int block5Xp1 = block5Xp1Array.getInt(index);
        int block5Xp2 = block5Xp2Array.getInt(index);
        int block5Xp3 = block5Xp3Array.getInt(index);
        int block5Xm0 = block5Xm0Array.getInt(index);
        int block5Xm1 = block5Xm1Array.getInt(index);
        int block5Xm2 = block5Xm2Array.getInt(index);
        int block5Xm3 = block5Xm3Array.getInt(index);
        int block5Yp0 = block5Yp0Array.getInt(index);
        int block5Yp1 = block5Yp1Array.getInt(index);
        int block5Yp2 = block5Yp2Array.getInt(index);
        int block5Yp3 = block5Yp3Array.getInt(index);
        int block5Ym0 = block5Ym0Array.getInt(index);
        int block5Ym1 = block5Ym1Array.getInt(index);
        int block5Ym2 = block5Ym2Array.getInt(index);
        int block5Ym3 = block5Ym3Array.getInt(index);
        int block5Zp0 = block5Zp0Array.getInt(index);
        int block5Zp1 = block5Zp1Array.getInt(index);
        int block5Zp2 = block5Zp2Array.getInt(index);
        int block5Zp3 = block5Zp3Array.getInt(index);
        int block5Zm0 = block5Zm0Array.getInt(index);
        int block5Zm1 = block5Zm1Array.getInt(index);
        int block5Zm2 = block5Zm2Array.getInt(index);
        int block5Zm3 = block5Zm3Array.getInt(index);

        int block6PositionAndFaceMask = block6PfArray.getInt(index);
        int block6Xp0 = block6Xp0Array.getInt(index);
        int block6Xp1 = block6Xp1Array.getInt(index);
        int block6Xp2 = block6Xp2Array.getInt(index);
        int block6Xp3 = block6Xp3Array.getInt(index);
        int block6Xm0 = block6Xm0Array.getInt(index);
        int block6Xm1 = block6Xm1Array.getInt(index);
        int block6Xm2 = block6Xm2Array.getInt(index);
        int block6Xm3 = block6Xm3Array.getInt(index);
        int block6Yp0 = block6Yp0Array.getInt(index);
        int block6Yp1 = block6Yp1Array.getInt(index);
        int block6Yp2 = block6Yp2Array.getInt(index);
        int block6Yp3 = block6Yp3Array.getInt(index);
        int block6Ym0 = block6Ym0Array.getInt(index);
        int block6Ym1 = block6Ym1Array.getInt(index);
        int block6Ym2 = block6Ym2Array.getInt(index);
        int block6Ym3 = block6Ym3Array.getInt(index);
        int block6Zp0 = block6Zp0Array.getInt(index);
        int block6Zp1 = block6Zp1Array.getInt(index);
        int block6Zp2 = block6Zp2Array.getInt(index);
        int block6Zp3 = block6Zp3Array.getInt(index);
        int block6Zm0 = block6Zm0Array.getInt(index);
        int block6Zm1 = block6Zm1Array.getInt(index);
        int block6Zm2 = block6Zm2Array.getInt(index);
        int block6Zm3 = block6Zm3Array.getInt(index);

        int block7PositionAndFaceMask = block7PfArray.getInt(index);
        int block7Xp0 = block7Xp0Array.getInt(index);
        int block7Xp1 = block7Xp1Array.getInt(index);
        int block7Xp2 = block7Xp2Array.getInt(index);
        int block7Xp3 = block7Xp3Array.getInt(index);
        int block7Xm0 = block7Xm0Array.getInt(index);
        int block7Xm1 = block7Xm1Array.getInt(index);
        int block7Xm2 = block7Xm2Array.getInt(index);
        int block7Xm3 = block7Xm3Array.getInt(index);
        int block7Yp0 = block7Yp0Array.getInt(index);
        int block7Yp1 = block7Yp1Array.getInt(index);
        int block7Yp2 = block7Yp2Array.getInt(index);
        int block7Yp3 = block7Yp3Array.getInt(index);
        int block7Ym0 = block7Ym0Array.getInt(index);
        int block7Ym1 = block7Ym1Array.getInt(index);
        int block7Ym2 = block7Ym2Array.getInt(index);
        int block7Ym3 = block7Ym3Array.getInt(index);
        int block7Zp0 = block7Zp0Array.getInt(index);
        int block7Zp1 = block7Zp1Array.getInt(index);
        int block7Zp2 = block7Zp2Array.getInt(index);
        int block7Zp3 = block7Zp3Array.getInt(index);
        int block7Zm0 = block7Zm0Array.getInt(index);
        int block7Zm1 = block7Zm1Array.getInt(index);
        int block7Zm2 = block7Zm2Array.getInt(index);
        int block7Zm3 = block7Zm3Array.getInt(index);

        int block8PositionAndFaceMask = block8PfArray.getInt(index);
        int block8Xp0 = block8Xp0Array.getInt(index);
        int block8Xp1 = block8Xp1Array.getInt(index);
        int block8Xp2 = block8Xp2Array.getInt(index);
        int block8Xp3 = block8Xp3Array.getInt(index);
        int block8Xm0 = block8Xm0Array.getInt(index);
        int block8Xm1 = block8Xm1Array.getInt(index);
        int block8Xm2 = block8Xm2Array.getInt(index);
        int block8Xm3 = block8Xm3Array.getInt(index);
        int block8Yp0 = block8Yp0Array.getInt(index);
        int block8Yp1 = block8Yp1Array.getInt(index);
        int block8Yp2 = block8Yp2Array.getInt(index);
        int block8Yp3 = block8Yp3Array.getInt(index);
        int block8Ym0 = block8Ym0Array.getInt(index);
        int block8Ym1 = block8Ym1Array.getInt(index);
        int block8Ym2 = block8Ym2Array.getInt(index);
        int block8Ym3 = block8Ym3Array.getInt(index);
        int block8Zp0 = block8Zp0Array.getInt(index);
        int block8Zp1 = block8Zp1Array.getInt(index);
        int block8Zp2 = block8Zp2Array.getInt(index);
        int block8Zp3 = block8Zp3Array.getInt(index);
        int block8Zm0 = block8Zm0Array.getInt(index);
        int block8Zm1 = block8Zm1Array.getInt(index);
        int block8Zm2 = block8Zm2Array.getInt(index);
        int block8Zm3 = block8Zm3Array.getInt(index);

        int block9PositionAndFaceMask = block9PfArray.getInt(index);
        int block9Xp0 = block9Xp0Array.getInt(index);
        int block9Xp1 = block9Xp1Array.getInt(index);
        int block9Xp2 = block9Xp2Array.getInt(index);
        int block9Xp3 = block9Xp3Array.getInt(index);
        int block9Xm0 = block9Xm0Array.getInt(index);
        int block9Xm1 = block9Xm1Array.getInt(index);
        int block9Xm2 = block9Xm2Array.getInt(index);
        int block9Xm3 = block9Xm3Array.getInt(index);
        int block9Yp0 = block9Yp0Array.getInt(index);
        int block9Yp1 = block9Yp1Array.getInt(index);
        int block9Yp2 = block9Yp2Array.getInt(index);
        int block9Yp3 = block9Yp3Array.getInt(index);
        int block9Ym0 = block9Ym0Array.getInt(index);
        int block9Ym1 = block9Ym1Array.getInt(index);
        int block9Ym2 = block9Ym2Array.getInt(index);
        int block9Ym3 = block9Ym3Array.getInt(index);
        int block9Zp0 = block9Zp0Array.getInt(index);
        int block9Zp1 = block9Zp1Array.getInt(index);
        int block9Zp2 = block9Zp2Array.getInt(index);
        int block9Zp3 = block9Zp3Array.getInt(index);
        int block9Zm0 = block9Zm0Array.getInt(index);
        int block9Zm1 = block9Zm1Array.getInt(index);
        int block9Zm2 = block9Zm2Array.getInt(index);
        int block9Zm3 = block9Zm3Array.getInt(index);

        int block10PositionAndFaceMask = block10PfArray.getInt(index);
        int block10Xp0 = block10Xp0Array.getInt(index);
        int block10Xp1 = block10Xp1Array.getInt(index);
        int block10Xp2 = block10Xp2Array.getInt(index);
        int block10Xp3 = block10Xp3Array.getInt(index);
        int block10Xm0 = block10Xm0Array.getInt(index);
        int block10Xm1 = block10Xm1Array.getInt(index);
        int block10Xm2 = block10Xm2Array.getInt(index);
        int block10Xm3 = block10Xm3Array.getInt(index);
        int block10Yp0 = block10Yp0Array.getInt(index);
        int block10Yp1 = block10Yp1Array.getInt(index);
        int block10Yp2 = block10Yp2Array.getInt(index);
        int block10Yp3 = block10Yp3Array.getInt(index);
        int block10Ym0 = block10Ym0Array.getInt(index);
        int block10Ym1 = block10Ym1Array.getInt(index);
        int block10Ym2 = block10Ym2Array.getInt(index);
        int block10Ym3 = block10Ym3Array.getInt(index);
        int block10Zp0 = block10Zp0Array.getInt(index);
        int block10Zp1 = block10Zp1Array.getInt(index);
        int block10Zp2 = block10Zp2Array.getInt(index);
        int block10Zp3 = block10Zp3Array.getInt(index);
        int block10Zm0 = block10Zm0Array.getInt(index);
        int block10Zm1 = block10Zm1Array.getInt(index);
        int block10Zm2 = block10Zm2Array.getInt(index);
        int block10Zm3 = block10Zm3Array.getInt(index);

        int block11PositionAndFaceMask = block11PfArray.getInt(index);
        int block11Xp0 = block11Xp0Array.getInt(index);
        int block11Xp1 = block11Xp1Array.getInt(index);
        int block11Xp2 = block11Xp2Array.getInt(index);
        int block11Xp3 = block11Xp3Array.getInt(index);
        int block11Xm0 = block11Xm0Array.getInt(index);
        int block11Xm1 = block11Xm1Array.getInt(index);
        int block11Xm2 = block11Xm2Array.getInt(index);
        int block11Xm3 = block11Xm3Array.getInt(index);
        int block11Yp0 = block11Yp0Array.getInt(index);
        int block11Yp1 = block11Yp1Array.getInt(index);
        int block11Yp2 = block11Yp2Array.getInt(index);
        int block11Yp3 = block11Yp3Array.getInt(index);
        int block11Ym0 = block11Ym0Array.getInt(index);
        int block11Ym1 = block11Ym1Array.getInt(index);
        int block11Ym2 = block11Ym2Array.getInt(index);
        int block11Ym3 = block11Ym3Array.getInt(index);
        int block11Zp0 = block11Zp0Array.getInt(index);
        int block11Zp1 = block11Zp1Array.getInt(index);
        int block11Zp2 = block11Zp2Array.getInt(index);
        int block11Zp3 = block11Zp3Array.getInt(index);
        int block11Zm0 = block11Zm0Array.getInt(index);
        int block11Zm1 = block11Zm1Array.getInt(index);
        int block11Zm2 = block11Zm2Array.getInt(index);
        int block11Zm3 = block11Zm3Array.getInt(index);

        int block12PositionAndFaceMask = block12PfArray.getInt(index);
        int block12Xp0 = block12Xp0Array.getInt(index);
        int block12Xp1 = block12Xp1Array.getInt(index);
        int block12Xp2 = block12Xp2Array.getInt(index);
        int block12Xp3 = block12Xp3Array.getInt(index);
        int block12Xm0 = block12Xm0Array.getInt(index);
        int block12Xm1 = block12Xm1Array.getInt(index);
        int block12Xm2 = block12Xm2Array.getInt(index);
        int block12Xm3 = block12Xm3Array.getInt(index);
        int block12Yp0 = block12Yp0Array.getInt(index);
        int block12Yp1 = block12Yp1Array.getInt(index);
        int block12Yp2 = block12Yp2Array.getInt(index);
        int block12Yp3 = block12Yp3Array.getInt(index);
        int block12Ym0 = block12Ym0Array.getInt(index);
        int block12Ym1 = block12Ym1Array.getInt(index);
        int block12Ym2 = block12Ym2Array.getInt(index);
        int block12Ym3 = block12Ym3Array.getInt(index);
        int block12Zp0 = block12Zp0Array.getInt(index);
        int block12Zp1 = block12Zp1Array.getInt(index);
        int block12Zp2 = block12Zp2Array.getInt(index);
        int block12Zp3 = block12Zp3Array.getInt(index);
        int block12Zm0 = block12Zm0Array.getInt(index);
        int block12Zm1 = block12Zm1Array.getInt(index);
        int block12Zm2 = block12Zm2Array.getInt(index);
        int block12Zm3 = block12Zm3Array.getInt(index);

        int block13PositionAndFaceMask = block13PfArray.getInt(index);
        int block13Xp0 = block13Xp0Array.getInt(index);
        int block13Xp1 = block13Xp1Array.getInt(index);
        int block13Xp2 = block13Xp2Array.getInt(index);
        int block13Xp3 = block13Xp3Array.getInt(index);
        int block13Xm0 = block13Xm0Array.getInt(index);
        int block13Xm1 = block13Xm1Array.getInt(index);
        int block13Xm2 = block13Xm2Array.getInt(index);
        int block13Xm3 = block13Xm3Array.getInt(index);
        int block13Yp0 = block13Yp0Array.getInt(index);
        int block13Yp1 = block13Yp1Array.getInt(index);
        int block13Yp2 = block13Yp2Array.getInt(index);
        int block13Yp3 = block13Yp3Array.getInt(index);
        int block13Ym0 = block13Ym0Array.getInt(index);
        int block13Ym1 = block13Ym1Array.getInt(index);
        int block13Ym2 = block13Ym2Array.getInt(index);
        int block13Ym3 = block13Ym3Array.getInt(index);
        int block13Zp0 = block13Zp0Array.getInt(index);
        int block13Zp1 = block13Zp1Array.getInt(index);
        int block13Zp2 = block13Zp2Array.getInt(index);
        int block13Zp3 = block13Zp3Array.getInt(index);
        int block13Zm0 = block13Zm0Array.getInt(index);
        int block13Zm1 = block13Zm1Array.getInt(index);
        int block13Zm2 = block13Zm2Array.getInt(index);
        int block13Zm3 = block13Zm3Array.getInt(index);

        int block14PositionAndFaceMask = block14PfArray.getInt(index);
        int block14Xp0 = block14Xp0Array.getInt(index);
        int block14Xp1 = block14Xp1Array.getInt(index);
        int block14Xp2 = block14Xp2Array.getInt(index);
        int block14Xp3 = block14Xp3Array.getInt(index);
        int block14Xm0 = block14Xm0Array.getInt(index);
        int block14Xm1 = block14Xm1Array.getInt(index);
        int block14Xm2 = block14Xm2Array.getInt(index);
        int block14Xm3 = block14Xm3Array.getInt(index);
        int block14Yp0 = block14Yp0Array.getInt(index);
        int block14Yp1 = block14Yp1Array.getInt(index);
        int block14Yp2 = block14Yp2Array.getInt(index);
        int block14Yp3 = block14Yp3Array.getInt(index);
        int block14Ym0 = block14Ym0Array.getInt(index);
        int block14Ym1 = block14Ym1Array.getInt(index);
        int block14Ym2 = block14Ym2Array.getInt(index);
        int block14Ym3 = block14Ym3Array.getInt(index);
        int block14Zp0 = block14Zp0Array.getInt(index);
        int block14Zp1 = block14Zp1Array.getInt(index);
        int block14Zp2 = block14Zp2Array.getInt(index);
        int block14Zp3 = block14Zp3Array.getInt(index);
        int block14Zm0 = block14Zm0Array.getInt(index);
        int block14Zm1 = block14Zm1Array.getInt(index);
        int block14Zm2 = block14Zm2Array.getInt(index);
        int block14Zm3 = block14Zm3Array.getInt(index);

        int block15PositionAndFaceMask = block15PfArray.getInt(index);
        int block15Xp0 = block15Xp0Array.getInt(index);
        int block15Xp1 = block15Xp1Array.getInt(index);
        int block15Xp2 = block15Xp2Array.getInt(index);
        int block15Xp3 = block15Xp3Array.getInt(index);
        int block15Xm0 = block15Xm0Array.getInt(index);
        int block15Xm1 = block15Xm1Array.getInt(index);
        int block15Xm2 = block15Xm2Array.getInt(index);
        int block15Xm3 = block15Xm3Array.getInt(index);
        int block15Yp0 = block15Yp0Array.getInt(index);
        int block15Yp1 = block15Yp1Array.getInt(index);
        int block15Yp2 = block15Yp2Array.getInt(index);
        int block15Yp3 = block15Yp3Array.getInt(index);
        int block15Ym0 = block15Ym0Array.getInt(index);
        int block15Ym1 = block15Ym1Array.getInt(index);
        int block15Ym2 = block15Ym2Array.getInt(index);
        int block15Ym3 = block15Ym3Array.getInt(index);
        int block15Zp0 = block15Zp0Array.getInt(index);
        int block15Zp1 = block15Zp1Array.getInt(index);
        int block15Zp2 = block15Zp2Array.getInt(index);
        int block15Zp3 = block15Zp3Array.getInt(index);
        int block15Zm0 = block15Zm0Array.getInt(index);
        int block15Zm1 = block15Zm1Array.getInt(index);
        int block15Zm2 = block15Zm2Array.getInt(index);
        int block15Zm3 = block15Zm3Array.getInt(index);

        int block16PositionAndFaceMask = block16PfArray.getInt(index);
        int block16Xp0 = block16Xp0Array.getInt(index);
        int block16Xp1 = block16Xp1Array.getInt(index);
        int block16Xp2 = block16Xp2Array.getInt(index);
        int block16Xp3 = block16Xp3Array.getInt(index);
        int block16Xm0 = block16Xm0Array.getInt(index);
        int block16Xm1 = block16Xm1Array.getInt(index);
        int block16Xm2 = block16Xm2Array.getInt(index);
        int block16Xm3 = block16Xm3Array.getInt(index);
        int block16Yp0 = block16Yp0Array.getInt(index);
        int block16Yp1 = block16Yp1Array.getInt(index);
        int block16Yp2 = block16Yp2Array.getInt(index);
        int block16Yp3 = block16Yp3Array.getInt(index);
        int block16Ym0 = block16Ym0Array.getInt(index);
        int block16Ym1 = block16Ym1Array.getInt(index);
        int block16Ym2 = block16Ym2Array.getInt(index);
        int block16Ym3 = block16Ym3Array.getInt(index);
        int block16Zp0 = block16Zp0Array.getInt(index);
        int block16Zp1 = block16Zp1Array.getInt(index);
        int block16Zp2 = block16Zp2Array.getInt(index);
        int block16Zp3 = block16Zp3Array.getInt(index);
        int block16Zm0 = block16Zm0Array.getInt(index);
        int block16Zm1 = block16Zm1Array.getInt(index);
        int block16Zm2 = block16Zm2Array.getInt(index);
        int block16Zm3 = block16Zm3Array.getInt(index);

        int block17PositionAndFaceMask = block17PfArray.getInt(index);
        int block17Xp0 = block17Xp0Array.getInt(index);
        int block17Xp1 = block17Xp1Array.getInt(index);
        int block17Xp2 = block17Xp2Array.getInt(index);
        int block17Xp3 = block17Xp3Array.getInt(index);
        int block17Xm0 = block17Xm0Array.getInt(index);
        int block17Xm1 = block17Xm1Array.getInt(index);
        int block17Xm2 = block17Xm2Array.getInt(index);
        int block17Xm3 = block17Xm3Array.getInt(index);
        int block17Yp0 = block17Yp0Array.getInt(index);
        int block17Yp1 = block17Yp1Array.getInt(index);
        int block17Yp2 = block17Yp2Array.getInt(index);
        int block17Yp3 = block17Yp3Array.getInt(index);
        int block17Ym0 = block17Ym0Array.getInt(index);
        int block17Ym1 = block17Ym1Array.getInt(index);
        int block17Ym2 = block17Ym2Array.getInt(index);
        int block17Ym3 = block17Ym3Array.getInt(index);
        int block17Zp0 = block17Zp0Array.getInt(index);
        int block17Zp1 = block17Zp1Array.getInt(index);
        int block17Zp2 = block17Zp2Array.getInt(index);
        int block17Zp3 = block17Zp3Array.getInt(index);
        int block17Zm0 = block17Zm0Array.getInt(index);
        int block17Zm1 = block17Zm1Array.getInt(index);
        int block17Zm2 = block17Zm2Array.getInt(index);
        int block17Zm3 = block17Zm3Array.getInt(index);

        int block18PositionAndFaceMask = block18PfArray.getInt(index);
        int block18Xp0 = block18Xp0Array.getInt(index);
        int block18Xp1 = block18Xp1Array.getInt(index);
        int block18Xp2 = block18Xp2Array.getInt(index);
        int block18Xp3 = block18Xp3Array.getInt(index);
        int block18Xm0 = block18Xm0Array.getInt(index);
        int block18Xm1 = block18Xm1Array.getInt(index);
        int block18Xm2 = block18Xm2Array.getInt(index);
        int block18Xm3 = block18Xm3Array.getInt(index);
        int block18Yp0 = block18Yp0Array.getInt(index);
        int block18Yp1 = block18Yp1Array.getInt(index);
        int block18Yp2 = block18Yp2Array.getInt(index);
        int block18Yp3 = block18Yp3Array.getInt(index);
        int block18Ym0 = block18Ym0Array.getInt(index);
        int block18Ym1 = block18Ym1Array.getInt(index);
        int block18Ym2 = block18Ym2Array.getInt(index);
        int block18Ym3 = block18Ym3Array.getInt(index);
        int block18Zp0 = block18Zp0Array.getInt(index);
        int block18Zp1 = block18Zp1Array.getInt(index);
        int block18Zp2 = block18Zp2Array.getInt(index);
        int block18Zp3 = block18Zp3Array.getInt(index);
        int block18Zm0 = block18Zm0Array.getInt(index);
        int block18Zm1 = block18Zm1Array.getInt(index);
        int block18Zm2 = block18Zm2Array.getInt(index);
        int block18Zm3 = block18Zm3Array.getInt(index);

        int block19PositionAndFaceMask = block19PfArray.getInt(index);
        int block19Xp0 = block19Xp0Array.getInt(index);
        int block19Xp1 = block19Xp1Array.getInt(index);
        int block19Xp2 = block19Xp2Array.getInt(index);
        int block19Xp3 = block19Xp3Array.getInt(index);
        int block19Xm0 = block19Xm0Array.getInt(index);
        int block19Xm1 = block19Xm1Array.getInt(index);
        int block19Xm2 = block19Xm2Array.getInt(index);
        int block19Xm3 = block19Xm3Array.getInt(index);
        int block19Yp0 = block19Yp0Array.getInt(index);
        int block19Yp1 = block19Yp1Array.getInt(index);
        int block19Yp2 = block19Yp2Array.getInt(index);
        int block19Yp3 = block19Yp3Array.getInt(index);
        int block19Ym0 = block19Ym0Array.getInt(index);
        int block19Ym1 = block19Ym1Array.getInt(index);
        int block19Ym2 = block19Ym2Array.getInt(index);
        int block19Ym3 = block19Ym3Array.getInt(index);
        int block19Zp0 = block19Zp0Array.getInt(index);
        int block19Zp1 = block19Zp1Array.getInt(index);
        int block19Zp2 = block19Zp2Array.getInt(index);
        int block19Zp3 = block19Zp3Array.getInt(index);
        int block19Zm0 = block19Zm0Array.getInt(index);
        int block19Zm1 = block19Zm1Array.getInt(index);
        int block19Zm2 = block19Zm2Array.getInt(index);
        int block19Zm3 = block19Zm3Array.getInt(index);

        int block20PositionAndFaceMask = block20PfArray.getInt(index);
        int block20Xp0 = block20Xp0Array.getInt(index);
        int block20Xp1 = block20Xp1Array.getInt(index);
        int block20Xp2 = block20Xp2Array.getInt(index);
        int block20Xp3 = block20Xp3Array.getInt(index);
        int block20Xm0 = block20Xm0Array.getInt(index);
        int block20Xm1 = block20Xm1Array.getInt(index);
        int block20Xm2 = block20Xm2Array.getInt(index);
        int block20Xm3 = block20Xm3Array.getInt(index);
        int block20Yp0 = block20Yp0Array.getInt(index);
        int block20Yp1 = block20Yp1Array.getInt(index);
        int block20Yp2 = block20Yp2Array.getInt(index);
        int block20Yp3 = block20Yp3Array.getInt(index);
        int block20Ym0 = block20Ym0Array.getInt(index);
        int block20Ym1 = block20Ym1Array.getInt(index);
        int block20Ym2 = block20Ym2Array.getInt(index);
        int block20Ym3 = block20Ym3Array.getInt(index);
        int block20Zp0 = block20Zp0Array.getInt(index);
        int block20Zp1 = block20Zp1Array.getInt(index);
        int block20Zp2 = block20Zp2Array.getInt(index);
        int block20Zp3 = block20Zp3Array.getInt(index);
        int block20Zm0 = block20Zm0Array.getInt(index);
        int block20Zm1 = block20Zm1Array.getInt(index);
        int block20Zm2 = block20Zm2Array.getInt(index);
        int block20Zm3 = block20Zm3Array.getInt(index);

        int block21PositionAndFaceMask = block21PfArray.getInt(index);
        int block21Xp0 = block21Xp0Array.getInt(index);
        int block21Xp1 = block21Xp1Array.getInt(index);
        int block21Xp2 = block21Xp2Array.getInt(index);
        int block21Xp3 = block21Xp3Array.getInt(index);
        int block21Xm0 = block21Xm0Array.getInt(index);
        int block21Xm1 = block21Xm1Array.getInt(index);
        int block21Xm2 = block21Xm2Array.getInt(index);
        int block21Xm3 = block21Xm3Array.getInt(index);
        int block21Yp0 = block21Yp0Array.getInt(index);
        int block21Yp1 = block21Yp1Array.getInt(index);
        int block21Yp2 = block21Yp2Array.getInt(index);
        int block21Yp3 = block21Yp3Array.getInt(index);
        int block21Ym0 = block21Ym0Array.getInt(index);
        int block21Ym1 = block21Ym1Array.getInt(index);
        int block21Ym2 = block21Ym2Array.getInt(index);
        int block21Ym3 = block21Ym3Array.getInt(index);
        int block21Zp0 = block21Zp0Array.getInt(index);
        int block21Zp1 = block21Zp1Array.getInt(index);
        int block21Zp2 = block21Zp2Array.getInt(index);
        int block21Zp3 = block21Zp3Array.getInt(index);
        int block21Zm0 = block21Zm0Array.getInt(index);
        int block21Zm1 = block21Zm1Array.getInt(index);
        int block21Zm2 = block21Zm2Array.getInt(index);
        int block21Zm3 = block21Zm3Array.getInt(index);

        int block22PositionAndFaceMask = block22PfArray.getInt(index);
        int block22Xp0 = block22Xp0Array.getInt(index);
        int block22Xp1 = block22Xp1Array.getInt(index);
        int block22Xp2 = block22Xp2Array.getInt(index);
        int block22Xp3 = block22Xp3Array.getInt(index);
        int block22Xm0 = block22Xm0Array.getInt(index);
        int block22Xm1 = block22Xm1Array.getInt(index);
        int block22Xm2 = block22Xm2Array.getInt(index);
        int block22Xm3 = block22Xm3Array.getInt(index);
        int block22Yp0 = block22Yp0Array.getInt(index);
        int block22Yp1 = block22Yp1Array.getInt(index);
        int block22Yp2 = block22Yp2Array.getInt(index);
        int block22Yp3 = block22Yp3Array.getInt(index);
        int block22Ym0 = block22Ym0Array.getInt(index);
        int block22Ym1 = block22Ym1Array.getInt(index);
        int block22Ym2 = block22Ym2Array.getInt(index);
        int block22Ym3 = block22Ym3Array.getInt(index);
        int block22Zp0 = block22Zp0Array.getInt(index);
        int block22Zp1 = block22Zp1Array.getInt(index);
        int block22Zp2 = block22Zp2Array.getInt(index);
        int block22Zp3 = block22Zp3Array.getInt(index);
        int block22Zm0 = block22Zm0Array.getInt(index);
        int block22Zm1 = block22Zm1Array.getInt(index);
        int block22Zm2 = block22Zm2Array.getInt(index);
        int block22Zm3 = block22Zm3Array.getInt(index);

        int block23PositionAndFaceMask = block23PfArray.getInt(index);
        int block23Xp0 = block23Xp0Array.getInt(index);
        int block23Xp1 = block23Xp1Array.getInt(index);
        int block23Xp2 = block23Xp2Array.getInt(index);
        int block23Xp3 = block23Xp3Array.getInt(index);
        int block23Xm0 = block23Xm0Array.getInt(index);
        int block23Xm1 = block23Xm1Array.getInt(index);
        int block23Xm2 = block23Xm2Array.getInt(index);
        int block23Xm3 = block23Xm3Array.getInt(index);
        int block23Yp0 = block23Yp0Array.getInt(index);
        int block23Yp1 = block23Yp1Array.getInt(index);
        int block23Yp2 = block23Yp2Array.getInt(index);
        int block23Yp3 = block23Yp3Array.getInt(index);
        int block23Ym0 = block23Ym0Array.getInt(index);
        int block23Ym1 = block23Ym1Array.getInt(index);
        int block23Ym2 = block23Ym2Array.getInt(index);
        int block23Ym3 = block23Ym3Array.getInt(index);
        int block23Zp0 = block23Zp0Array.getInt(index);
        int block23Zp1 = block23Zp1Array.getInt(index);
        int block23Zp2 = block23Zp2Array.getInt(index);
        int block23Zp3 = block23Zp3Array.getInt(index);
        int block23Zm0 = block23Zm0Array.getInt(index);
        int block23Zm1 = block23Zm1Array.getInt(index);
        int block23Zm2 = block23Zm2Array.getInt(index);
        int block23Zm3 = block23Zm3Array.getInt(index);

        int block24PositionAndFaceMask = block24PfArray.getInt(index);
        int block24Xp0 = block24Xp0Array.getInt(index);
        int block24Xp1 = block24Xp1Array.getInt(index);
        int block24Xp2 = block24Xp2Array.getInt(index);
        int block24Xp3 = block24Xp3Array.getInt(index);
        int block24Xm0 = block24Xm0Array.getInt(index);
        int block24Xm1 = block24Xm1Array.getInt(index);
        int block24Xm2 = block24Xm2Array.getInt(index);
        int block24Xm3 = block24Xm3Array.getInt(index);
        int block24Yp0 = block24Yp0Array.getInt(index);
        int block24Yp1 = block24Yp1Array.getInt(index);
        int block24Yp2 = block24Yp2Array.getInt(index);
        int block24Yp3 = block24Yp3Array.getInt(index);
        int block24Ym0 = block24Ym0Array.getInt(index);
        int block24Ym1 = block24Ym1Array.getInt(index);
        int block24Ym2 = block24Ym2Array.getInt(index);
        int block24Ym3 = block24Ym3Array.getInt(index);
        int block24Zp0 = block24Zp0Array.getInt(index);
        int block24Zp1 = block24Zp1Array.getInt(index);
        int block24Zp2 = block24Zp2Array.getInt(index);
        int block24Zp3 = block24Zp3Array.getInt(index);
        int block24Zm0 = block24Zm0Array.getInt(index);
        int block24Zm1 = block24Zm1Array.getInt(index);
        int block24Zm2 = block24Zm2Array.getInt(index);
        int block24Zm3 = block24Zm3Array.getInt(index);

        int block25PositionAndFaceMask = block25PfArray.getInt(index);
        int block25Xp0 = block25Xp0Array.getInt(index);
        int block25Xp1 = block25Xp1Array.getInt(index);
        int block25Xp2 = block25Xp2Array.getInt(index);
        int block25Xp3 = block25Xp3Array.getInt(index);
        int block25Xm0 = block25Xm0Array.getInt(index);
        int block25Xm1 = block25Xm1Array.getInt(index);
        int block25Xm2 = block25Xm2Array.getInt(index);
        int block25Xm3 = block25Xm3Array.getInt(index);
        int block25Yp0 = block25Yp0Array.getInt(index);
        int block25Yp1 = block25Yp1Array.getInt(index);
        int block25Yp2 = block25Yp2Array.getInt(index);
        int block25Yp3 = block25Yp3Array.getInt(index);
        int block25Ym0 = block25Ym0Array.getInt(index);
        int block25Ym1 = block25Ym1Array.getInt(index);
        int block25Ym2 = block25Ym2Array.getInt(index);
        int block25Ym3 = block25Ym3Array.getInt(index);
        int block25Zp0 = block25Zp0Array.getInt(index);
        int block25Zp1 = block25Zp1Array.getInt(index);
        int block25Zp2 = block25Zp2Array.getInt(index);
        int block25Zp3 = block25Zp3Array.getInt(index);
        int block25Zm0 = block25Zm0Array.getInt(index);
        int block25Zm1 = block25Zm1Array.getInt(index);
        int block25Zm2 = block25Zm2Array.getInt(index);
        int block25Zm3 = block25Zm3Array.getInt(index);

        int block26PositionAndFaceMask = block26PfArray.getInt(index);
        int block26Xp0 = block26Xp0Array.getInt(index);
        int block26Xp1 = block26Xp1Array.getInt(index);
        int block26Xp2 = block26Xp2Array.getInt(index);
        int block26Xp3 = block26Xp3Array.getInt(index);
        int block26Xm0 = block26Xm0Array.getInt(index);
        int block26Xm1 = block26Xm1Array.getInt(index);
        int block26Xm2 = block26Xm2Array.getInt(index);
        int block26Xm3 = block26Xm3Array.getInt(index);
        int block26Yp0 = block26Yp0Array.getInt(index);
        int block26Yp1 = block26Yp1Array.getInt(index);
        int block26Yp2 = block26Yp2Array.getInt(index);
        int block26Yp3 = block26Yp3Array.getInt(index);
        int block26Ym0 = block26Ym0Array.getInt(index);
        int block26Ym1 = block26Ym1Array.getInt(index);
        int block26Ym2 = block26Ym2Array.getInt(index);
        int block26Ym3 = block26Ym3Array.getInt(index);
        int block26Zp0 = block26Zp0Array.getInt(index);
        int block26Zp1 = block26Zp1Array.getInt(index);
        int block26Zp2 = block26Zp2Array.getInt(index);
        int block26Zp3 = block26Zp3Array.getInt(index);
        int block26Zm0 = block26Zm0Array.getInt(index);
        int block26Zm1 = block26Zm1Array.getInt(index);
        int block26Zm2 = block26Zm2Array.getInt(index);
        int block26Zm3 = block26Zm3Array.getInt(index);

        int block27PositionAndFaceMask = block27PfArray.getInt(index);
        int block27Xp0 = block27Xp0Array.getInt(index);
        int block27Xp1 = block27Xp1Array.getInt(index);
        int block27Xp2 = block27Xp2Array.getInt(index);
        int block27Xp3 = block27Xp3Array.getInt(index);
        int block27Xm0 = block27Xm0Array.getInt(index);
        int block27Xm1 = block27Xm1Array.getInt(index);
        int block27Xm2 = block27Xm2Array.getInt(index);
        int block27Xm3 = block27Xm3Array.getInt(index);
        int block27Yp0 = block27Yp0Array.getInt(index);
        int block27Yp1 = block27Yp1Array.getInt(index);
        int block27Yp2 = block27Yp2Array.getInt(index);
        int block27Yp3 = block27Yp3Array.getInt(index);
        int block27Ym0 = block27Ym0Array.getInt(index);
        int block27Ym1 = block27Ym1Array.getInt(index);
        int block27Ym2 = block27Ym2Array.getInt(index);
        int block27Ym3 = block27Ym3Array.getInt(index);
        int block27Zp0 = block27Zp0Array.getInt(index);
        int block27Zp1 = block27Zp1Array.getInt(index);
        int block27Zp2 = block27Zp2Array.getInt(index);
        int block27Zp3 = block27Zp3Array.getInt(index);
        int block27Zm0 = block27Zm0Array.getInt(index);
        int block27Zm1 = block27Zm1Array.getInt(index);
        int block27Zm2 = block27Zm2Array.getInt(index);
        int block27Zm3 = block27Zm3Array.getInt(index);

        int block28PositionAndFaceMask = block28PfArray.getInt(index);
        int block28Xp0 = block28Xp0Array.getInt(index);
        int block28Xp1 = block28Xp1Array.getInt(index);
        int block28Xp2 = block28Xp2Array.getInt(index);
        int block28Xp3 = block28Xp3Array.getInt(index);
        int block28Xm0 = block28Xm0Array.getInt(index);
        int block28Xm1 = block28Xm1Array.getInt(index);
        int block28Xm2 = block28Xm2Array.getInt(index);
        int block28Xm3 = block28Xm3Array.getInt(index);
        int block28Yp0 = block28Yp0Array.getInt(index);
        int block28Yp1 = block28Yp1Array.getInt(index);
        int block28Yp2 = block28Yp2Array.getInt(index);
        int block28Yp3 = block28Yp3Array.getInt(index);
        int block28Ym0 = block28Ym0Array.getInt(index);
        int block28Ym1 = block28Ym1Array.getInt(index);
        int block28Ym2 = block28Ym2Array.getInt(index);
        int block28Ym3 = block28Ym3Array.getInt(index);
        int block28Zp0 = block28Zp0Array.getInt(index);
        int block28Zp1 = block28Zp1Array.getInt(index);
        int block28Zp2 = block28Zp2Array.getInt(index);
        int block28Zp3 = block28Zp3Array.getInt(index);
        int block28Zm0 = block28Zm0Array.getInt(index);
        int block28Zm1 = block28Zm1Array.getInt(index);
        int block28Zm2 = block28Zm2Array.getInt(index);
        int block28Zm3 = block28Zm3Array.getInt(index);

        int block29PositionAndFaceMask = block29PfArray.getInt(index);
        int block29Xp0 = block29Xp0Array.getInt(index);
        int block29Xp1 = block29Xp1Array.getInt(index);
        int block29Xp2 = block29Xp2Array.getInt(index);
        int block29Xp3 = block29Xp3Array.getInt(index);
        int block29Xm0 = block29Xm0Array.getInt(index);
        int block29Xm1 = block29Xm1Array.getInt(index);
        int block29Xm2 = block29Xm2Array.getInt(index);
        int block29Xm3 = block29Xm3Array.getInt(index);
        int block29Yp0 = block29Yp0Array.getInt(index);
        int block29Yp1 = block29Yp1Array.getInt(index);
        int block29Yp2 = block29Yp2Array.getInt(index);
        int block29Yp3 = block29Yp3Array.getInt(index);
        int block29Ym0 = block29Ym0Array.getInt(index);
        int block29Ym1 = block29Ym1Array.getInt(index);
        int block29Ym2 = block29Ym2Array.getInt(index);
        int block29Ym3 = block29Ym3Array.getInt(index);
        int block29Zp0 = block29Zp0Array.getInt(index);
        int block29Zp1 = block29Zp1Array.getInt(index);
        int block29Zp2 = block29Zp2Array.getInt(index);
        int block29Zp3 = block29Zp3Array.getInt(index);
        int block29Zm0 = block29Zm0Array.getInt(index);
        int block29Zm1 = block29Zm1Array.getInt(index);
        int block29Zm2 = block29Zm2Array.getInt(index);
        int block29Zm3 = block29Zm3Array.getInt(index);

        int block30PositionAndFaceMask = block30PfArray.getInt(index);
        int block30Xp0 = block30Xp0Array.getInt(index);
        int block30Xp1 = block30Xp1Array.getInt(index);
        int block30Xp2 = block30Xp2Array.getInt(index);
        int block30Xp3 = block30Xp3Array.getInt(index);
        int block30Xm0 = block30Xm0Array.getInt(index);
        int block30Xm1 = block30Xm1Array.getInt(index);
        int block30Xm2 = block30Xm2Array.getInt(index);
        int block30Xm3 = block30Xm3Array.getInt(index);
        int block30Yp0 = block30Yp0Array.getInt(index);
        int block30Yp1 = block30Yp1Array.getInt(index);
        int block30Yp2 = block30Yp2Array.getInt(index);
        int block30Yp3 = block30Yp3Array.getInt(index);
        int block30Ym0 = block30Ym0Array.getInt(index);
        int block30Ym1 = block30Ym1Array.getInt(index);
        int block30Ym2 = block30Ym2Array.getInt(index);
        int block30Ym3 = block30Ym3Array.getInt(index);
        int block30Zp0 = block30Zp0Array.getInt(index);
        int block30Zp1 = block30Zp1Array.getInt(index);
        int block30Zp2 = block30Zp2Array.getInt(index);
        int block30Zp3 = block30Zp3Array.getInt(index);
        int block30Zm0 = block30Zm0Array.getInt(index);
        int block30Zm1 = block30Zm1Array.getInt(index);
        int block30Zm2 = block30Zm2Array.getInt(index);
        int block30Zm3 = block30Zm3Array.getInt(index);

        int block31PositionAndFaceMask = block31PfArray.getInt(index);
        int block31Xp0 = block31Xp0Array.getInt(index);
        int block31Xp1 = block31Xp1Array.getInt(index);
        int block31Xp2 = block31Xp2Array.getInt(index);
        int block31Xp3 = block31Xp3Array.getInt(index);
        int block31Xm0 = block31Xm0Array.getInt(index);
        int block31Xm1 = block31Xm1Array.getInt(index);
        int block31Xm2 = block31Xm2Array.getInt(index);
        int block31Xm3 = block31Xm3Array.getInt(index);
        int block31Yp0 = block31Yp0Array.getInt(index);
        int block31Yp1 = block31Yp1Array.getInt(index);
        int block31Yp2 = block31Yp2Array.getInt(index);
        int block31Yp3 = block31Yp3Array.getInt(index);
        int block31Ym0 = block31Ym0Array.getInt(index);
        int block31Ym1 = block31Ym1Array.getInt(index);
        int block31Ym2 = block31Ym2Array.getInt(index);
        int block31Ym3 = block31Ym3Array.getInt(index);
        int block31Zp0 = block31Zp0Array.getInt(index);
        int block31Zp1 = block31Zp1Array.getInt(index);
        int block31Zp2 = block31Zp2Array.getInt(index);
        int block31Zp3 = block31Zp3Array.getInt(index);
        int block31Zm0 = block31Zm0Array.getInt(index);
        int block31Zm1 = block31Zm1Array.getInt(index);
        int block31Zm2 = block31Zm2Array.getInt(index);
        int block31Zm3 = block31Zm3Array.getInt(index);

        // meshlet header
        byteBuffer.putFloat(normalX);
        byteBuffer.putFloat(normalY);
        byteBuffer.putFloat(normalZ);
        byteBuffer.putFloat(0f); // padding
        byteBuffer.putInt(chunkPosX);
        byteBuffer.putInt(chunkPosY);
        byteBuffer.putInt(chunkPosZ);
        byteBuffer.putInt(blockCount);

        // 32 blocks

        // ---- Block 0 ----
        byteBuffer.putInt(block0Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block0Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block0Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block0Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block0Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block0Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block0Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block0Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block0Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block0Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block0Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block0Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block0Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block0Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block0Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block0Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block0Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block0Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block0Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block0Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block0Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block0Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block0Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block0Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block0PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 1 ----
        byteBuffer.putInt(block1Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block1Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block1Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block1Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block1Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block1Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block1Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block1Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block1Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block1Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block1Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block1Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block1Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block1Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block1Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block1Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block1Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block1Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block1Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block1Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block1Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block1Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block1Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block1Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block1PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 2 ----
        byteBuffer.putInt(block2Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block2Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block2Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block2Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block2Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block2Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block2Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block2Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block2Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block2Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block2Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block2Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block2Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block2Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block2Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block2Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block2Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block2Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block2Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block2Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block2Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block2Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block2Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block2Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block2PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 3 ----
        byteBuffer.putInt(block3Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block3Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block3Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block3Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block3Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block3Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block3Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block3Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block3Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block3Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block3Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block3Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block3Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block3Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block3Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block3Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block3Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block3Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block3Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block3Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block3Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block3Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block3Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block3Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block3PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 4 ----
        byteBuffer.putInt(block4Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block4Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block4Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block4Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block4Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block4Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block4Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block4Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block4Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block4Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block4Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block4Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block4Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block4Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block4Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block4Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block4Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block4Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block4Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block4Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block4Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block4Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block4Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block4Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block4PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 5 ----
        byteBuffer.putInt(block5Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block5Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block5Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block5Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block5Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block5Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block5Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block5Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block5Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block5Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block5Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block5Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block5Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block5Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block5Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block5Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block5Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block5Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block5Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block5Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block5Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block5Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block5Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block5Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block5PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 6 ----
        byteBuffer.putInt(block6Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block6Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block6Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block6Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block6Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block6Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block6Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block6Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block6Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block6Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block6Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block6Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block6Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block6Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block6Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block6Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block6Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block6Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block6Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block6Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block6Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block6Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block6Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block6Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block6PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 7 ----
        byteBuffer.putInt(block7Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block7Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block7Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block7Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block7Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block7Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block7Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block7Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block7Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block7Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block7Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block7Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block7Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block7Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block7Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block7Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block7Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block7Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block7Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block7Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block7Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block7Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block7Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block7Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block7PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 8 ----
        byteBuffer.putInt(block8Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block8Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block8Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block8Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block8Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block8Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block8Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block8Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block8Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block8Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block8Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block8Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block8Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block8Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block8Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block8Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block8Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block8Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block8Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block8Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block8Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block8Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block8Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block8Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block8PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 9 ----
        byteBuffer.putInt(block9Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block9Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block9Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block9Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block9Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block9Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block9Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block9Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block9Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block9Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block9Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block9Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block9Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block9Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block9Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block9Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block9Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block9Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block9Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block9Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block9Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block9Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block9Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block9Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block9PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 10 ----
        byteBuffer.putInt(block10Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block10Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block10Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block10Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block10Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block10Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block10Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block10Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block10Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block10Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block10Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block10Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block10Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block10Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block10Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block10Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block10Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block10Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block10Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block10Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block10Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block10Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block10Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block10Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block10PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 11 ----
        byteBuffer.putInt(block11Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block11Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block11Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block11Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block11Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block11Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block11Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block11Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block11Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block11Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block11Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block11Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block11Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block11Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block11Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block11Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block11Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block11Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block11Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block11Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block11Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block11Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block11Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block11Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block11PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 12 ----
        byteBuffer.putInt(block12Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block12Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block12Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block12Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block12Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block12Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block12Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block12Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block12Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block12Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block12Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block12Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block12Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block12Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block12Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block12Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block12Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block12Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block12Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block12Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block12Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block12Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block12Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block12Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block12PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 13 ----
        byteBuffer.putInt(block13Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block13Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block13Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block13Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block13Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block13Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block13Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block13Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block13Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block13Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block13Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block13Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block13Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block13Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block13Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block13Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block13Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block13Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block13Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block13Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block13Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block13Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block13Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block13Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block13PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 14 ----
        byteBuffer.putInt(block14Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block14Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block14Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block14Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block14Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block14Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block14Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block14Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block14Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block14Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block14Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block14Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block14Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block14Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block14Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block14Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block14Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block14Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block14Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block14Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block14Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block14Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block14Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block14Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block14PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 15 ----
        byteBuffer.putInt(block15Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block15Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block15Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block15Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block15Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block15Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block15Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block15Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block15Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block15Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block15Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block15Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block15Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block15Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block15Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block15Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block15Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block15Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block15Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block15Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block15Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block15Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block15Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block15Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block15PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 16 ----
        byteBuffer.putInt(block16Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block16Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block16Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block16Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block16Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block16Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block16Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block16Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block16Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block16Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block16Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block16Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block16Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block16Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block16Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block16Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block16Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block16Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block16Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block16Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block16Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block16Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block16Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block16Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block16PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 17 ----
        byteBuffer.putInt(block17Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block17Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block17Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block17Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block17Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block17Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block17Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block17Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block17Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block17Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block17Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block17Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block17Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block17Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block17Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block17Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block17Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block17Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block17Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block17Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block17Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block17Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block17Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block17Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block17PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 18 ----
        byteBuffer.putInt(block18Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block18Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block18Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block18Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block18Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block18Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block18Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block18Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block18Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block18Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block18Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block18Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block18Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block18Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block18Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block18Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block18Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block18Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block18Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block18Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block18Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block18Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block18Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block18Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block18PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 19 ----
        byteBuffer.putInt(block19Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block19Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block19Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block19Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block19Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block19Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block19Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block19Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block19Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block19Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block19Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block19Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block19Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block19Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block19Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block19Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block19Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block19Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block19Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block19Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block19Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block19Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block19Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block19Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block19PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 20 ----
        byteBuffer.putInt(block20Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block20Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block20Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block20Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block20Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block20Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block20Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block20Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block20Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block20Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block20Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block20Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block20Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block20Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block20Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block20Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block20Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block20Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block20Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block20Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block20Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block20Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block20Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block20Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block20PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 21 ----
        byteBuffer.putInt(block21Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block21Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block21Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block21Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block21Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block21Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block21Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block21Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block21Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block21Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block21Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block21Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block21Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block21Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block21Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block21Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block21Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block21Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block21Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block21Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block21Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block21Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block21Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block21Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block21PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 22 ----
        byteBuffer.putInt(block22Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block22Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block22Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block22Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block22Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block22Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block22Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block22Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block22Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block22Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block22Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block22Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block22Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block22Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block22Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block22Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block22Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block22Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block22Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block22Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block22Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block22Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block22Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block22Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block22PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 23 ----
        byteBuffer.putInt(block23Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block23Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block23Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block23Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block23Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block23Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block23Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block23Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block23Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block23Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block23Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block23Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block23Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block23Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block23Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block23Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block23Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block23Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block23Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block23Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block23Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block23Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block23Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block23Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block23PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 24 ----
        byteBuffer.putInt(block24Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block24Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block24Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block24Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block24Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block24Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block24Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block24Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block24Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block24Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block24Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block24Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block24Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block24Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block24Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block24Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block24Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block24Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block24Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block24Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block24Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block24Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block24Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block24Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block24PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 25 ----
        byteBuffer.putInt(block25Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block25Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block25Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block25Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block25Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block25Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block25Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block25Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block25Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block25Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block25Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block25Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block25Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block25Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block25Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block25Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block25Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block25Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block25Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block25Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block25Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block25Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block25Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block25Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block25PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 26 ----
        byteBuffer.putInt(block26Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block26Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block26Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block26Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block26Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block26Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block26Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block26Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block26Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block26Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block26Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block26Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block26Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block26Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block26Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block26Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block26Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block26Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block26Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block26Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block26Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block26Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block26Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block26Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block26PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 27 ----
        byteBuffer.putInt(block27Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block27Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block27Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block27Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block27Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block27Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block27Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block27Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block27Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block27Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block27Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block27Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block27Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block27Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block27Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block27Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block27Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block27Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block27Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block27Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block27Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block27Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block27Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block27Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block27PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 28 ----
        byteBuffer.putInt(block28Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block28Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block28Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block28Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block28Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block28Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block28Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block28Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block28Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block28Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block28Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block28Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block28Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block28Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block28Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block28Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block28Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block28Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block28Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block28Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block28Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block28Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block28Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block28Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block28PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 29 ----
        byteBuffer.putInt(block29Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block29Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block29Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block29Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block29Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block29Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block29Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block29Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block29Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block29Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block29Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block29Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block29Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block29Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block29Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block29Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block29Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block29Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block29Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block29Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block29Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block29Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block29Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block29Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block29PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 30 ----
        byteBuffer.putInt(block30Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block30Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block30Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block30Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block30Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block30Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block30Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block30Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block30Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block30Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block30Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block30Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block30Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block30Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block30Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block30Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block30Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block30Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block30Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block30Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block30Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block30Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block30Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block30Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block30PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding

        // ---- Block 31 ----
        byteBuffer.putInt(block31Xp0); // xPlusFaceTexCoord0
        byteBuffer.putInt(block31Xp1); // xPlusFaceTexCoord1
        byteBuffer.putInt(block31Xp2); // xPlusFaceTexCoord2
        byteBuffer.putInt(block31Xp3); // xPlusFaceTexCoord3
        byteBuffer.putInt(block31Xm0); // xMinusFaceTexCoord0
        byteBuffer.putInt(block31Xm1); // xMinusFaceTexCoord1
        byteBuffer.putInt(block31Xm2); // xMinusFaceTexCoord2
        byteBuffer.putInt(block31Xm3); // xMinusFaceTexCoord3
        byteBuffer.putInt(block31Yp0); // yPlusFaceTexCoord0
        byteBuffer.putInt(block31Yp1); // yPlusFaceTexCoord1
        byteBuffer.putInt(block31Yp2); // yPlusFaceTexCoord2
        byteBuffer.putInt(block31Yp3); // yPlusFaceTexCoord3
        byteBuffer.putInt(block31Ym0); // yMinusFaceTexCoord0
        byteBuffer.putInt(block31Ym1); // yMinusFaceTexCoord1
        byteBuffer.putInt(block31Ym2); // yMinusFaceTexCoord2
        byteBuffer.putInt(block31Ym3); // yMinusFaceTexCoord3
        byteBuffer.putInt(block31Zp0); // zPlusFaceTexCoord0
        byteBuffer.putInt(block31Zp1); // zPlusFaceTexCoord1
        byteBuffer.putInt(block31Zp2); // zPlusFaceTexCoord2
        byteBuffer.putInt(block31Zp3); // zPlusFaceTexCoord3
        byteBuffer.putInt(block31Zm0); // zMinusFaceTexCoord0
        byteBuffer.putInt(block31Zm1); // zMinusFaceTexCoord1
        byteBuffer.putInt(block31Zm2); // zMinusFaceTexCoord2
        byteBuffer.putInt(block31Zm3); // zMinusFaceTexCoord3
        byteBuffer.putInt(block31PositionAndFaceMask);
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
        byteBuffer.putInt(0); // padding
    }

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(MeshletComponent.class);
    }

    @Override
    public int estimateWorkload(int index) {
        return 32 * 2;
    }
}
