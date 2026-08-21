package com.cleanroommc.kirino.ui.simpletext.text;

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
    @SuppressWarnings("DataFlowIssue")
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
        boolean colorValid = true;

        float q, p; // for HSL

        for (int start = 0; start < rawText.length(); start++) {
            switch (state) {
                case 0:
                    // <editor-fold desc="add text">
                    if (rawText.charAt(start) == CONTROL_PREFIX) {
                        state = 1;
                        continue;
                    } else if (rawText.charAt(start) == CONTROL_ESCAPE && start + 1 < rawText.length()) {
                        int escapedCodepoint = rawText.codePointAt(start + 1);
                        builder.appendCodepoint(escapedCodepoint);
                        start += Character.charCount(escapedCodepoint);
                        continue;
                    } else if (start + 1 < rawText.length() && rawText.charAt(start) == CONTROL_END && rawText.charAt(start + 1) == CONTROL_PREFIX) {
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
                    // <editor-fold desc="hint detected, reading hint field name">
                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    end = readHintName(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);

                    if (start >= rawText.length()) {
                        continue;
                    }

                    hintIndex = HINT_NAME_MAP.getOrDefault(tmp, -1);
                    if (hintIndex == -1) {
                        if (rawText.charAt(start) == CONTROL_ASSIGNMENT) {
                            end = readHintValueEnd(rawText, start + 1);
                            start = end;
                        }

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        }

                        continue;
                    }

                    if (hintIndex >= 2 && hintIndex <= 7) {
                        if (rawText.charAt(start) == CONTROL_ASSIGNMENT) {
                            state = hintIndex;
                            continue;
                        }

                        if (rawText.charAt(start) == CONTROL_SUFFIX) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        }

                        continue;
                    }

                    if (rawText.charAt(start) == CONTROL_ASSIGNMENT) {
                        end = readHintValueEnd(rawText, start + 1);
                        start = end;

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        }

                        continue;
                    }

                    if (hintIndex == 18) {
                        curr = builder.defaultStyle();
                    } else {
                        curr = newStyleFromFlags(curr, hintIndex);
                    }

                    if (rawText.charAt(start) == CONTROL_SUFFIX) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    continue;
                    // </editor-fold>
                case 2:
                    // <editor-fold desc="reading outline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex != -1) {
                        curr = curr.withHint(TextHintLayout.OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    }

                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
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
                    if (hintIndex != -1) {
                        curr = curr.withHint(TextHintLayout.STRIKETHROUGH_COLOR.set(curr.hint(), hintIndex));
                    }

                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 4:
                    // <editor-fold desc="reading strikethrough outline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex != -1) {
                        curr = curr.withHint(TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.set(curr.hint(), hintIndex));
                    }

                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 5:
                    // <editor-fold desc="reading underline color">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex != -1) {
                        curr = curr.withHint(TextHintLayout.UNDERLINE_COLOR.set(curr.hint(), hintIndex));
                    }

                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 6:
                    // <editor-fold desc="reading color type">
                    if (rawText.charAt(start) == '#') {
                        state = 8;
                        continue;
                    } else if (rawText.charAt(start) == 'a') {
                        if (start + 5 > rawText.length()) {
                            continue;
                        }
                        if (rawText.startsWith("rgb(", start + 1)) {
                            colorIdx = 0;
                            colorValid = true;
                            state = 10;
                            start += 4;
                            continue;
                        }
                    } else if (start + 5 <= rawText.length() && rawText.startsWith("rgba(", start)) {
                        colorIdx = 0;
                        colorValid = true;
                        state = 11;
                        start += 4;
                        continue;
                    } else if (start + 4 <= rawText.length() && rawText.startsWith("rgb(", start)) {
                        colorIdx = 0;
                        colorValid = true;
                        state = 9;
                        start += 3;
                        continue;
                    } else if (start + 4 <= rawText.length() && rawText.startsWith("hsl(", start)) {
                        colorIdx = 0;
                        colorValid = true;
                        state = 13;
                        start += 3;
                        continue;
                    }
                    state = 12;
                    start--;
                    continue;
                    // </editor-fold>
                case 7:
                    // <editor-fold desc="reading size">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    try {
                        float size = Float.parseFloat(tmp);
                        if (Float.isFinite(size) && size > 0f) {
                            curr = curr.withSize(size);
                        }
                    } catch (NumberFormatException _) {
                    } finally {
                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
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
                    // <editor-fold desc="reading color hex">
                    end = readHexColor(rawText, start);
                    try {
                        int length = end - start;
                        if (length == 6 || length == 8) {
                            int parsedColor = Integer.parseUnsignedInt(rawText.substring(start, end), 16);
                            if (length == 6) {
                                parsedColor |= 0xFF000000;
                            }

                            curr = curr.withColor(parsedColor);
                        }
                    } catch (NumberFormatException _) {
                    }
                    start = end;
                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 9:
                    // <editor-fold desc="reading color rgb">
                    end = readNumber(rawText, start);
                    try {
                        int component = Integer.parseInt(rawText.substring(start, end));
                        if (!(component >= 0 && component <= 255)) {
                            colorValid = false;
                        } else {
                            color[rgbPerm[colorIdx + 1]] = (short) component;
                        }
                    } catch (NumberFormatException _) {
                        colorValid = false;
                    }
                    start = end;

                    if (!isCharAt(rawText, start, colorIdx == 2 ? ')' : CONTROL_SEPARATOR)) {
                        colorIdx = 0;
                        colorValid = true;

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        } else {
                            state = 1;
                        }

                        continue;
                    }

                    colorIdx++;
                    if (colorIdx == 3) {
                        colorIdx = 0;
                        if (colorValid) {
                            curr = curr.withColor(0xFF << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        }

                        colorValid = true;
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 10:
                    // <editor-fold desc="reading color argb">
                    end = readNumber(rawText, start);
                    try {
                        int component = Integer.parseInt(rawText.substring(start, end));
                        if (!(component >= 0 && component <= 255)) {
                            colorValid = false;
                        } else {
                            color[argbPerm[colorIdx]] = (short) component;
                        }
                    } catch (NumberFormatException _) {
                        colorValid = false;
                    }
                    start = end;

                    if (!isCharAt(rawText, start, colorIdx == 3 ? ')' : CONTROL_SEPARATOR)) {
                        colorIdx = 0;
                        colorValid = true;

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        } else {
                            state = 1;
                        }

                        continue;
                    }

                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        if (colorValid) {
                            curr = curr.withColor(color[argbPerm[0]] << 24 | color[argbPerm[1]] << 16 | color[argbPerm[2]] << 8 | color[argbPerm[3]]);
                        }

                        colorValid = true;
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 11:
                    // <editor-fold desc="reading color rgba">
                    end = readNumber(rawText, start);
                    try {
                        int component = Integer.parseInt(rawText.substring(start, end));
                        if (!(component >= 0 && component <= 255)) {
                            colorValid = false;
                        } else {
                            color[argbPerm[colorIdx]] = (short) component;
                        }
                    } catch (NumberFormatException _) {
                        colorValid = false;
                    }
                    start = end;

                    if (!isCharAt(rawText, start, colorIdx == 3 ? ')' : CONTROL_SEPARATOR)) {
                        colorIdx = 0;
                        colorValid = true;

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        } else {
                            state = 1;
                        }

                        continue;
                    }

                    colorIdx++;
                    if (colorIdx == 4) {
                        colorIdx = 0;
                        if (colorValid) {
                            curr = curr.withColor(color[rgbPerm[0]] << 24 | color[rgbPerm[1]] << 16 | color[rgbPerm[2]] << 8 | color[rgbPerm[3]]);
                        }

                        colorValid = true;
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
                case 12:
                    // <editor-fold desc="reading color name">
                    end = readHint(rawText, start, internalBuilder);
                    start = end;
                    tmp = internalBuilder.toString().trim();
                    internalBuilder.setLength(0);
                    hintIndex = COLOR_MAP.getOrDefault(tmp, -1);
                    if (hintIndex != -1) {
                        curr = curr.withColor(TextColorPalette32.replaceRgb(0xFF000000, TextColorPalette32.rgb(hintIndex)));
                    }

                    if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                        builder.style(curr);
                        styleStack.push(curr);
                        state = 0;
                        continue;
                    }

                    state = 1;
                    continue;
                    // </editor-fold>
                case 13:
                    // <editor-fold desc="reading color hsl">
                    end = readNumber(rawText, start);
                    try {
                        hsl[colorIdx] = Float.parseFloat(rawText.substring(start, end));
                        if (!(hsl[colorIdx] >= 0f && hsl[colorIdx] <= 1f)) {
                            colorValid = false;
                        }
                    } catch (NumberFormatException _) {
                        colorValid = false;
                    }
                    start = end;

                    if (!isCharAt(rawText, start, colorIdx == 2 ? ')' : CONTROL_SEPARATOR)) {
                        colorIdx = 0;
                        colorValid = true;

                        if (isCharAt(rawText, start, CONTROL_SUFFIX)) {
                            builder.style(curr);
                            styleStack.push(curr);
                            state = 0;
                        } else {
                            state = 1;
                        }

                        continue;
                    }

                    colorIdx++;
                    if (colorIdx == 3) {
                        colorIdx = 0;
                        if (colorValid) {
                            if (hsl[1] == 0) {
                                int gray = Math.round(hsl[2] * 255) & 0xFF;
                                curr = curr.withColor(0xFF << 24 | gray << 16 | gray << 8 | gray);
                                colorValid = true;
                                state = 1;

                                continue;
                            }

                            q = hsl[2] < 0.5f ? hsl[2] * (1 + hsl[1]) : hsl[2] + hsl[1] - hsl[1] * hsl[2];
                            p = 2 * hsl[2] - q;

                            curr = curr.withColor(0xFF << 24
                                    | Math.round(hueToRgb(p, q, hsl[0] + 0.333333333f) * 255) << 16
                                    | Math.round(hueToRgb(p, q, hsl[0]) * 255) << 8
                                    | Math.round(hueToRgb(p, q, hsl[0] - 0.333333333f) * 255));
                        }

                        colorValid = true;
                        state = 1;
                    }

                    continue;
                    // </editor-fold>
            }
        }
    }

    private int readText(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_PREFIX
                    || rawText.charAt(end) == CONTROL_ESCAPE && end + 1 < rawText.length()
                    || rawText.charAt(end) == CONTROL_END && end + 1 < rawText.length()
                    && rawText.charAt(end + 1) == CONTROL_PREFIX) {

                return end;
            }
        }

        return rawText.length();
    }

    private int readHintName(@NonNull String rawText, int start, @NonNull StringBuilder hintBuilder) {
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

    private int readHint(@NonNull String rawText, int start, @NonNull StringBuilder hintBuilder) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == CONTROL_SUFFIX) {
                hintBuilder.append(rawText, start, end);
                return end;
            }
        }

        return rawText.length();
    }

    private int readHintValueEnd(@NonNull String rawText, int start) {
        int parentheses = 0;
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == '(') {
                parentheses++;
            } else if (rawText.charAt(end) == ')' && parentheses > 0) {
                parentheses--;
            } else if (parentheses == 0 && (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == CONTROL_SUFFIX)) {
                return end;
            }
        }

        return rawText.length();
    }

    private int readHexColor(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR || rawText.charAt(end) == CONTROL_SUFFIX) {
                return end;
            }
        }

        return rawText.length();
    }

    private int readNumber(@NonNull String rawText, int start) {
        for (int end = start; end < rawText.length(); end++) {
            if (rawText.charAt(end) == CONTROL_SEPARATOR
                    || rawText.charAt(end) == ')'
                    || rawText.charAt(end) == CONTROL_SUFFIX) {

                return end;
            }
        }

        return rawText.length();
    }

    private boolean isCharAt(@NonNull String rawText, int index, char value) {
        return index >= 0 && index < rawText.length() && rawText.charAt(index) == value;
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
}
