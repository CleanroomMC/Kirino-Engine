package com.cleanroommc.kirino.utils;

import org.jspecify.annotations.NonNull;

public class MutableChainString implements CharSequence {

    private final char[] buffer;
    private int length = 0;

    public MutableChainString(int maxCapacity) {
        this.buffer = new char[maxCapacity];
    }

    public MutableChainString set(String... chain) {
        this.length = 0;
        if (chain == null || chain.length == 0) {
            append("\"<empty>\"");
            return this;
        }

        append("\".");
        for (int i = 0; i < chain.length; i++) {
            append(chain[i]);
            if (i < chain.length - 1) {
                append('.');
            }
        }
        append('"');
        return this;
    }

    private void append(String str) {
        int strLen = str.length();
        str.getChars(0, strLen, buffer, length);
        length += strLen;
    }

    private void append(char c) {
        buffer[length++] = c;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public char charAt(int index) {
        return buffer[index];
    }

    @Override
    public @NonNull CharSequence subSequence(int start, int end) {
        throw new UnsupportedOperationException("Zero-allocation policy");
    }

    @Override
    public @NonNull String toString() {
        return new String(buffer, 0, length);
    }
}
