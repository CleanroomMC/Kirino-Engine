package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import com.ibm.icu.text.Normalizer2;
import org.jspecify.annotations.NonNull;

public final class UnicodeNormalizer {

    private static final Normalizer2 NFC = Normalizer2.getNFCInstance();

    private UnicodeNormalizer() {
    }

    /**
     * Normalizes the given text into Unicode Normalization Form C (NFC).
     *
     * @param text Text to normalize
     * @return NFC-normalized text
     */
    @NonNull
    public static String normalizeNFC(@NonNull CharSequence text) {
        Preconditions.checkNotNull(text);

        return NFC.normalize(text);
    }

    /**
     * Tests whether the given text is already in Unicode Normalization Form C (NFC).
     *
     * @param text Text to test
     * @return <code>true</code> if the text is already NFC-normalized
     */
    public static boolean isNFC(@NonNull CharSequence text) {
        return NFC.isNormalized(text);
    }
}
