package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFRenderOptions;
import com.cleanroommc.kirino.ui.transform.UiTransformContext;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public interface SimpleTextConsumer extends AutoCloseable {

    void consume(@NonNull TextCommandList commandList);

    default void consume(
            @NonNull TextCommandList commandList,
            @NonNull UiTransformContext context) {

        validateTransform(context);
        consume(commandList);
    }

    default void consume(
            @NonNull TextCommandList commandList,
            @NonNull UiTransformContext context,
            @NonNull SDFRenderOptions options) {

        validateRenderOptions(options);
        consume(commandList, context);
    }

    default void validateRenderOptions(@NonNull SDFRenderOptions options) {
        if (!SDFRenderOptions.DEFAULT.equals(Preconditions.checkNotNull(options))) {
            throw new UnsupportedOperationException("This text consumer does not support SDF rendering options.");
        }
    }

    default void validateTransform(@NonNull UiTransformContext context) {
        if (!Preconditions.checkNotNull(context).isDefault()) {
            throw new UnsupportedOperationException("This text consumer does not support UI transform contexts.");
        }
    }
}
