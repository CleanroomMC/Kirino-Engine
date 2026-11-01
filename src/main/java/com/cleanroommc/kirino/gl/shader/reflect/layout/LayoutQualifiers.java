package com.cleanroommc.kirino.gl.shader.reflect.layout;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.cleanroommc.kirino.gl.shader.reflect.ConstantExpressions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public record LayoutQualifiers(
        @Nullable BlockLayout blockLayout,
        @Nullable MatrixOrder matrixOrder,
        @Nullable IntegerExpression binding,
        @Nullable IntegerExpression location,
        @Nullable IntegerExpression component,
        @Nullable IntegerExpression index,
        @Nullable IntegerExpression offset,
        @Nullable IntegerExpression align,
        @NonNull TransformFeedbackLayout transformFeedback,
        @Nullable ImageFormat imageFormat,
        boolean originUpperLeft,
        boolean pixelCenterInteger,
        @Nullable ConservativeDepth conservativeDepth,
        @NonNull ImmutableList<ShaderMeta.Layout> declared) {

    public static final LayoutQualifiers EMPTY = new LayoutQualifiers(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            TransformFeedbackLayout.EMPTY,
            null,
            false,
            false,
            null,
            ImmutableList.of());

    public LayoutQualifiers {
        Preconditions.checkNotNull(transformFeedback);
        Preconditions.checkNotNull(declared);
    }

    private static IntegerExpression requireNumber(IntegerExpression value, String name) {
        Preconditions.checkArgument(value != null, "Layout qualifier \"%s\" requires a value.", name);
        Preconditions.checkArgument(value.value() == null || value.value() >= 0,
                "Layout qualifier \"%s\" must not be negative.", name);

        return value;
    }

    @SuppressWarnings("DataFlowIssue")
    @NonNull
    public static LayoutQualifiers resolve(
            @NonNull List<ShaderMeta.Layout> declarations,
            @NonNull ConstantExpressions expressions,
            @NonNull LayoutQualifiers defaults) {

        Preconditions.checkNotNull(declarations);
        Preconditions.checkNotNull(expressions);
        Preconditions.checkNotNull(defaults);

        BlockLayout block = defaults.blockLayout;
        MatrixOrder matrix = defaults.matrixOrder;
        IntegerExpression binding = defaults.binding;
        IntegerExpression location = defaults.location;
        IntegerExpression component = defaults.component;
        IntegerExpression index = defaults.index;
        IntegerExpression offset = defaults.offset;
        IntegerExpression align = defaults.align;
        IntegerExpression xfbBuffer = defaults.transformFeedback.buffer();
        IntegerExpression xfbOffset = defaults.transformFeedback.offset();
        IntegerExpression xfbStride = defaults.transformFeedback.stride();
        IntegerExpression stream = defaults.transformFeedback.stream();
        ImageFormat format = defaults.imageFormat;
        ConservativeDepth depth = defaults.conservativeDepth;
        boolean origin = defaults.originUpperLeft;
        boolean pixel = defaults.pixelCenterInteger;
        for (ShaderMeta.Layout declaration : declarations) {
            String name = declaration.name().toLowerCase(Locale.ROOT);
            IntegerExpression value = declaration.expression() == null
                    ? null
                    : expressions.expression(declaration.expression());

            switch (name) {
                case "std140" -> block = BlockLayout.STD140;
                case "std430" -> block = BlockLayout.STD430;
                case "shared" -> block = BlockLayout.SHARED;
                case "packed" -> block = BlockLayout.PACKED;
                case "row_major" -> matrix = MatrixOrder.ROW_MAJOR;
                case "column_major" -> matrix = MatrixOrder.COLUMN_MAJOR;
                case "binding" -> binding = requireNumber(value, name);
                case "location" -> location = requireNumber(value, name);
                case "component" -> component = requireNumber(value, name);
                case "index" -> index = requireNumber(value, name);
                case "offset" -> offset = requireNumber(value, name);
                case "align" -> align = requireNumber(value, name);
                case "xfb_buffer" -> xfbBuffer = requireNumber(value, name);
                case "xfb_offset" -> xfbOffset = requireNumber(value, name);
                case "xfb_stride" -> xfbStride = requireNumber(value, name);
                case "stream" -> stream = requireNumber(value, name);
                case "origin_upper_left" -> origin = true;
                case "pixel_center_integer" -> pixel = true;
                case "depth_any" -> depth = ConservativeDepth.ANY;
                case "depth_greater" -> depth = ConservativeDepth.GREATER;
                case "depth_less" -> depth = ConservativeDepth.LESS;
                case "depth_unchanged" -> depth = ConservativeDepth.UNCHANGED;
                default -> {
                    try {
                        format = ImageFormat.valueOf(name.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        }

        if (align != null && align.value() != null) {
            long value = align.value();
            Preconditions.checkArgument(value > 0 && (value & value - 1) == 0,
                    "Alignment \"%s\" must be a power of two.", value);
        }
        Preconditions.checkArgument(component == null || component.value() == null || component.value() <= 3,
                "A location component must be between zero and three.");

        return new LayoutQualifiers(
                block,
                matrix,
                binding,
                location,
                component,
                index,
                offset,
                align,
                new TransformFeedbackLayout(
                        xfbBuffer,
                        xfbOffset,
                        xfbStride,
                        stream),
                format,
                origin,
                pixel,
                depth,
                ImmutableList.copyOf(declarations));
    }

    @NonNull
    public LayoutQualifiers memberDefaults() {
        return new LayoutQualifiers(
                null,
                matrixOrder,
                null,
                null,
                null,
                null,
                null,
                align,
                new TransformFeedbackLayout(
                        transformFeedback.buffer(),
                        null,
                        transformFeedback.stride(),
                        transformFeedback.stream()),
                null,
                false,
                false,
                null,
                ImmutableList.of());
    }

    @NonNull
    public static LayoutQualifiers bufferDefaults(@NonNull BlockLayout block, @NonNull MatrixOrder matrix) {
        Preconditions.checkNotNull(block);
        Preconditions.checkNotNull(matrix);

        return new LayoutQualifiers(
                block,
                matrix,
                null,
                null,
                null,
                null,
                null,
                null,
                TransformFeedbackLayout.EMPTY,
                null,
                false,
                false,
                null,
                ImmutableList.of());
    }

    @NonNull
    public LayoutQualifiers withOffset(@NonNull IntegerExpression offset) {
        Preconditions.checkNotNull(offset);

        return new LayoutQualifiers(
                blockLayout,
                matrixOrder,
                binding,
                location,
                component,
                index,
                offset,
                align,
                transformFeedback,
                imageFormat,
                originUpperLeft,
                pixelCenterInteger,
                conservativeDepth,
                declared);
    }

    @NonNull
    public LayoutQualifiers withLocation(@Nullable IntegerExpression location) {
        return new LayoutQualifiers(
                blockLayout,
                matrixOrder,
                binding,
                location,
                component,
                index,
                offset,
                align,
                transformFeedback,
                imageFormat,
                originUpperLeft,
                pixelCenterInteger,
                conservativeDepth,
                declared);
    }

    @NonNull
    public LayoutQualifiers withTransformFeedback(@NonNull TransformFeedbackLayout feedback) {
        Preconditions.checkNotNull(feedback);

        return new LayoutQualifiers(
                blockLayout,
                matrixOrder,
                binding,
                location,
                component,
                index,
                offset,
                align,
                feedback,
                imageFormat,
                originUpperLeft,
                pixelCenterInteger,
                conservativeDepth,
                declared);
    }
}
