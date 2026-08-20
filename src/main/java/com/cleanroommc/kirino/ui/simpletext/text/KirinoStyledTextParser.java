package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;

final class KirinoStyledTextParser implements StyledTextParser{

    static final KirinoStyledTextParser INSTANCE = new KirinoStyledTextParser();

    private static final char CONTROL_PREFIX = '§';
    private static final char CONTROL_SEPARATOR = ';';
    private static final char CONTROL_ASSIGNMENT = '=';
    private static final char CONTROL_SUFFIX = '[';
    private static final char CONTROL_END = ']';
    private static final char CONTROL_ESCAPE = '\\';

    private KirinoStyledTextParser() {
    }

    @Override
    public void parse(@NonNull String rawText, @NonNull StyledTextBuilder builder) {
        Stack<TextStyle> styleStack = new ObjectArrayList<>();
        StringBuilder internalBuilder = new StringBuilder();
        TextStyle curr = builder.defaultStyle();

        styleStack.push(builder.defaultStyle());

        // State:
        // 0 - Add text
        // 1 - Hint detected, reading hint field name
        // 2 - Reading outline color
        // 3 - Reading strikethrough color
        // 4 - Reading strikethrough outline color
        // 5 - Reading underline color
        // 6 - Reading color type
        // 7 - Reading size
        // 8 - Reading color hex
        // 9 - Reading color rgb
        // 10 - Reading color argb
        // 11 - Reading color rgba
        // 12 - Reading color name
        int state = 0;

        int end = 0;
        String tmp;
        int hintIndex;
        short[] color = new short[]{255,255,255,255};
        final int[] argbPerm = new int[]{0,1,2,3};
        final int[] rgbPerm = new int[]{3,0,1,2};
        int colorIdx = 0;

        for (int start = 0; start < rawText.length(); start++) {
            switch (state) {
                case 0:
                    if (rawText.charAt(start) == CONTROL_PREFIX) {
                        state = 1;
                        continue;
                    } else if (rawText.charAt(start) == CONTROL_ESCAPE && start + 1 < rawText.length()) {
                        builder.appendCodepoint(rawText.codePointAt(start + 1));
                        start++;
                        continue;
                    } else if (start + 1 < rawText.length()
                            && rawText.charAt(start) == CONTROL_END && rawText.charAt(start + 1) == CONTROL_PREFIX) {
                        styleStack.pop();
                        if (styleStack.isEmpty())
                            styleStack.push(builder.defaultStyle());
                        curr = styleStack.top();
                        builder.style(curr);
                        start++;
                        continue;
                    }
                    end = readText(rawText, start);
                    builder.appendLiteral(rawText, start, end);
                    start = end+1;
                    continue;
                case 1:
                    end = readHintName(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = HINT_NAME_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1)
                        continue;
                    if (hintIndex >= 2 && hintIndex <= 7) {
                        state = hintIndex;
                        if (hintIndex == 7)
                            end = start;
                        continue;
                    }
                    curr = newStyleFromFlags(curr, hintIndex);
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
                    continue;
                case 2:
                    end = readHint(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1)
                        continue;
                    curr = curr.withHint(TextHintLayout.OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
                    continue;
                case 3:
                    end = readHint(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1)
                        continue;
                    curr = curr.withHint(TextHintLayout.STRIKETHROUGH_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
                    continue;
                case 4:
                    end = readHint(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1)
                        continue;
                    curr = curr.withHint(TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
                    continue;
                case 5:
                    end = readHint(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1)
                        continue;
                    curr = curr.withHint(TextHintLayout.UNDERLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
                    continue;
                case 6:
                    if (rawText.charAt(start) == '#') {
                        state = 8;
                        continue;
                    } else if (rawText.charAt(start) == 'a') {
                        if (start + 5 > rawText.length())
                            continue;
                        if (rawText.substring(start + 1, start + 5).equals("rgb(")) {
                            state = 10;
                            start += 5;
                            continue;
                        }
                    } else if (start + 5 <= rawText.length() && rawText.substring(start, start + 5).equals("rgba(")) {
                        state = 11;
                        start += 5;
                        continue;
                    } else if (start + 4 <= rawText.length() && rawText.substring(start, start + 4).equals("rgb(")) {
                        state = 9;
                        start += 4;
                        continue;
                    }
                    state = 12;
                    continue;
                case 7:
                    if (rawText.charAt(start) == CONTROL_SEPARATOR || rawText.charAt(start) == CONTROL_SUFFIX) {
                        try {
                            curr = curr.withSize(Float.parseFloat(rawText.substring(end, start)));
                        } catch (NumberFormatException _) {} finally {
                            state = 1;
                        }
                        if (rawText.charAt(start) == CONTROL_SUFFIX) {
                            builder.style(curr);
                            styleStack.push(curr);
                        }
                    }
                    continue;
                case 8:
                    end = readHexColor(rawText, start);
                    curr = curr.withColor(Integer.parseInt(rawText.substring(start, end), 16));
                    start = end+1;
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    continue;
                case 9:
                    end = readIntColor(rawText, start);
                    color[rgbPerm[colorIdx+1]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end+1;
                    colorIdx++;
                    if (colorIdx == 3) {
                        colorIdx = 0;
                        curr = curr.withColor(0xFF << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        start++;
                        state = 1;
                    }
                    continue;
                case 10:
                    end = readIntColor(rawText, start);
                    color[argbPerm[colorIdx]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end+1;
                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        curr = curr.withColor(color[argbPerm[0]] << 24 | color[argbPerm[1]] << 16 | color[argbPerm[2]] << 8 | color[argbPerm[3]]);
                        start++;
                    }
                    continue;
                case 11:
                    end = readIntColor(rawText, start);
                    color[argbPerm[colorIdx]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end+1;
                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        curr = curr.withColor(color[rgbPerm[0]] << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        start++;
                    }
                    continue;
                case 12:
                    end = readHint(rawText, start, internalBuilder);
                    start = end+1;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    colorIdx = COLOR_HEX_MAP.getOrDefault(tmp, -1);
                    if (colorIdx == -1) {
                        colorIdx = 0;
                        continue;
                    }
                    curr = curr.withColor(colorIdx);
                    colorIdx = 0;
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                    }
                    start++;
            }
        }
    }

    private int readText(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_PREFIX
                    || rawText.charAt(end) == CONTROL_ESCAPE
                    || rawText.charAt(end) == CONTROL_END)
                return end - 1;
        }
        return rawText.length() - 1;
    }

    private int readHintName(@NonNull String rawText, int start, StringBuilder hintBuilder) {
        boolean gibberish = false;
        for (int end = start; end < rawText.length(); end++) {
            if (gibberish)
                continue;

            gibberish = !(Character.isAlphabetic(rawText.charAt(end)) | Character.isWhitespace(rawText.charAt(end)));

            if (rawText.charAt(end) == CONTROL_SEPARATOR
                    || rawText.charAt(end) == CONTROL_ASSIGNMENT
                    || rawText.charAt(end) == CONTROL_SUFFIX) {
                hintBuilder.append(rawText, start, end - 1);
                return end - 1;
            }
        }
        return rawText.length();
    }

    private @NonNull TextStyle newStyleFromFlags(final @NonNull TextStyle prev, final int hintIndex) {
        int hint = prev.hint();
        if (hintIndex-8 == 10)
            return TextStyle.DEFAULT;
        hint = switch (hintIndex - 8) {
            case 0 -> {
                hint = TextHintLayout.STRIKETHROUGH_OUTLINE_ENABLED.set(hint, true);
                yield TextHintLayout.STRIKETHROUGH.set(hint, true);
            }
            case 1 -> TextHintLayout.OUTLINE.set(hint, true);
            case 2 -> {
                hint = TextHintLayout.STRIKETHROUGH_ROUNDED.set(hint, true);
                yield TextHintLayout.STRIKETHROUGH.set(hint, true);
            }
            case 3 -> {
                hint = TextHintLayout.UNDERLINE_SHADOW_ENABLED.set(hint, true);
                yield TextHintLayout.UNDERLINE.set(hint, true);
            }
            case 4 -> TextHintLayout.ITALIC.set(hint, true);
            case 5 -> TextHintLayout.BOLD.set(hint, true);
            case 6 -> TextHintLayout.OBFUSCATED.set(hint, true);
            case 7 -> TextHintLayout.STRIKETHROUGH.set(hint, true);
            case 8 -> TextHintLayout.UNDERLINE.set(hint, true);
            case 9 -> TextHintLayout.SHADOW.set(hint, true);
            default -> hint;
        };
        return prev.withHint(hint);
    }

    private int readHint(@NonNull String rawText, int start, StringBuilder hintBuilder) {
        boolean alphabetic = false;
        boolean numeric = false;
        for (int end = start; end < rawText.length(); end++) {
            if (alphabetic && numeric)
                continue; // Gibberish

            alphabetic |= Character.isAlphabetic(rawText.charAt(end));
            numeric |= Character.isDigit(rawText.charAt(end));

            if (rawText.charAt(end) == CONTROL_SEPARATOR
                    || rawText.charAt(end) == CONTROL_SUFFIX) {
                hintBuilder.append(rawText, start, end - 1);
                return end - 1;
            }
        }
        return rawText.length();
    }

    private int readHexColor(@NonNull String rawText, int start) {
        boolean gibberish = false;
        for (int end = start; end < rawText.length(); end++) {
            if (gibberish)
                continue;

            gibberish = !(Character.isDigit(rawText.charAt(end)) | Character.isWhitespace(rawText.charAt(end)) | Character.toLowerCase(rawText.charAt(end)) >= 'a' & Character.toLowerCase(rawText.charAt(end)) <= 'f');

            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == CONTROL_SUFFIX | end - start == 8)
                return end - 1;
        }
        return rawText.length() - 1;
    }

    private int readIntColor(@NonNull String rawText, int start) {
        boolean gibberish = false;
        for (int end = start; end < rawText.length(); end++) {
            if (gibberish)
                continue;

            gibberish = !(Character.isDigit(rawText.charAt(end)) | rawText.charAt(end) == '.');

            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == ')')
                return end - 1;
        }
        return rawText.length() - 1;
    }

    private static final ImmutableMap<String, Integer> HINT_NAME_MAP = ImmutableMap.<String, Integer>builder()
            .put("outline color", 2)
            .put("outline colour", 2)
            .put("oc", 2)
            .put("strikethrough color", 3)
            .put("strikethrough colour", 3)
            .put("ssc", 3)
            .put("strikethrough outline color", 4)
            .put("strikethrough outline colour", 4)
            .put("ssoc", 4)
            .put("underline color", 5)
            .put("underline colour", 5)
            .put("uc", 5)
            .put("color", 6)
            .put("colour", 6)
            .put("c", 6)
            .put("size", 7)
            .put("s", 7)
            .put("strikethrough outline", 8)
            .put("sso", 8)
            .put("outline", 9)
            .put("o", 9)
            .put("strikethrough rounded", 10)
            .put("ssr", 10)
            .put("underline shadow", 11)
            .put("ush", 11)
            .put("italic", 12)
            .put("i", 12)
            .put("bold", 13)
            .put("b", 13)
            .put("obfuscated", 14)
            .put("x", 14)
            .put("strikethrough", 15)
            .put("ss", 15)
            .put("underline", 16)
            .put("u", 16)
            .put("shadow", 17)
            .put("sh", 17)
            .put("default", 18)
            .put("def", 18)
            .build();

    private static final ImmutableMap<String, Integer> COLOR_MAP = ImmutableMap.<String, Integer>builder()
            .put("black", 0)
            .put("dark blue", 1)
            .put("dark green", 2)
            .put("dark aqua", 3)
            .put("dark red", 4)
            .put("dark purple", 5)
            .put("gold", 6)
            .put("gray", 7)
            .put("dark gray", 8)
            .put("blue", 9)
            .put("green", 10)
            .put("aqua", 11)
            .put("red", 12)
            .put("light purple", 13)
            .put("yellow", 14)
            .put("white", 15)
            .put("charcoal", 16)
            .put("neutral gray", 17)
            .put("silver", 18)
            .put("soft white", 19)
            .put("maroon", 20)
            .put("orange", 21)
            .put("olive", 22)
            .put("lime", 23)
            .put("forest green", 24)
            .put("teal", 25)
            .put("sky blue", 26)
            .put("navy", 27)
            .put("violet", 28)
            .put("purple", 29)
            .put("rose", 30)
            .put("pink", 31)
            .build();

    private static final ImmutableMap<String, Integer> COLOR_HEX_MAP = ImmutableMap.<String, Integer>builder()
            .put("black", 0x000000)
            .put("dark blue", 0x0000AA)
            .put("dark green", 0x00AA00)
            .put("dark aqua", 0x00AAAA)
            .put("dark red", 0xAA0000)
            .put("dark purple", 0xAA00AA)
            .put("gold", 0xFFAA00)
            .put("gray", 0xAAAAAA)
            .put("dark gray", 0x555555)
            .put("blue", 0x5555FF)
            .put("green", 0x55FF55)
            .put("aqua", 0x55FFFF)
            .put("red", 0xFF5555)
            .put("light purple", 0xFF55FF)
            .put("yellow", 0xFFFF55)
            .put("white", 0xFFFFFF)
            .put("charcoal", 0x2B2B2B)
            .put("neutral gray", 0x808080)
            .put("silver", 0xC0C0C0)
            .put("soft white", 0xE6E6E6)
            .put("maroon", 0x800000)
            .put("orange", 0xFF8000)
            .put("olive", 0x808000)
            .put("lime", 0x80FF00)
            .put("forest green", 0x008000)
            .put("teal", 0x008080)
            .put("sky blue", 0x0080FF)
            .put("navy", 0x000080)
            .put("violet", 0x8000FF)
            .put("purple", 0x800080)
            .put("rose", 0xFF0080)
            .put("pink", 0xFF80B5)
            .build();
}
