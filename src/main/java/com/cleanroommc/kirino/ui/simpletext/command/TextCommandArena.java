package com.cleanroommc.kirino.ui.simpletext.command;

import com.cleanroommc.kirino.schemata.arena.SoAArena;

final class TextCommandArena {

    private final SoAArena arena;

    final SoAArena.IntColumn glyphIndex;

    final SoAArena.FloatColumn x;
    final SoAArena.FloatColumn y;
    final SoAArena.FloatColumn width;
    final SoAArena.FloatColumn height;

    final SoAArena.FloatColumn size;

    final SoAArena.IntColumn color;
    final SoAArena.IntColumn hint;

    TextCommandArena(int initialCapacity) {
        arena = new SoAArena(initialCapacity);

        glyphIndex = arena.intColumn();

        x = arena.floatColumn();
        y = arena.floatColumn();
        width = arena.floatColumn();
        height = arena.floatColumn();

        size = arena.floatColumn();

        color = arena.intColumn();
        hint = arena.intColumn();
    }

    int alloc() {
        return arena.alloc();
    }

    void reset() {
        arena.reset();
    }

    int size() {
        return arena.size();
    }
}