package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import org.jspecify.annotations.NonNull;

public interface SimpleTextProducer {
    void beginBatch();
    void endBatch();
    void append(@NonNull String text, float x, float y);
    @NonNull TextCommandList submit();
}
