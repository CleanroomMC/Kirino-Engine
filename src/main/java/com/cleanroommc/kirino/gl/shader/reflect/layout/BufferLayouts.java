package com.cleanroommc.kirino.gl.shader.reflect.layout;

import com.cleanroommc.kirino.gl.shader.reflect.resource.BlockMember;
import com.cleanroommc.kirino.gl.shader.reflect.type.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public final class BufferLayouts {

    private static final class UnknownLayoutException extends RuntimeException {

        private UnknownLayoutException() {
            super(null, null, false, false);
        }
    }

    private BufferLayouts() {
    }

    private static long round(long value, long alignment) {
        return Math.multiplyExact(Math.floorDiv(Math.addExact(value, alignment - 1), alignment), alignment);
    }

    private static long arrayAlignment(long alignment, BlockLayout standard) {
        return standard == BlockLayout.STD140 ? round(alignment, 16) : alignment;
    }

    private static BufferLayout leaf(long alignment, long size) {
        return new BufferLayout(
                alignment,
                size,
                0,
                0,
                false,
                false,
                null,
                ImmutableList.of());
    }

    private static BufferLayout struct(ImmutableList<BlockMember> members, BlockLayout standard, boolean rowMajor) {
        long cursor = 0;
        long alignment = standard == BlockLayout.STD140 ? 16 : 1;
        boolean runtime = false;

        ImmutableList.Builder<BufferLayoutMember> result = ImmutableList.builder();
        for (int i = 0; i < members.size(); i++) {
            BlockMember member = members.get(i);
            LayoutQualifiers qualifiers = member.layout();
            boolean rows = qualifiers.matrixOrder() == null ? rowMajor : qualifiers.matrixOrder() == MatrixOrder.ROW_MAJOR;
            BufferLayout layout = value(member.type(), standard, rows);
            long requiredAlignment = layout.alignment();
            if (qualifiers.align() != null) {
                if (qualifiers.align().value() == null) {
                    throw new UnknownLayoutException();
                }

                requiredAlignment = Math.max(requiredAlignment, qualifiers.align().value());
            }

            long start = cursor;
            if (qualifiers.offset() != null) {
                if (qualifiers.offset().value() == null) {
                    throw new UnknownLayoutException();
                }

                start = qualifiers.offset().value();
                Preconditions.checkArgument(start >= cursor && start % layout.alignment() == 0,
                        "Member \"%s\" has an overlapping or misaligned explicit offset.", member.name());
            }
            long offset = round(start, requiredAlignment);
            Preconditions.checkArgument(!layout.runtimeSized() || i == members.size() - 1,
                    "Only the final block member can be runtime-sized.");

            runtime |= layout.runtimeSized();
            result.add(new BufferLayoutMember(member.name(), member.type(), offset, layout));
            cursor = Math.addExact(offset, layout.size());
            alignment = Math.max(alignment, layout.alignment());
        }

        return new BufferLayout(
                alignment,
                runtime ? cursor : round(cursor, alignment),
                0,
                0,
                rowMajor,
                runtime,
                null,
                result.build());
    }

    private static BufferLayout value(GlslType type, BlockLayout standard, boolean rowMajor) {
        return switch (type) {
            case ScalarType scalar -> leaf(scalar == ScalarType.DOUBLE ? 8 : 4, scalar == ScalarType.DOUBLE ? 8 : 4);
            case VectorType vector -> {
                long scalar = vector.componentType() == ScalarType.DOUBLE ? 8 : 4;
                yield leaf(scalar * (vector.components() == 2 ? 2 : 4), scalar * vector.components());
            }
            case MatrixType matrix -> {
                int components = rowMajor ? matrix.columns() : matrix.rows();
                int vectors = rowMajor ? matrix.rows() : matrix.columns();
                BufferLayout vector = value(new VectorType(matrix.componentType(), components), standard, false);
                long alignment = arrayAlignment(vector.alignment(), standard);
                long stride = round(vector.size(), alignment);
                yield new BufferLayout(
                        alignment,
                        Math.multiplyExact(vectors, stride),
                        0,
                        stride,
                        rowMajor,
                        false,
                        vector,
                        ImmutableList.of());
            }
            case ArrayType array -> {
                BufferLayout element = value(array.elementType(), standard, rowMajor);
                Preconditions.checkArgument(!element.runtimeSized(),
                        "A runtime-sized array cannot be nested in another array.");

                long alignment = arrayAlignment(element.alignment(), standard);
                long stride = round(element.size(), alignment);
                if (!array.extent().runtimeSized() && array.extent().length().isEmpty()) {
                    throw new UnknownLayoutException();
                }

                long size = array.extent().runtimeSized() ? 0 : Math.multiplyExact(stride, array.extent().length().orElseThrow());
                yield new BufferLayout(
                        alignment,
                        size,
                        stride,
                        element.matrixStride(),
                        rowMajor,
                        array.extent().runtimeSized(),
                        element,
                        ImmutableList.of());
            }
            case StructType struct -> {
                ImmutableList.Builder<BlockMember> members = ImmutableList.builder();
                for (StructMember member : struct.members()) {
                    members.add(new BlockMember(member.name(), member.type(), member.qualifiers(), LayoutQualifiers.EMPTY));
                }
                yield struct(members.build(), standard, rowMajor);
            }
            default -> throw new UnknownLayoutException();
        };
    }

    @NonNull
    public static Optional<BufferLayout> compute(@NonNull ImmutableList<BlockMember> members, @NonNull LayoutQualifiers layout) {
        Preconditions.checkNotNull(members);
        Preconditions.checkNotNull(layout);

        if (layout.blockLayout() != BlockLayout.STD140 && layout.blockLayout() != BlockLayout.STD430) {
            return Optional.empty();
        }
        try {
            return Optional.of(struct(members, layout.blockLayout(), layout.matrixOrder() == MatrixOrder.ROW_MAJOR));
        } catch (UnknownLayoutException ignored) {
            return Optional.empty();
        }
    }

    @NonNull
    public static Optional<BufferLayout> compute(@NonNull GlslType type, @NonNull BlockLayout standard, @NonNull MatrixOrder order) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(standard);
        Preconditions.checkNotNull(order);

        if (standard != BlockLayout.STD140 && standard != BlockLayout.STD430) {
            return Optional.empty();
        }
        try {
            return Optional.of(value(type, standard, order == MatrixOrder.ROW_MAJOR));
        } catch (UnknownLayoutException ignored) {
            return Optional.empty();
        }
    }
}
