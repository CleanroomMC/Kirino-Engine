package com.cleanroommc.kirino.engine.render.core.resource.builder;

import com.cleanroommc.kirino.engine.render.core.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.core.resource.UploadStrategy;
import com.cleanroommc.kirino.engine.render.core.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.core.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;

import java.nio.ByteBuffer;

public final class MeshTicketBuilder {
    private final String meshID;
    private final UploadStrategy uploadStrategy;
    private final int defaultLife;
    private GResourceTicket<MeshPayload, MeshReceipt> ticket;
    private boolean built = false;

    public MeshTicketBuilder(String meshID, UploadStrategy uploadStrategy, int defaultLife) {
        this.meshID = meshID;
        this.uploadStrategy = uploadStrategy;
        this.defaultLife = defaultLife;
    }

    public String getMeshID() {
        return meshID;
    }

    public UploadStrategy getUploadStrategy() {
        return uploadStrategy;
    }

    public GResourceTicket<MeshPayload, MeshReceipt> getTicket() {
        return ticket;
    }

    public boolean isBuilt() {
        return built;
    }

    public void build(ByteBuffer vboByteBuffer, ByteBuffer eboByteBuffer, AttributeLayout attributeLayout, boolean isVboByteBufferFromLwjgl, boolean isEboByteBufferFromLwjgl) {
        if (!built) {
            MeshPayload payload = new MeshPayload();
            MeshReceipt receipt = new MeshReceipt();

            payload.vboByteBuffer = vboByteBuffer;
            payload.eboByteBuffer = eboByteBuffer;
            payload.attributeLayout = attributeLayout;
            payload.isVboByteBufferFromLwjgl = isVboByteBufferFromLwjgl;
            payload.isEboByteBufferFromLwjgl = isEboByteBufferFromLwjgl;

            ticket = new GResourceTicket<>(uploadStrategy, MeshPayload.class, MeshReceipt.class, payload, receipt);
            if (defaultLife >= 1) {
                ticket.setDefaultLife(defaultLife);
            }

            built = true;
        }
    }
}
