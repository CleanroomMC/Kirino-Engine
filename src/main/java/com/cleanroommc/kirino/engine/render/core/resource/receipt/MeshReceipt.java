package com.cleanroommc.kirino.engine.render.core.resource.receipt;

import org.jspecify.annotations.NonNull;

public class MeshReceipt implements ResourceReceipt<MeshReceipt> {
    public MeshReceipt() {
    }

    public int vao;
    public int eboOffset;
    public int eboLength;
    public int baseVertex = 0;

    @NonNull
    @Override
    public MeshReceipt getReceipt() {
        return this;
    }
}
