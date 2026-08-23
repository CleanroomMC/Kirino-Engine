package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

final class MinecraftShadowStyledTextParser extends MinecraftStyledTextParser {

    static final MinecraftShadowStyledTextParser INSTANCE = new MinecraftShadowStyledTextParser();

    private MinecraftShadowStyledTextParser() {
    }

    @Override
    protected @NonNull TextStyle transformStyle(@NonNull TextStyle style) {
        int hint = TextHintLayout.SHADOW.set(style.hint(), true);
        return style.withHint(hint);
    }
}
