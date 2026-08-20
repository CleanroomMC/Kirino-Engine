package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum FlattenedScalarType {
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOL;

    @NonNull
    public static List<@NonNull FlattenedScalarType> flatten(@NonNull ScalarType scalarType) {
        Preconditions.checkNotNull(scalarType);

        return switch (scalarType) {
            case BYTE -> Collections.singletonList(BYTE);
            case SHORT -> Collections.singletonList(SHORT);
            case INT -> Collections.singletonList(INT);
            case LONG -> Collections.singletonList(LONG);
            case FLOAT -> Collections.singletonList(FLOAT);
            case DOUBLE -> Collections.singletonList(DOUBLE);
            case BOOL -> Collections.singletonList(BOOL);
            case VEC2 -> Arrays.asList(FLOAT, FLOAT);
            case VEC3 -> Arrays.asList(FLOAT, FLOAT, FLOAT);
            case VEC4 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT);
            case MAT3 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT);
            case MAT4 -> Arrays.asList(FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT);
        };
    }

    public static int flattenedUnitCount(@NonNull ScalarType scalarType) {
        Preconditions.checkNotNull(scalarType);

        return switch (scalarType) {
            case BYTE -> 1;
            case SHORT -> 1;
            case INT -> 1;
            case LONG -> 1;
            case FLOAT -> 1;
            case DOUBLE -> 1;
            case BOOL -> 1;
            case VEC2 -> 2;
            case VEC3 -> 3;
            case VEC4 -> 4;
            case MAT3 -> 9;
            case MAT4 -> 16;
        };
    }
}
