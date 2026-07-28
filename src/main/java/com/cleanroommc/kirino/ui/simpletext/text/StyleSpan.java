package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

/**
 * @param start Codepoint index (inclusive)
 * @param end Codepoint index (exclusive)
 */
record StyleSpan(int start, int end, @NonNull TextStyle style) {
}
