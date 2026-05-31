package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;

public interface SimpleTextConsumer {
    void consume(TextCommandList commandList);
}
