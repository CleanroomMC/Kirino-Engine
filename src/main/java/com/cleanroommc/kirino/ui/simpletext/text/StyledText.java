package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class StyledText {

    public enum Syntax {
        KIRINO(KirinoStyledTextParser.INSTANCE),
        MINECRAFT(MinecraftStyledTextParser.INSTANCE);

        private final StyledTextParser parser;

        Syntax(StyledTextParser parser) {
            this.parser = parser;
        }
    }

    private final String pureText;
    private final int codepointCount;
    private final List<StyleSpan> spans;

    public StyledText(@NonNull String rawText) {
        this(rawText, Syntax.KIRINO);
    }

    public StyledText(@NonNull String rawText, @NonNull Syntax syntax) {
        Preconditions.checkNotNull(rawText);
        Preconditions.checkNotNull(syntax);

        StyledTextBuilder builder = new StyledTextBuilder(TextStyle.DEFAULT);
        syntax.parser.parse(rawText, builder);
        StyledTextBuilder.Result result = builder.finish();

        this.pureText = result.pureText();
        this.spans = result.spans();
        this.codepointCount = result.codepointCount();
    }

    @NonNull
    public String getPureText() {
        return pureText;
    }

    public int getCodepointCount() {
        return codepointCount;
    }

    @NonNull
    public TextStyle get(int codepointIndex) {
        Preconditions.checkElementIndex(codepointIndex, codepointCount);

        int low = 0;
        int high = spans.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            StyleSpan span = spans.get(mid);

            if (codepointIndex < span.start()) {
                high = mid - 1;
            } else if (codepointIndex >= span.end()) {
                low = mid + 1;
            } else {
                return span.style();
            }
        }

        throw new IllegalStateException("StyledText span coverage is broken.");
    }
}
