package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.cleanroommc.kirino.gl.shader.reflect.type.ArrayType;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record ProgramBlockMember(
        @NonNull String name,
        @NonNull GlslType type,
        long offset,
        long arrayStride,
        long matrixStride,
        boolean rowMajor,
        int topLevelArraySize,
        long topLevelArrayStride) {

    public ProgramBlockMember {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
    }

    @NonNull
    public ProgramBlockMember element(long index) {
        Preconditions.checkArgument(type instanceof ArrayType && arrayStride > 0,
                "This member is not a buffer array.");
        ArrayType array = (ArrayType) type;
        Preconditions.checkArgument(index >= 0
                        && (array.extent().length().isEmpty() || index < array.extent().length().orElseThrow()),
                "Array index \"%s\" is out of range.", index);

        String base = name.endsWith("[0]") ? name.substring(0, name.length() - 3) : name;
        boolean topLevel = topLevelArrayStride > 0 && name.indexOf("[0]") == name.length() - 3;
        return new ProgramBlockMember(
                base + "[" + index + "]", array.elementType(),
                Math.addExact(offset, Math.multiplyExact(index, arrayStride)),
                0,
                matrixStride,
                rowMajor,
                topLevel ? 1 : topLevelArraySize,
                topLevel ? 0 : topLevelArrayStride);
    }

    @NonNull
    public ProgramBlockMember topLevelElement(long index) {
        Preconditions.checkArgument(topLevelArrayStride > 0 && name.contains("[0]"),
                "This member is not inside a top-level buffer array.");
        Preconditions.checkArgument(index >= 0 && (topLevelArraySize == 0 || index < topLevelArraySize),
                "Top-level array index \"%s\" is out of range.", index);

        if (type instanceof ArrayType && name.indexOf("[0]") == name.length() - 3) {
            return element(index);
        }

        return new ProgramBlockMember(
                name.replaceFirst("\\[0]", "[" + index + "]"),
                type,
                Math.addExact(offset, Math.multiplyExact(index, topLevelArrayStride)),
                arrayStride,
                matrixStride,
                rowMajor,
                1,
                0);
    }
}
