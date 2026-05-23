package com.cleanroommc.kirino.simpletext.command;

import com.cleanroommc.kirino.simpletext.ST_FontObject;
import org.jspecify.annotations.NonNull;

/**
 * <p>Note: {@link TextCommandList} must be owned by a {@link ST_FontObject} owner,
 * so <code>glyphIndex</code> therefore makes sense with a given font face.</p>
 */
public final class TextCommandList {

    private final TextCommandArena arena;

    public TextCommandList(int initialCapacity) {
        arena = new TextCommandArena(initialCapacity);
    }

    public void clear() {
        arena.reset();
    }

    public int size() {
        return arena.size();
    }

    /**
     * Push a missing glyph draw command (<code>glyphIndex</code> == 0 to be exact).
     */
    public void push(float x, float y, float width, float height) {
        int i = arena.alloc();

        arena.glyphIndex.set(i, 0);

        arena.x.set(i, x);
        arena.y.set(i, y);
        arena.width.set(i, width);
        arena.height.set(i, height);
    }

    /**
     * Push a glyph draw command.
     */
    public void push(
            int glyphIndex,
            float x, float y,
            float width, float height,
            float size,
            int color,
            int hint) {

        int i = arena.alloc();

        arena.glyphIndex.set(i, glyphIndex);

        arena.x.set(i, x);
        arena.y.set(i, y);
        arena.width.set(i, width);
        arena.height.set(i, height);

        arena.size.set(i, size);

        arena.color.set(i, color);
        arena.hint.set(i, hint);
    }

    public int glyphIndex(int i) {
        return arena.glyphIndex.get(i);
    }

    public float x(int i) {
        return arena.x.get(i);
    }

    public float y(int i) {
        return arena.y.get(i);
    }

    public float width(int i) {
        return arena.width.get(i);
    }

    public float height(int i) {
        return arena.height.get(i);
    }

    public float size(int i) {
        return arena.size.get(i);
    }

    public int color(int i) {
        return arena.color.get(i);
    }

    public int hint(int i) {
        return arena.hint.get(i);
    }

    @NonNull
    public TextCommandList copy() {
        int size = size();
        TextCommandList copy = new TextCommandList(size + 1);
        for (int i = 0; i < size; i++) {
            copy.arena.alloc();
            copy.arena.glyphIndex.set(i, this.arena.glyphIndex.get(i));
            copy.arena.x.set(i, this.arena.x.get(i));
            copy.arena.y.set(i, this.arena.y.get(i));
            copy.arena.width.set(i, this.arena.width.get(i));
            copy.arena.height.set(i, this.arena.height.get(i));
            copy.arena.size.set(i, this.arena.size.get(i));
            copy.arena.color.set(i, this.arena.color.get(i));
            copy.arena.hint.set(i, this.arena.hint.get(i));
        }
        return copy;
    }
}
