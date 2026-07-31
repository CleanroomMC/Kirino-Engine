package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * <pre>
 *  0..4   outline color
 *  5..9   strikethrough color
 * 10..14  strikethrough outline color
 * 15      strikethrough outline enabled
 * 16      strikethrough shadow enabled
 * 17      strikethrough rounded corners
 * 18..22  underline color
 * 23      underline shadow enabled
 * 24      obfuscated
 * 25      bold
 * 26      italic
 * 27      strikethrough
 * 28      underline
 * </pre>
 *
 * @see TextColorPalette32
 */
final class TextHintABI {

    record Field(@NonNull String name, int offset, int width) {

        Field {
            Preconditions.checkNotNull(name);
            Preconditions.checkArgument(offset >= 0,
                    "Field offset must be non-negative. (\"field\"=%s, \"offset\"=%s)",
                    name,
                    offset);
            Preconditions.checkArgument(width > 0 && width < Integer.SIZE,
                    "Field width must be in range [1, 31]. (\"field\"=%s, \"width\"=%s)",
                    name,
                    width);
            Preconditions.checkArgument(offset + width <= BIT_COUNT,
                    "Field exceeds TextHint ABI. (\"field\"=%s, \"offset\"=%s, \"width\"=%s)",
                    name,
                    offset,
                    width);
        }

        int get(int hint) {
            return hint >>> offset & valueMask();
        }

        int set(int hint, int value) {
            Preconditions.checkArgument(value >= 0 && value <= valueMask(),
                    "Value does not fit field \"%s\". (\"value\"=%s, \"width\"=%s)",
                    name,
                    value,
                    width);

            return hint & ~mask() | value << offset;
        }

        int valueMask() {
            return (1 << width) - 1;
        }

        int mask() {
            return valueMask() << offset;
        }

        int endOffset() {
            return offset + width;
        }
    }

    record Bit(String name, int offset) {

        Bit {
            Preconditions.checkNotNull(name);
            Preconditions.checkArgument(offset >= 0 && offset < BIT_COUNT,
                    "Bit offset exceeds TextHint ABI. (\"bit\"=%s, \"offset\"=%s)",
                    name,
                    offset);
        }

        boolean get(int hint) {
            return (hint & mask()) != 0;
        }

        int set(int hint, boolean enabled) {
            if (enabled) {
                return hint | mask();
            } else {
                return hint & ~mask();
            }
        }

        int mask() {
            return 1 << offset;
        }
    }

    private TextHintABI() {
    }

    static final int BIT_COUNT = 29;
    static final int USED_MASK = (1 << BIT_COUNT) - 1;

    static final Field OUTLINE_COLOR = new Field("outline_color", 0, 5);
    static final Field STRIKETHROUGH_COLOR = new Field("strikethrough_color", 5, 5);
    static final Field STRIKETHROUGH_OUTLINE_COLOR = new Field("strikethrough_outline_color", 10, 5);
    static final Bit STRIKETHROUGH_OUTLINE_ENABLED = new Bit("strikethrough_outline_enabled", 15);
    static final Bit STRIKETHROUGH_SHADOW_ENABLED = new Bit("strikethrough_shadow_enabled", 16);
    static final Bit STRIKETHROUGH_ROUNDED = new Bit("strikethrough_rounded", 17);
    static final Field UNDERLINE_COLOR = new Field("underline_color", 18, 5);
    static final Bit UNDERLINE_SHADOW_ENABLED = new Bit("underline_shadow_enabled", 23);
    static final Bit OBFUSCATED = new Bit("obfuscated", 24);
    static final Bit BOLD = new Bit("bold", 25);
    static final Bit ITALIC = new Bit("italic", 26);
    static final Bit STRIKETHROUGH = new Bit("strikethrough", 27);
    static final Bit UNDERLINE = new Bit("underline", 28);

    static int reservedBits(int hint) {
        return hint & ~USED_MASK;
    }
}
