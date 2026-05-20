package com.cleanroommc.kirino.simpletext.backend;

import com.cleanroommc.kirino.simpletext.SimpleTextConsumer;
import com.cleanroommc.kirino.simpletext.SimpleTextContext;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;

public class DebugTextRenderer implements SimpleTextConsumer {

    private final SimpleTextContext context;

    public DebugTextRenderer(SimpleTextContext context) {
        this.context = context;
    }

    @Override
    public void consume(TextCommandList commandList) {

    }
}
