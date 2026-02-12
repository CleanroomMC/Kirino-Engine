package com.cleanroommc.kirino.engine.render.core.resource.receipt;

import org.jspecify.annotations.NonNull;

public class TextureReceipt implements ResourceReceipt<TextureReceipt> {
    public TextureReceipt() {
    }

    @NonNull
    @Override
    public TextureReceipt getReceipt() {
        return this;
    }
}
