package com.cleanroommc.kirino.ui.simpletext.text;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KirinoStyledTextTest {

    @Test
    public void testStylizedText() {
        for (int i = 0; i < raws.length; i++) {
            StyledText text = new StyledText(raws[i], StyledText.Syntax.KIRINO);
            assertEquals(pureText[i], text.getPureText());
            for (int j = 0; j < indices[i].length; j++) {
                assertEquals(styles[i][j], text.get(indices[i][j]));
            }
        }
    }

    private static final String[] raws = {
            "Plain text",
            "§c=red;b;i[Red Bold Italic]§",
            "§s=2.0;o;oc=blue[Large Outlined Blue]§",
            "§u;uc=green;ush[Underlined Green Shadow]§",
            "§ss;ssc=yellow;ssr;sso;ssoc=aqua[Strikethrough]§",
            "§c=#FF00FF00;x[Hex Green Obfuscated]§",
            "§c=rgb(100;150;200)[RGB Color]§",
            "§c=argb(50;100;150;200)[ARGB Color]§",
            "§c=rgba(100;150;200;50)[RGBA Color]§",
            "§c=red[Red]§ §b[Bold]§",
            "§c=red[r§c=blue[b]§r]§",
            "\\§",
            "§c=red;b[Red Bold]§ §def[Default]§"
    };

    private static final String[] pureText = {
            "Plain text",
            "Red Bold Italic",
            "Large Outlined Blue",
            "Underlined Green Shadow",
            "Strikethrough",
            "Hex Green Obfuscated",
            "RGB Color",
            "ARGB Color",
            "RGBA Color",
            "Red Bold",
            "rbr",
            "§",
            "Red Bold Default"
    };

    private static final int[][] indices = {
            {0, 9},
            {0, 14},
            {0, 18},
            {0, 22},
            {0, 12},
            {0, 19},
            {0, 8},
            {0, 9},
            {0, 8},
            {0, 3, 4, 7},
            {0, 1, 2},
            {0},
            {0, 7, 8, 15}
    };

    private static final TextStyle[][] styles = {
            { TextStyle.DEFAULT, TextStyle.DEFAULT },
            { new TextStyle(1.0f, 0xFFFF5555, TextHintLayout.BOLD.mask() | TextHintLayout.ITALIC.mask()), new TextStyle(1.0f, 0xFFFF5555, TextHintLayout.BOLD.mask() | TextHintLayout.ITALIC.mask()) },
            { new TextStyle(2.0f, 0xFFFFFFFF, TextHintLayout.OUTLINE.mask() | TextHintLayout.OUTLINE_COLOR.set(0, 9)), new TextStyle(2.0f, 0xFFFFFFFF, TextHintLayout.OUTLINE.mask() | TextHintLayout.OUTLINE_COLOR.set(0, 9)) },
            { new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.UNDERLINE.mask() | TextHintLayout.UNDERLINE_COLOR.set(0, 10) | TextHintLayout.UNDERLINE_SHADOW_ENABLED.mask()), new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.UNDERLINE.mask() | TextHintLayout.UNDERLINE_COLOR.set(0, 10) | TextHintLayout.UNDERLINE_SHADOW_ENABLED.mask()) },
            { new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.STRIKETHROUGH.mask() | TextHintLayout.STRIKETHROUGH_COLOR.set(0, 14) | TextHintLayout.STRIKETHROUGH_ROUNDED.mask() | TextHintLayout.STRIKETHROUGH_OUTLINE_ENABLED.mask() | TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.set(0, 11)), new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.STRIKETHROUGH.mask() | TextHintLayout.STRIKETHROUGH_COLOR.set(0, 14) | TextHintLayout.STRIKETHROUGH_ROUNDED.mask() | TextHintLayout.STRIKETHROUGH_OUTLINE_ENABLED.mask() | TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.set(0, 11)) },
            { new TextStyle(1.0f, 0xFF00FF00, TextHintLayout.OBFUSCATED.mask()), new TextStyle(1.0f, 0xFF00FF00, TextHintLayout.OBFUSCATED.mask()) },
            { new TextStyle(1.0f, 0xFF6496C8, 0), new TextStyle(1.0f, 0xFF6496C8, 0) },
            { new TextStyle(1.0f, 0x326496C8, 0), new TextStyle(1.0f, 0x326496C8, 0) },
            { new TextStyle(1.0f, 0x326496C8, 0), new TextStyle(1.0f, 0x326496C8, 0) },
            { new TextStyle(1.0f, 0xFFFF5555, 0), TextStyle.DEFAULT, new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.BOLD.mask()), new TextStyle(1.0f, 0xFFFFFFFF, TextHintLayout.BOLD.mask()) },
            { new TextStyle(1.0f, 0xFFFF5555, 0), new TextStyle(1.0f, 0xFF5555FF, 0), new TextStyle(1.0f, 0xFFFF5555, 0) },
            { TextStyle.DEFAULT },
            { new TextStyle(1.0f, 0xFFFF5555, TextHintLayout.BOLD.mask()), new TextStyle(1.0f, 0xFFFF5555, TextHintLayout.BOLD.mask()), TextStyle.DEFAULT, TextStyle.DEFAULT }
    };
}
