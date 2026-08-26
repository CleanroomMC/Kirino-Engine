package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;

public interface SimpleTextConsumer extends AutoCloseable {
    void consume(TextCommandList commandList);
}
