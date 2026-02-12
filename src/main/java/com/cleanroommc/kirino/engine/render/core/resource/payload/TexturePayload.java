package com.cleanroommc.kirino.engine.render.core.resource.payload;

import org.jspecify.annotations.NonNull;

public class TexturePayload implements ResourcePayload<TexturePayload> {
    public TexturePayload() {
    }

    @NonNull
    @Override
    public TexturePayload getPayload() {
        return this;
    }

    private boolean released = false;

    @Override
    public void release() {
        if (!released){

            released = true;
        }
    }
}
