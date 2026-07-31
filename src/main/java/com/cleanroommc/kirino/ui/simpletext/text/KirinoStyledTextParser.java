package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

final class KirinoStyledTextParser implements StyledTextParser{

    static final KirinoStyledTextParser INSTANCE = new KirinoStyledTextParser();

    private KirinoStyledTextParser() {
    }

    @Override
    public void parse(@NonNull String rawText, @NonNull StyledTextBuilder builder) {

    }
}
