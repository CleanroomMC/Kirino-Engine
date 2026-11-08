package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.cleanroommc.kirino.ecs.component.schema.codec.ArrayValues;
import com.cleanroommc.kirino.ecs.component.schema.codec.ScalarCodec;
import org.joml.*;
import java.lang.invoke.MethodType;
import java.util.*;

public enum ScalarType {
    BYTE(byte.class, FlattenedScalarType.BYTE, 1),
    SHORT(short.class, FlattenedScalarType.SHORT, 1),
    INT(int.class, FlattenedScalarType.INT, 1),
    LONG(long.class, FlattenedScalarType.LONG, 1),
    FLOAT(float.class, FlattenedScalarType.FLOAT, 1),
    DOUBLE(double.class, FlattenedScalarType.DOUBLE, 1),
    BOOL(boolean.class, FlattenedScalarType.BOOL, 1),
    VEC2(Vector2f.class, FlattenedScalarType.FLOAT, 2, "x", "y"),
    VEC3(Vector3f.class, FlattenedScalarType.FLOAT, 3, "x", "y", "z"),
    VEC4(Vector4f.class, FlattenedScalarType.FLOAT, 4, "x", "y", "z", "w"),
    MAT3(Matrix3f.class, FlattenedScalarType.FLOAT, 9, "m00", "m01", "m02", "m10", "m11", "m12", "m20", "m21", "m22"),
    MAT4(Matrix4f.class, FlattenedScalarType.FLOAT, 16, "m00", "m01", "m02", "m03", "m10", "m11", "m12", "m13", "m20", "m21", "m22", "m23", "m30", "m31", "m32", "m33");

    public final Class<?> clazz;
    public final String[] fieldNames;
    public final FlattenedScalarType primitiveType;
    public final int unitCount;
    public final List<FlattenedScalarType> flattenedTypes;
    private final Class<?> boxedClass;
    private final String[] sortedNames;
    private final int[] ordinals;

    ScalarType(Class<?> clazz, FlattenedScalarType primitiveType, int unitCount, String... fields) {
        this.clazz = clazz;
        this.boxedClass = MethodType.methodType(clazz).wrap().returnType();
        this.primitiveType = primitiveType;
        this.unitCount = unitCount;
        this.flattenedTypes = Collections.nCopies(unitCount, primitiveType);
        sortedNames = fields.clone();
        Arrays.sort(sortedNames);
        ordinals = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            ordinals[Arrays.binarySearch(sortedNames, fields[i])] = i;
        }
        fieldNames = fields.length == 0 ? null : sortedNames.clone();
    }

    public boolean isSingular() { return ordinals.length == 0; }

    public int ordinalOffsetOfField(String field) {
        Objects.requireNonNull(field);
        int index = Arrays.binarySearch(sortedNames, field);
        return index < 0 ? -1 : ordinals[index];
    }

    public Object newScalar(Object... args) {
        Objects.requireNonNull(args);
        if (args.length != unitCount) { return null; }
        for (Object arg : args) {
            if (!primitiveType.boxedClass.isInstance(arg)) { return null; }
        }
        return ScalarCodec.create(this, new ArrayValues(args), 0, 0);
    }

    public Object[] flattenScalar(Object instance) {
        Objects.requireNonNull(instance);
        if (!boxedClass.isInstance(instance)) {
            throw new IllegalArgumentException("Expected " + boxedClass.getName() + ", got " + instance.getClass().getName());
        }
        Object[] result = new Object[unitCount];
        ScalarCodec.write(this, instance, new ArrayValues(result), 0, 0);
        return result;
    }
}
