package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import java.util.List;

public enum FlattenedScalarType {
    BYTE(Byte.class), SHORT(Short.class), INT(Integer.class), LONG(Long.class),
    FLOAT(Float.class), DOUBLE(Double.class), BOOL(Boolean.class);

    public final Class<?> boxedClass;

    FlattenedScalarType(Class<?> boxedClass) { this.boxedClass = boxedClass; }

    public static List<FlattenedScalarType> flatten(ScalarType type) { return type.flattenedTypes; }
    public static int flattenedUnitCount(ScalarType type) { return type.unitCount; }
}
