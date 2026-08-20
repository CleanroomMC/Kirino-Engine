package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

final class StyledTextBuilder {

    static final class Result {

        private final String pureText;
        private final List<StyleSpan> spans;
        private final int codepointCount;

        private Result(
                @NonNull String pureText,
                @NonNull List<@NonNull StyleSpan> spans,
                int codepointCount) {

            Preconditions.checkNotNull(pureText);
            Preconditions.checkNotNull(spans);
            for (StyleSpan span : spans) {
                Preconditions.checkNotNull(span);
            }

            this.pureText = pureText;
            this.spans = spans;
            this.codepointCount = codepointCount;
        }

        @NonNull
        String pureText() {
            return pureText;
        }

        @NonNull
        List<@NonNull StyleSpan> spans() {
            return spans;
        }

        int codepointCount() {
            return codepointCount;
        }
    }

    private final StringBuilder pureText = new StringBuilder();
    private final List<StyleSpan> spans = new ArrayList<>();

    private final TextStyle defaultStyle;
    private TextStyle currentStyle;

    private int codepointCursor;
    private int runStart;

    private boolean finished = false;

    StyledTextBuilder(@NonNull TextStyle defaultStyle) {
        Preconditions.checkNotNull(defaultStyle);

        this.defaultStyle = defaultStyle;
        this.currentStyle = defaultStyle;
    }

    @NonNull
    TextStyle defaultStyle() {
        return defaultStyle;
    }

    @NonNull
    TextStyle currentStyle() {
        return currentStyle;
    }

    void style(@NonNull TextStyle style) {
        Preconditions.checkNotNull(style);
        Preconditions.checkState(!finished,
                "StyledTextBuilder has already been finished.");

        if (currentStyle.equals(style)) {
            return;
        }

        flushRun();

        currentStyle = style;
    }

    void resetStyle() {
        style(defaultStyle);
    }

    void appendCodepoint(int codepoint) {
        Preconditions.checkState(!finished,
                "StyledTextBuilder has already been finished.");
        Preconditions.checkArgument(Character.isValidCodePoint(codepoint),
                "Invalid Unicode codepoint: %s", codepoint);

        pureText.appendCodePoint(codepoint);
        codepointCursor++;
    }

    /**
     * @param start UFT-16 index
     * @param end UFT-16 index
     */
    void appendLiteral(@NonNull String text, int start, int end) {
        Preconditions.checkNotNull(text);
        Preconditions.checkState(!finished,
                "StyledTextBuilder has already been finished.");
        Preconditions.checkPositionIndexes(start, end, text.length());

        pureText.append(text, start, end);
        codepointCursor += text.codePointCount(start, end);
    }

    @NonNull
    Result finish() {
        Preconditions.checkState(!finished,
                "StyledTextBuilder has already been finished.");

        finished = true;

        flushRun();
        validate();

        return new Result(pureText.toString(), List.copyOf(spans), codepointCursor);
    }

    private void flushRun() {
        if (runStart == codepointCursor) {
            return;
        }

        spans.add(new StyleSpan(
                runStart,
                codepointCursor,
                currentStyle));

        runStart = codepointCursor;
    }

    private void validate() {
        if (codepointCursor == 0) {
            Preconditions.checkState(spans.isEmpty(),
                    "Empty StyledText must not contain spans.");

            return;
        }

        Preconditions.checkState(!spans.isEmpty(),
                "Non-empty StyledText must contain spans.");

        Preconditions.checkState(spans.getFirst().start() == 0,
                "First span must start at codepoint 0. (got = %s)",
                spans.getFirst().start());

        Preconditions.checkState(spans.getLast().end() == codepointCursor,
                "Last span must end at \"codepointCount\"=%s. (got = %s)",
                spans.getLast().end());

        for (int i = 1; i < spans.size(); i++) {
            Preconditions.checkState(spans.get(i - 1).end() == spans.get(i).start(),
                    "StyledText spans must be contiguous. Span %s ends at codepoint %s, but span %s starts at codepoint %s.",
                    i - 1,
                    spans.get(i - 1).end(),
                    i,
                    spans.get(i).start());
        }
    }
}
