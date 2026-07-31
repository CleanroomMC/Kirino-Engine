package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.ui.simpletext.text.StyledText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class MinecraftStyledTextTest {

    @Test
    public void testColorHints() {
        StyledText styledText = new StyledText("§0123§1asd§2qwe", StyledText.Syntax.MINECRAFT);

        assertEquals("123asdqwe", styledText.getPureText());
        assertEquals(9, styledText.getCodepointCount());
        assertNotEquals(styledText.get(0).color(), styledText.get(3).color());
        assertNotEquals(styledText.get(3).color(), styledText.get(6).color());
        assertNotEquals(styledText.get(6).color(), styledText.get(0).color());
        assertEquals(styledText.get(0).color(), styledText.get(1).color());
        assertEquals(styledText.get(3).color(), styledText.get(4).color());
        assertEquals(styledText.get(6).color(), styledText.get(7).color());
    }
}
