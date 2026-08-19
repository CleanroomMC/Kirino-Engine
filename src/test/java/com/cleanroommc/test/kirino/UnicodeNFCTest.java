package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.ui.simpletext.text.CodepointIterator;
import com.cleanroommc.kirino.ui.simpletext.text.UnicodeNormalizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnicodeNFCTest {

    @Test
    public void testNFC() {
        String str = "e\u0301";

        assertEquals(2, CodepointIterator.count(str));
        assertEquals(1, CodepointIterator.count(UnicodeNormalizer.normalizeNFC(str)));
    }
}
