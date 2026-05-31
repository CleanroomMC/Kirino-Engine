package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.function.IntConsumer;

public class CodepointIterator implements PrimitiveIterator.OfInt {

    private final String text;
    private int index;

    public CodepointIterator(@NonNull String text) {
        Preconditions.checkNotNull(text);

        this.text = text;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < text.length();
    }

    @Override
    public int nextInt() {
        if (!hasNext()) {
            throw new NoSuchElementException("No remaining codepoints.");
        }

        int codepoint = text.codePointAt(index);

        index += Character.charCount(codepoint);

        return codepoint;
    }

    public int getIndex() {
        return index;
    }

    public void reset() {
        index = 0;
    }

    public static int count(@NonNull String text) {
        Preconditions.checkNotNull(text);

        final int[] count = new int[1];
        (new CodepointIterator(text)).forEachRemaining((IntConsumer) (codepoint) -> {
            count[0] = count[0] + 1;
        });

        return count[0];
    }
}
