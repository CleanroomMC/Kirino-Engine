package com.cleanroommc.kirino.engine.render.core.framebuffer;

import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public final class ResolutionContainer {

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private int width;
    private int height;

    private final BiConsumer<Integer, Integer> resizeCallback;
    private final BiConsumer<Integer, Integer> synchronizeCallback;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ResolutionContainer(
            @Nullable BiConsumer<Integer, Integer> resizeCallback,
            @Nullable BiConsumer<Integer, Integer> synchronizeCallback) {

        width = MINECRAFT.displayWidth;
        height = MINECRAFT.displayHeight;
        this.resizeCallback = resizeCallback;
        this.synchronizeCallback = synchronizeCallback;
    }

    /**
     * Update screen resolution if needed and trigger {@link #resizeCallback} if needed.
     */
    public void update() {
        if (width != MINECRAFT.displayWidth || height != MINECRAFT.displayHeight) {
            width = MINECRAFT.displayWidth;
            height = MINECRAFT.displayHeight;
            if (resizeCallback != null) {
                resizeCallback.accept(width, height);
            }
        }
    }

    /**
     * Force update screen resolution (even if there's no diff) and trigger {@link #synchronizeCallback} if needed.
     */
    public void synchronize() {
        width = MINECRAFT.displayWidth;
        height = MINECRAFT.displayHeight;
        if (synchronizeCallback != null) {
            synchronizeCallback.accept(width, height);
        }
    }
}
