package com.cleanroommc.kirino.gl.shader.reflect.layout;

import com.cleanroommc.kirino.gl.shader.reflect.type.ArrayExtent;
import com.cleanroommc.kirino.gl.shader.reflect.type.ArrayType;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BufferLayout(
        long alignment,
        long size,
        long arrayStride,
        long matrixStride,
        boolean rowMajor,
        boolean runtimeSized,
        @Nullable BufferLayout elementLayout,
        @NonNull ImmutableList<BufferLayoutMember> members) {

    private static final Pattern PATH = Pattern.compile("([a-zA-Z_][a-zA-Z_0-9]*)|\\[([0-9]+)]");

    public BufferLayout {
        Preconditions.checkNotNull(members);
        Preconditions.checkArgument(alignment > 0 && size >= 0 && arrayStride >= 0 && matrixStride >= 0,
                "Buffer layout dimensions must be non-negative with positive alignment.");
    }

    @NonNull
    public Optional<BufferLayoutMember> member(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Matcher matcher = PATH.matcher(name);
        BufferLayout current = this;
        GlslType type = null;
        long offset = 0;
        int end = 0;
        while (matcher.find()) {
            int expected = end > 0 && matcher.group(1) != null ? end + 1 : end;
            if (matcher.start() != expected || expected > end && name.charAt(end) != '.') {
                return Optional.empty();
            }
            if (matcher.group(1) != null) {
                BufferLayoutMember found = null;
                for (BufferLayoutMember member : current.members()) {
                    if (member.name().equals(matcher.group(1))) {
                        found = member;
                        break;
                    }
                }
                if (found == null) {
                    return Optional.empty();
                }
                offset = Math.addExact(offset, found.offset());
                type = found.type();
                current = found.layout();
            } else {
                if (!(type instanceof ArrayType(GlslType elementType, ArrayExtent extent)) || current.elementLayout() == null) {
                    return Optional.empty();
                }
                long index;
                try {
                    index = Long.parseLong(matcher.group(2));
                } catch (NumberFormatException ignored) {
                    return Optional.empty();
                }
                if (extent.length().isPresent() && index >= extent.length().orElseThrow()) {
                    return Optional.empty();
                }
                offset = Math.addExact(offset, Math.multiplyExact(index, current.arrayStride()));
                type = elementType;
                current = current.elementLayout();
            }
            end = matcher.end();
        }

        return type == null || end != name.length()
                ? Optional.empty()
                : Optional.of(new BufferLayoutMember(name, type, offset, current));
    }

    public long requiredSize(long runtimeElements) {
        Preconditions.checkArgument(runtimeElements >= 0,
                "Runtime element count must not be negative.");
        if (!runtimeSized) {
            Preconditions.checkArgument(runtimeElements == 0,
                    "This layout has no runtime-sized tail.");
            return size;
        }

        long stride = arrayStride;
        BufferLayout tail = this;
        while (stride == 0 && !tail.members().isEmpty()) {
            tail = tail.members().getLast().layout();
            stride = tail.arrayStride();
        }

        return Math.addExact(size, Math.multiplyExact(runtimeElements, stride));
    }
}
