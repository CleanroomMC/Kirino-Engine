package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A {@link ScalarType} is not by definition a scalar but more like built-in atomic types.
 * A {@link ScalarType} can be flattened into {@link FlattenedScalarType} which is strictly a scalar mathematically.
 */
public enum ScalarType implements Scalar {

    BYTE(byte.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Byte b) {
                return b;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Byte,
                    "Expected a java.lang.Byte. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    SHORT(short.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Short s) {
                return s;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Short,
                    "Expected a java.lang.Short. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    INT(int.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Integer i) {
                return i;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Integer,
                    "Expected a java.lang.Integer. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    LONG(long.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Long l) {
                return l;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Long,
                    "Expected a java.lang.Long. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    FLOAT(float.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Float f) {
                return f;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Float,
                    "Expected a java.lang.Float. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    DOUBLE(double.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Double d) {
                return d;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Double,
                    "Expected a java.lang.Double. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    BOOL(boolean.class) {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 1 && args[0] instanceof Boolean b) {
                return b;
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Boolean,
                    "Expected a java.lang.Boolean. Got %s instead.", scalarInstance.getClass().getName());

            return new Object[]{scalarInstance};
        }
    },
    VEC2(Vector2f.class, "x", "y") {

        @Override
        @Nullable
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 2) {
                if (args[ordinalOffsetOfField("x")] instanceof Float x &&
                        args[ordinalOffsetOfField("y")] instanceof Float y) {
                    return new Vector2f(x, y);
                }
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector2f,
                    "Expected a org.joml.Vector2f. Got %s instead.", scalarInstance.getClass().getName());

            Object[] flat = new Object[2];
            Vector2f obj = (Vector2f) scalarInstance;
            flat[ordinalOffsetOfField("x")] = obj.x;
            flat[ordinalOffsetOfField("y")] = obj.y;
            return flat;
        }
    },
    VEC3(Vector3f.class, "x", "y", "z") {

        @Nullable
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 3) {
                if (args[ordinalOffsetOfField("x")] instanceof Float x &&
                        args[ordinalOffsetOfField("y")] instanceof Float y &&
                        args[ordinalOffsetOfField("z")] instanceof Float z) {
                    return new Vector3f(x, y, z);
                }
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector3f,
                    "Expected a org.joml.Vector3f. Got %s instead.", scalarInstance.getClass().getName());

            Object[] flat = new Object[3];
            Vector3f obj = (Vector3f) scalarInstance;
            flat[ordinalOffsetOfField("x")] = obj.x;
            flat[ordinalOffsetOfField("y")] = obj.y;
            flat[ordinalOffsetOfField("z")] = obj.z;
            return flat;
        }
    },
    VEC4(Vector4f.class, "x", "y", "z", "w") {

        @Nullable
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 4) {
                if (args[ordinalOffsetOfField("x")] instanceof Float x &&
                        args[ordinalOffsetOfField("y")] instanceof Float y &&
                        args[ordinalOffsetOfField("z")] instanceof Float z &&
                        args[ordinalOffsetOfField("w")] instanceof Float w) {
                    return new Vector4f(x, y, z, w);
                }
            }

            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector4f,
                    "Expected a org.joml.Vector4f. Got %s instead.", scalarInstance.getClass().getName());

            Object[] flat = new Object[4];
            Vector4f obj = (Vector4f) scalarInstance;
            flat[ordinalOffsetOfField("x")] = obj.x;
            flat[ordinalOffsetOfField("y")] = obj.y;
            flat[ordinalOffsetOfField("z")] = obj.z;
            flat[ordinalOffsetOfField("w")] = obj.w;
            return flat;
        }
    },
    MAT3(Matrix3f.class,
            "m00", "m01", "m02",
            "m10", "m11", "m12",
            "m20", "m21", "m22") {

        @Nullable
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 9) {
                for (Object arg : args) {
                    if (!(arg instanceof Float)) {
                        return null;
                    }
                }
                return new Matrix3f(
                        (float) args[ordinalOffsetOfField("m00")],
                        (float) args[ordinalOffsetOfField("m01")],
                        (float) args[ordinalOffsetOfField("m02")],
                        (float) args[ordinalOffsetOfField("m10")],
                        (float) args[ordinalOffsetOfField("m11")],
                        (float) args[ordinalOffsetOfField("m12")],
                        (float) args[ordinalOffsetOfField("m20")],
                        (float) args[ordinalOffsetOfField("m21")],
                        (float) args[ordinalOffsetOfField("m22")]);
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Matrix3f,
                    "Expected a org.joml.Matrix3f. Got %s instead.", scalarInstance.getClass().getName());

            Object[] flat = new Object[9];
            Matrix3f matrix3f = (Matrix3f) scalarInstance;
            for (String field : fieldNames) {
                flat[ordinalOffsetOfField(field)] = matrix3f.get(
                        Integer.parseInt(String.valueOf(field.charAt(1))),
                        Integer.parseInt(String.valueOf(field.charAt(2))));
            }
            return flat;
        }
    },
    MAT4(Matrix4f.class,
            "m00", "m01", "m02", "m03",
            "m10", "m11", "m12", "m13",
            "m20", "m21", "m22", "m23",
            "m30", "m31", "m32", "m33") {

        @Nullable
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 16) {
                for (Object arg : args) {
                    if (!(arg instanceof Float)) {
                        return null;
                    }
                }
                return new Matrix4f(
                        (float) args[ordinalOffsetOfField("m00")],
                        (float) args[ordinalOffsetOfField("m01")],
                        (float) args[ordinalOffsetOfField("m02")],
                        (float) args[ordinalOffsetOfField("m03")],
                        (float) args[ordinalOffsetOfField("m10")],
                        (float) args[ordinalOffsetOfField("m11")],
                        (float) args[ordinalOffsetOfField("m12")],
                        (float) args[ordinalOffsetOfField("m13")],
                        (float) args[ordinalOffsetOfField("m20")],
                        (float) args[ordinalOffsetOfField("m21")],
                        (float) args[ordinalOffsetOfField("m22")],
                        (float) args[ordinalOffsetOfField("m23")],
                        (float) args[ordinalOffsetOfField("m30")],
                        (float) args[ordinalOffsetOfField("m31")],
                        (float) args[ordinalOffsetOfField("m32")],
                        (float) args[ordinalOffsetOfField("m33")]);
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Matrix4f,
                    "Expected a org.joml.Matrix4f. Got %s instead.", scalarInstance.getClass().getName());

            Object[] flat = new Object[16];
            Matrix4f matrix4f = (Matrix4f) scalarInstance;
            for (String field : fieldNames) {
                flat[ordinalOffsetOfField(field)] = matrix4f.get(
                        Integer.parseInt(String.valueOf(field.charAt(1))),
                        Integer.parseInt(String.valueOf(field.charAt(2))));
            }
            return flat;
        }
    };

    public final Class<?> clazz;
    public final String[] fieldNames;
    private final Integer[] ordinals;

    ScalarType(Class<?> clazz, String... fields) {
        this.clazz = clazz;

        if (fields.length == 0) {
            fieldNames = null;
            ordinals = null;
            return;
        }

        String[] tmpFieldNames = fields.clone();
        Integer[] tmpOrdinals = new Integer[fields.length];

        for (int i = 0; i < tmpOrdinals.length; i++) {
            tmpOrdinals[i] = i;
        }

        // this allows binary search based query
        Arrays.sort(tmpOrdinals, Comparator.comparing(lhs -> tmpFieldNames[lhs]));
        Arrays.sort(tmpFieldNames);

        fieldNames = tmpFieldNames;
        ordinals = tmpOrdinals;
    }

    public boolean isSingular() {
        return fieldNames == null;
    }

    /**
     * If the query fails, it returns <code>-1</code>.
     * Querying the ordinal offset of a singular type automatically fails.
     *
     * <p>Note: This mapping is strictly bijective.</p>
     */
    public int ordinalOffsetOfField(@NonNull String field) {
        Preconditions.checkNotNull(field);

        if (ordinals == null) {
            return -1;
        }

        // this requires the field names to be sorted
        int index = Arrays.binarySearch(fieldNames, field);
        if (index < 0) {
            return -1;
        }

        Preconditions.checkElementIndex(index, ordinals.length);

        return ordinals[index];
    }
}
