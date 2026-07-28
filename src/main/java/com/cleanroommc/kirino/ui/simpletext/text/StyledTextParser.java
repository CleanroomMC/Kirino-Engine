package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

interface StyledTextParser {
    void parse(@NonNull String rawText, @NonNull StyledTextBuilder builder);
}
