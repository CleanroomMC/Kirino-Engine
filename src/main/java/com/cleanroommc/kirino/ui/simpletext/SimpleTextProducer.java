package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface SimpleTextProducer {

    /**
     * It resembles a builder pattern. Call {@link #set(float, float, float, float, float, float, int, float[])}
     * to initialize the line info.
     */
    final class LineInfo {

        private boolean empty = true;

        private float lineWidth;
        private float lineTopToBaseline;
        private float minX;
        private float maxX;
        private float minY;
        private float maxY;
        private int codepointCount;
        private float[] progressiveLength;

        public int getCodepointCount() {
            return codepointCount;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getProgressiveLengthAt(int index) {
            Preconditions.checkElementIndex(index, codepointCount);

            return progressiveLength[index];
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getLineTopToBaseline() {
            return lineTopToBaseline;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getLineWidth() {
            return lineWidth;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getMaxX() {
            return maxX;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getMaxY() {
            return maxY;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getMinX() {
            return minX;
        }

        /**
         * <p>Unit: Minecraft scaled resolution</p>
         */
        public float getMinY() {
            return minY;
        }

        /**
         * A {@link LineInfo} is only considered empty when no parameters have been set.
         */
        public boolean isEmpty() {
            return empty;
        }

        /**
         * <p>Note: It must only be called once.</p>
         */
        public void set(
                float lineWidth,
                float lineTopToBaseline,
                float minX,
                float maxX,
                float minY,
                float maxY,
                int codepointCount,
                float @NonNull [] progressiveLength) {

            Preconditions.checkNotNull(progressiveLength);
            Preconditions.checkState(empty,
                    "LineInfo must be empty. Must not set parameters twice.");

            empty = false;

            this.lineWidth = lineWidth;
            this.lineTopToBaseline = lineTopToBaseline;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.codepointCount = codepointCount;
            this.progressiveLength = progressiveLength;
        }
    }

    void beginBatch();
    void endBatch();

    /**
     * <p>Note: Font size is unitless and intended as a guideline only.
     * Actual implementations interpret it as appropriate.</p>
     */
    float standardFontSize();

    /**
     * Line height is also known as the <code>LineTopToBaseline</code>.
     *
     * <p>Unit: Minecraft scaled resolution</p>
     *
     * @param text The text to evaluate
     * @param fontSize This parameter is unitless and intended as a guideline only.
     *                 Actual implementations interpret it as appropriate
     *
     * @see LineInfo#getLineTopToBaseline()
     */
    float calcLineHeight(@NonNull String text, float fontSize);

    /**
     * @param text The raw text to be drawn
     * @param x Minecraft scaled resolution x coordinate
     * @param y Minecraft scaled resolution y coordinate
     * @param fontSize This parameter is unitless and intended as a guideline only.
     *                 Actual implementations interpret it as appropriate
     * @param outLineInfo It'll receive the line info when the input is non-null
     */
    void append(
            @NonNull String text,
            float x,
            float y,
            float fontSize,
            @Nullable LineInfo outLineInfo);

    /**
     * <p>Note: Parameters like <code>size</code>, <code>color</code>, <code>hint</code>
     * are essentially meaningless; your {@link SimpleTextConsumer} implementations interpret them as appropriate.</p>
     *
     * @param text The raw text to be drawn
     * @param x Minecraft scaled resolution x coordinate
     * @param y Minecraft scaled resolution y coordinate
     * @param fontSize This parameter is unitless and intended as a guideline only.
     *                 Actual implementations interpret it as appropriate
     * @param size Per-glyph size override. Length must match <code>text</code> codepoint count
     * @param color Per-glyph color override. Length must match <code>text</code> codepoint count
     * @param hint Per-glyph hint override. Length must match <code>text</code> codepoint count
     * @param outLineInfo It'll receive the line info when the input is non-null
     */
    void append(
            @NonNull String text,
            float x,
            float y,
            float fontSize,
            float @NonNull [] size,
            int @NonNull [] color,
            int @NonNull [] hint,
            @Nullable LineInfo outLineInfo);

    @NonNull TextCommandList submit();
}
