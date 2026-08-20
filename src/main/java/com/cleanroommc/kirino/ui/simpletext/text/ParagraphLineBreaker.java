package com.cleanroommc.kirino.ui.simpletext.text;

import com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer;
import com.google.common.base.Preconditions;
import com.ibm.icu.text.BreakIterator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ParagraphLineBreaker {

    private final BreakIterator lineBreakIterator;
    private final BreakIterator characterBreakIterator;

    public ParagraphLineBreaker(
            @NonNull BreakIterator lineBreakIterator,
            @NonNull BreakIterator characterBreakIterator) {

        Preconditions.checkNotNull(lineBreakIterator);
        Preconditions.checkNotNull(characterBreakIterator);

        this.lineBreakIterator = lineBreakIterator;
        this.characterBreakIterator = characterBreakIterator;
    }

    /**
     * @param lineInfo It must describe the entirety of <code>text</code>
     */
    @NonNull
    public WrapBreak findWrapBreak(
            @NonNull String text,
            SimpleTextProducer.@NonNull LineInfo lineInfo,
            float maxWidth) {

        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(lineInfo);

        int cpCount = lineInfo.getCodepointCount();

        Preconditions.checkState(cpCount > 0,
                "Cannot find a wrap break for an empty line.");

        int fittingCpCount = findFittingCodepointCount(lineInfo, maxWidth);
        if (fittingCpCount == cpCount) {
            return new WrapBreak(text.length(), text.length());
        }

        int maxUtf16Offset = text.offsetByCodePoints(0, fittingCpCount);

        WrapBreak preferred = findPreferredLineBreak(text, maxUtf16Offset);
        if (preferred != null) {
            return preferred;
        }

        return findEmergencyBreak(text, maxUtf16Offset);
    }

    /**
     * It returns how many codepoints can fit within <code>maxWidth</code>.
     */
    private static int findFittingCodepointCount(
            SimpleTextProducer.@NonNull LineInfo lineInfo,
            float maxWidth) {

        int cpCount = lineInfo.getCodepointCount();

        for (int i = 0; i < cpCount; i++) {
            if (lineInfo.getProgressiveLengthAt(i) > maxWidth) {
                return i;
            }
        }

        return cpCount;
    }

    /**
     * It finds the last ICU legal line-break position that can fit inside
     * <code>maxUtf16Offset</code>.
     */
    @Nullable
    private WrapBreak findPreferredLineBreak(@NonNull String text, int maxUtf16Offset) {
        lineBreakIterator.setText(text);
        lineBreakIterator.first();

        WrapBreak best = null;

        for (int boundary = lineBreakIterator.next();
             boundary != BreakIterator.DONE;
             boundary = lineBreakIterator.next()) {

            int trimmedBoundary = trimTrailingWrapWhitespace(text, boundary);
            if (trimmedBoundary > maxUtf16Offset) {
                break;
            }

            if (boundary <= maxUtf16Offset) {
                best = new WrapBreak(boundary, boundary);
            } else if (trimmedBoundary > 0) {
                best = new WrapBreak(boundary, trimmedBoundary);
            }
        }

        return best;
    }

    /**
     * It's used when there is no proper Unicode line-break opportunity
     * like a single extremely long English word.
     */
    @NonNull
    private WrapBreak findEmergencyBreak(@NonNull String text, int maxUtf16Offset) {
        characterBreakIterator.setText(text);

        int boundary = 0;
        if (maxUtf16Offset > 0) {
            if (characterBreakIterator.isBoundary(maxUtf16Offset)) {
                boundary = maxUtf16Offset;
            } else {
                boundary = characterBreakIterator.preceding(maxUtf16Offset);
            }
        }

        if (boundary <= 0) {
            boundary = characterBreakIterator.following(0);
            if (boundary == BreakIterator.DONE) {
                boundary = Character.charCount(text.codePointAt(0));
            }
        }

        return new WrapBreak(boundary, boundary);
    }

    private static int trimTrailingWrapWhitespace(@NonNull String text, int end) {
        while (end > 0) {
            int cp = text.codePointBefore(end);
            if (!Character.isWhitespace(cp)) {
                break;
            }

            end -= Character.charCount(cp);
        }
        return end;
    }

    /**
     * It detects <code>\n</code> and <code>\r\n</code>.
     * It returns the index of the first formatting control code OR <code>-1</code> if no control code detected.
     */
    public static int findHardBreak(@NonNull String text, int fromIndex) {
        for (int i = fromIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return i;
            }
            if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                return i;
            }
        }
        return -1;
    }

    /**
     * It returns <code>1</code> for <code>\n</code> and <code>2</code> for <code>\r\n</code>, OR <code>0</code>
     * if no control code detected.
     */
    public static int hardBreakLength(@NonNull String text, int index) {
        if (text.charAt(index) == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
            return 2;
        }
        if (text.charAt(index) == '\n') {
            return 1;
        }
        return 0;
    }
}
