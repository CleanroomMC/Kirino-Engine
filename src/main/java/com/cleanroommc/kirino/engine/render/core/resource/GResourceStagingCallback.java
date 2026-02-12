package com.cleanroommc.kirino.engine.render.core.resource;

import com.cleanroommc.kirino.engine.render.core.resource.mapping.Obj2BufMorphism;
import com.cleanroommc.kirino.engine.render.core.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.core.staging.StagingCallback;
import com.cleanroommc.kirino.engine.render.core.staging.StagingContext;
import com.cleanroommc.kirino.engine.render.core.staging.handle.*;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;

import java.nio.ByteBuffer;
import java.util.*;

public final class GResourceStagingCallback implements StagingCallback {
    private final GraphicResourceManager graphicResourceManager;

    public GResourceStagingCallback(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    @SuppressWarnings("unchecked")
    private void handleMeshTickets(StagingContext context, Map<GResourceType, Map<String, GResourceTicket<?, ?>>> resourceTickets) {
        Map<String, GResourceTicket<?, ?>> map = resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());

        // group by AttributeLayout; combine vbo and ebo to bulk upload
        Map<AttributeLayout, List<GResourceTicket<MeshPayload, MeshReceipt>>> temporaryTicketsToUpload = new HashMap<>();
        List<GResourceTicket<MeshPayload, MeshReceipt>> persistentTicketsToUpload = new ArrayList<>();

        List<String> entriesToRemove = new ArrayList<>();

        for (Map.Entry<String, GResourceTicket<?, ?>> entry : map.entrySet()) {
            GResourceTicket<MeshPayload, MeshReceipt> ticket = (GResourceTicket<MeshPayload, MeshReceipt>) entry.getValue();

            if (ticket.isExpired()) {
                entriesToRemove.add(entry.getKey());
                ticket.expire();
                continue;
            }

            // all temporary handles will be disposed every frame
            if (ticket.uploadStrategy == UploadStrategy.TEMPORARY) {
                // we don't release payloads for temporary tickets
                // so their payload is now cpu only
                ticket.status = GResourceStatus.CPU_ONLY;
            }

            if (ticket.status == GResourceStatus.CPU_ONLY) {
                if (ticket.uploadStrategy == UploadStrategy.PERSISTENT) {
                    persistentTicketsToUpload.add(ticket);
                    ticket.status = GResourceStatus.GPU_READY;
                } else if (ticket.uploadStrategy == UploadStrategy.TEMPORARY) {
                    temporaryTicketsToUpload.computeIfAbsent(
                            ticket.payload.getPayload().attributeLayout, k -> new ArrayList<>()).add(ticket);
                    ticket.status = GResourceStatus.GPU_READY;
                }
            }

            ticket.tickFrame();
        }

        // remove expired tickets
        entriesToRemove.forEach(map::remove);

        // upload persistent tickets
        for (GResourceTicket<MeshPayload, MeshReceipt> ticket : persistentTicketsToUpload) {
            MeshPayload meshPayload = ticket.payload.getPayload();
            MeshReceipt meshReceipt = ticket.receipt.getReceipt();

            PersistentVBOHandle vboHandle = context.getPersistentVBO("default", meshPayload.vboByteBuffer.remaining()).write(0, meshPayload.vboByteBuffer);
            PersistentEBOHandle eboHandle = context.getPersistentEBO("default", meshPayload.eboByteBuffer.remaining()).write(0, meshPayload.eboByteBuffer);
            PersistentVAOHandle vaoHandle = context.getPersistentVAO(
                    meshPayload.attributeLayout,
                    "default",
                    "default",
                    eboHandle.getSlotPageIndex(),
                    vboHandle.getSlotPageIndex());

            final Runnable vboRelease = vboHandle.getSlotReleaseAction();
            final Runnable eboRelease = eboHandle.getSlotReleaseAction();

            ticket.setExpireCallback(() -> {
                vboRelease.run();
                eboRelease.run();
            });

            meshReceipt.vao = vaoHandle.getVaoID();
            meshReceipt.eboOffset = eboHandle.getSlotOffset();
            meshReceipt.eboLength = eboHandle.getSlotSize();
            meshReceipt.baseVertex = vboHandle.getSlotOffset() / meshPayload.attributeLayout.getFirstStride().getSize();

            // unlike temporary tickets; release payload immediately
            meshPayload.release();
        }

        // upload temporary tickets
        for (Map.Entry<AttributeLayout, List<GResourceTicket<MeshPayload, MeshReceipt>>> entry : temporaryTicketsToUpload.entrySet()) {
            // case 1: simply upload
            if (entry.getValue().size() == 1) {
                MeshPayload meshPayload = entry.getValue().getFirst().payload.getPayload();
                MeshReceipt meshReceipt = entry.getValue().getFirst().receipt.getReceipt();

                TemporaryVBOHandle vboHandle = context.getTemporaryVBO(meshPayload.vboByteBuffer.remaining()).write(0, meshPayload.vboByteBuffer);
                TemporaryEBOHandle eboHandle = context.getTemporaryEBO(meshPayload.eboByteBuffer.remaining()).write(0, meshPayload.eboByteBuffer);
                meshReceipt.vao = context.getTemporaryVAO(meshPayload.attributeLayout, eboHandle, vboHandle).getVaoID();
                meshReceipt.eboOffset = 0;
                meshReceipt.eboLength = meshPayload.eboByteBuffer.remaining();

                // don't release payload for temporary tickets

                continue;
            }

            // case 2: bulk upload
            int totalVboSize = 0;
            int totalEboSize = 0;

            for (GResourceTicket<MeshPayload, MeshReceipt> ticket : entry.getValue()) {
                totalVboSize += ticket.payload.getPayload().vboByteBuffer.remaining();
                totalEboSize += ticket.payload.getPayload().eboByteBuffer.remaining();
            }

            Obj2BufMorphism vboMorphism = (new Obj2BufMorphism(totalVboSize)).
                    update((domain) -> {
                        for (GResourceTicket<MeshPayload, MeshReceipt> ticket : entry.getValue()) {
                            domain.add(new SpaceItem(ticket.payload.getPayload().vboByteBuffer.remaining(), ticket.payload.getPayload().vboByteBuffer));
                        }
                    }).build();
            Obj2BufMorphism eboMorphism = (new Obj2BufMorphism(totalEboSize)).
                    update((domain) -> {
                        for (GResourceTicket<MeshPayload, MeshReceipt> ticket : entry.getValue()) {
                            domain.add(new SpaceItem(ticket.payload.getPayload().eboByteBuffer.remaining(), ticket.payload.getPayload().eboByteBuffer));
                        }
                    }).build();

            TemporaryVBOHandle vboHandle = context.getTemporaryVBO(vboMorphism.getMinimizedBufferSize());
            TemporaryEBOHandle eboHandle = context.getTemporaryEBO(eboMorphism.getMinimizedBufferSize());
            TemporaryVAOHandle vaoHandle = context.getTemporaryVAO(entry.getKey(), eboHandle, vboHandle);

            Arrays.stream(vboMorphism.getCodomain()).forEach(output -> {
                SpaceItem input = vboMorphism.applyInverse(output);
                vboHandle.write(output.offset, (ByteBuffer) input.payload);
            });

            Arrays.stream(eboMorphism.getCodomain()).forEach(output -> {
                SpaceItem input = eboMorphism.applyInverse(output);
                eboHandle.write(output.offset, (ByteBuffer) input.payload);
            });

            for (GResourceTicket<MeshPayload, MeshReceipt> ticket : entry.getValue()) {
                MeshPayload meshPayload = ticket.payload.getPayload();
                MeshReceipt meshReceipt = ticket.receipt.getReceipt();

                SpaceItem vboSlice = vboMorphism.apply(new SpaceItem(meshPayload.vboByteBuffer.remaining(), meshPayload.vboByteBuffer));
                SpaceItem eboSlice = eboMorphism.apply(new SpaceItem(meshPayload.eboByteBuffer.remaining(), meshPayload.eboByteBuffer));

                meshReceipt.vao = vaoHandle.getVaoID();
                meshReceipt.eboOffset = eboSlice.offset;
                meshReceipt.eboLength = eboSlice.length;
                meshReceipt.baseVertex = vboSlice.offset / entry.getKey().getFirstStride().getSize();

                // don't release payload for temporary tickets
            }
        }
    }

    private void handleTextureTickets(StagingContext context, Map<GResourceType, Map<String, GResourceTicket<?, ?>>> resourceTickets) {
        // todo
    }

    @Override
    public void run(StagingContext context) {
        handleMeshTickets(context, graphicResourceManager.resourceTickets);
        handleTextureTickets(context, graphicResourceManager.resourceTickets);
    }
}
