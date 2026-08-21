package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;

final class KirinoStyledTextParser implements StyledTextParser {

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
    @SuppressWarnings("null")
    public void parse(@NonNull String rawText, @NonNull StyledTextBuilder builder) {
        Stack<TextStyle> styleStack = new ObjectArrayList<>();
        StringBuilder internalBuilder = new StringBuilder();
        TextStyle curr = builder.defaultStyle();

        styleStack.push(builder.defaultStyle());

        // state:
        // 0 - add text
        // 1 - hint detected, reading hint field name
        // 2 - reading outline color
        // 3 - reading strikethrough color
        // 4 - reading strikethrough outline color
        // 5 - reading underline color
        // 6 - reading color type
        // 7 - reading size
        // 8 - reading color hex
        // 9 - reading color rgb
        // 10 - reading color argb
        // 11 - reading color rgba
        // 12 - reading color name
        // 13 - reading color hsl
        int state = 0;

        int end = 0;
        String tmp;
        int hintIndex;
        short[] color = new short[]{255, 255, 255, 255};
        float[] hsl = new float[]{0, 0, 0};
        final int[] argbPerm = new int[]{0, 1, 2, 3};
        final int[] rgbPerm = new int[]{3, 0, 1, 2};
        int colorIdx = 0;

        float q, p; // for HSL

        for (int start = 0; start < rawText.length(); start++) {
            switch (state) {
                case 0:
                    // <editor-fold desc="Add text">
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
                        if (styleStack.isEmpty()) {
                            styleStack.push(builder.defaultStyle());
                        }

                        curr = styleStack.top();
                        builder.style(curr);
                        start++;
                        continue;
                    }
                    end = readText(rawText, start);
                    builder.appendLiteral(rawText, start, end);
                    start = end - 1;
                    continue;
                    // </editor-fold>
                case 1:
                    // <editor-fold desc="Hint detected, reading hint field name">
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                    }
                    end = readHintName(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = HINT_NAME_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        continue;
                    }

                    if (hintIndex >= 2 && hintIndex <= 7) {
                        state = hintIndex;
                        continue;
                    }

                    curr = newStyleFromFlags(curr, hintIndex);
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }
                    continue;
                    // </editor-fold>
                case 2:
                    // <editor-fold desc="Reading outline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        continue;
                    }

                    curr = curr.withHint(TextHintLayout.OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 3:
                    // <editor-fold desc="reading strikethrough color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        continue;
                    }

                    curr = curr.withHint(TextHintLayout.STRIKETHROUGH_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 4:
                    // <editor-fold desc="Reading strikethrough outline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        continue;
                    }

                    curr = curr.withHint(TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 5:
                    // <editor-fold desc="Reading underline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        state = 1;
                        continue;
                    }

                    curr = curr.withHint(TextHintLayout.UNDERLINE_COLOR.set(curr.hint(), hintIndex));
                    if (rawText.charAt(start + 1) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 6:
                    // <editor-fold desc="Reading color type">
                    if (rawText.charAt(start) == '#') {
                        state = 8;
                        continue;
                    } else if (rawText.charAt(start) == 'a') {
                        if (start + 5 > rawText.length()) {
                            continue;
                        }
                        if (rawText.substring(start + 1, start + 5).equals("rgb(")) {
                            state = 10;
                            start += 4;
                            continue;
                        }
                    } else if (start + 5 <= rawText.length() && rawText.substring(start, start + 5).equals("rgba(")) {
                        state = 11;
                        start += 4;
                        continue;
                    } else if (start + 4 <= rawText.length() && rawText.substring(start, start + 4).equals("rgb(")) {
                        state = 9;
                        start += 3;
                        continue;
                    } else if (start + 4 <= rawText.length() && rawText.substring(start, start + 4).equals("hsl(")) {
                        state = 13;
                        start += 3;
                        continue;
                    }
                    state = 12;
                    start--;
                    continue;
                    // </editor-fold>
                case 7:
                    // <editor-fold desc="Reading size">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    try {
                        curr = curr.withSize(Float.parseFloat(tmp));
                    } catch (NumberFormatException _) {
                    } finally {
                        if (rawText.charAt(start) == CONTROL_SUFFIX) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        } else {
                            state = 1;
                        }
                    }
                    continue;
                    // </editor-fold>
                case 8:
                    // <editor-fold desc="Reading color hex">
                    end = readHexColor(rawText, start);
                    try {
                        colorIdx = Integer.parseUnsignedInt(rawText.substring(start, end), 16);
                    } catch (NumberFormatException _) {
                        colorIdx = 0;
                    }
                    curr = curr.withColor(colorIdx);
                    colorIdx = 0;
                    start = end;
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 9:
                    // <editor-fold desc="Reading color rgb">
                    end = readNumber(rawText, start);
                    color[rgbPerm[colorIdx + 1]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end;
                    colorIdx++;
                    if (colorIdx == 3) {
                        colorIdx = 0;
                        curr = curr.withColor(0xFF << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 10:
                    // <editor-fold desc="Reading color argb">
                    end = readNumber(rawText, start);
                    color[argbPerm[colorIdx]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end;
                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        curr = curr.withColor(color[argbPerm[0]] << 24 | color[argbPerm[1]] << 16 | color[argbPerm[2]] << 8 | color[argbPerm[3]]);
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 11:
                    // <editor-fold desc="Reading color rgba">
                    end = readNumber(rawText, start);
                    color[argbPerm[colorIdx]] = (short) (Short.parseShort(rawText.substring(start, end)) & 0xFF);
                    start = end;
                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        curr = curr.withColor(color[rgbPerm[0]] << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 12:
                    // <editor-fold desc="Reading color name">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    colorIdx = COLOR_HEX_MAP.getOrDefault(tmp, -1);
                    if (colorIdx == -1) {
                        colorIdx = 0;
                        state = 1;
                        continue;
                    }

                    curr = curr.withColor(colorIdx);
                    colorIdx = 0;
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 13:
                    // <editor-fold desc="Reading color hsl">
                    end = readNumber(rawText, start);
                    hsl[colorIdx] = Float.parseFloat(rawText.substring(start, end));
                    start = end;
                    colorIdx++;
                    if (colorIdx == 3) {
                        colorIdx = 0;
                        if (hsl[1] == 0) {
                            colorIdx = Math.round(hsl[2] * 255) & 0xFF;
                            curr = curr.withColor(0xFF << 24 | colorIdx << 16 | colorIdx << 8 | colorIdx);
                            colorIdx = 0;
                            state = 1;

                            continue;
                        }

                        q = hsl[2] < 0.5f ? hsl[2] * (1 + hsl[1]) : hsl[2] + hsl[1] - hsl[1] * hsl[2];
                        p = 2 * hsl[2] - q;

                        curr = curr.withColor(0xFF << 24
                                | Math.round(hueToRgb(p, q, hsl[0] + 0.333333333f) * 255) << 16
                                | Math.round(hueToRgb(p, q, hsl[0]) * 255) << 8
                                | Math.round(hueToRgb(p, q, hsl[0] - 0.333333333f) * 255));
                        state = 1;
                    }
                    // </editor-fold>
            }
        }
    }

    private int readText(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_PREFIX
                    || rawText.charAt(end) == CONTROL_ESCAPE
                    || rawText.charAt(end) == CONTROL_END) {
                return end;
            }
        }

        return rawText.length();
    }

    private int readHintName(@NonNull String rawText, int start, StringBuilder hintBuilder) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR
                    || rawText.charAt(end) == CONTROL_ASSIGNMENT
                    || rawText.charAt(end) == CONTROL_SUFFIX) {
                hintBuilder.append(rawText, start, end);
                return end;
            }
        }

        return rawText.length();
    }

    private @NonNull TextStyle newStyleFromFlags(final @NonNull TextStyle prev, final int hintIndex) {
        int hint = prev.hint();
        if (hintIndex - 8 == 10) {
            return TextStyle.DEFAULT;
        }

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
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR
                    || rawText.charAt(end) == CONTROL_SUFFIX) {
                hintBuilder.append(rawText, start, end);
                return end;
            }
        }

        return rawText.length();
    }

    private int readHexColor(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == CONTROL_SUFFIX || end - start == 8) {
                return end;
            }
        }

        return rawText.length();
    }

    private int readNumber(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == ')') {
                return end;
            }
        }

        return rawText.length();
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) {
            t += 1;
        }

        if (t > 1) {
            t -= 1;
        }

        if (t < .166666666f) { // t < 1/6
            return p + (q - p) * 6 * t;
        }

        if (t < .5f) {
            return q;
        }

        if (t < .666666666f) { // t < 2/3
            return p + (q - p) * (.666666666f - t) * 6;
        }

        return p;
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
            .put("black", 0xFF000000)
            .put("dark blue", 0xFF0000AA)
            .put("dark green", 0xFF00AA00)
            .put("dark aqua", 0xFF00AAAA)
            .put("dark red", 0xFFAA0000)
            .put("dark purple", 0xFFAA00AA)
            .put("gold", 0xFFFFAA00)
            .put("gray", 0xFFAAAAAA)
            .put("dark gray", 0xFF555555)
            .put("blue", 0xFF5555FF)
            .put("green", 0xFF55FF55)
            .put("aqua", 0xFF55FFFF)
            .put("red", 0xFFFF5555)
            .put("light purple", 0xFFFF55FF)
            .put("yellow", 0xFFFFFF55)
            .put("white", 0xFFFFFFFF)
            .put("charcoal", 0xFF2B2B2B)
            .put("neutral gray", 0xFF808080)
            .put("silver", 0xFFC0C0C0)
            .put("soft white", 0xFFE6E6E6)
            .put("maroon", 0xFF800000)
            .put("orange", 0xFFFF8000)
            .put("olive", 0xFF808000)
            .put("lime", 0xFF80FF00)
            .put("forest green", 0xFF008000)
            .put("teal", 0xFF008080)
            .put("sky blue", 0xFF0080FF)
            .put("navy", 0xFF000080)
            .put("violet", 0xFF8000FF)
            .put("purple", 0xFF800080)
            .put("rose", 0xFFFF0080)
            .put("pink", 0xFFFF80B5)
            .build();
}
