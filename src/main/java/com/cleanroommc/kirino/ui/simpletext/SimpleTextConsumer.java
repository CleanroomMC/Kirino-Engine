package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.ui.transform.UiTransformContext;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public interface SimpleTextConsumer extends AutoCloseable {

    void consume(@NonNull TextCommandList commandList);

    default void consume(@NonNull TextCommandList commandList, @NonNull UiTransformContext context) {
        validateTransform(context);
        consume(commandList);
    }

    default void validateTransform(@NonNull UiTransformContext context) {
        if (!Preconditions.checkNotNull(context).isDefault()) {
            throw new UnsupportedOperationException("This text consumer does not support UI transform contexts.");
        }
    }
}
