package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.command.TextCommandList;

public interface SimpleTextConsumer {
    void consume(TextCommandList commandList);
}
