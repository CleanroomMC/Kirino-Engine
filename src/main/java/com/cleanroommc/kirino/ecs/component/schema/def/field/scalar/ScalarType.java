package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.joml.*;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A {@link ScalarType} is not by definition a scalar but more like built-in atomic types.
 * A {@link ScalarType} can be flattened into {@link FlattenedScalarType} which is strictly a scalar mathematically.
 */
public enum ScalarType implements Scalar {
    BYTE{
        @Override
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
    SHORT {
        @Override
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
    INT {
        @Override
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
    LONG {
        @Override
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
    FLOAT {
        @Override
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
    DOUBLE {
        @Override
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
    BOOL {
        @Override
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
    VEC2("x", "y") {
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 2) {
                if (args[0] instanceof Float x && args[1] instanceof Float y) {
                    return new Vector2f(x,y);
                }
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector2f,
                    "Expected a org.joml.Vector2f. Got %s instead.", scalarInstance.getClass().getName());

            Vector2f obj = (Vector2f) scalarInstance;
            return new Object[]{obj.x, obj.y};
        }
    },
    VEC3("x", "y", "z") {
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 3) {
                if (args[0] instanceof Float x
                        && args[1] instanceof Float y
                        && args[2] instanceof Float z) {
                    return new Vector3f(x,y,z);
                }
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector3f,
                    "Expected a org.joml.Vector3f. Got %s instead.", scalarInstance.getClass().getName());

            Vector3f obj = (Vector3f) scalarInstance;
            return new Object[]{obj.x, obj.y, obj.z};
        }
    },
    VEC4("x", "y", "z", "w") {
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 4) {
                if (args[0] instanceof Float x
                        && args[1] instanceof Float y
                        && args[2] instanceof Float z
                        && args[3] instanceof Float w) {
                    return new Vector4f(x,y,z,w);
                }
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Vector4f,
                    "Expected a org.joml.Vector4f. Got %s instead.", scalarInstance.getClass().getName());

            Vector4f obj = (Vector4f) scalarInstance;
            return new Object[]{obj.x, obj.y, obj.z, obj.w};
        }
    },
    MAT3("m00", "m01", "m02",
                "m10", "m11", "m12",
                "m20", "m21", "m22") {
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 9) {
                for (int i = 0; i < args.length; i++) {
                    if (!(args[i] instanceof Float)) {
                        return null;
                    }
                }
                return new Matrix3f(
                        (float) args[0],
                        (float) args[1],
                        (float) args[2],
                        (float) args[3],
                        (float) args[4],
                        (float) args[5],
                        (float) args[6],
                        (float) args[7],
                        (float) args[8]);
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Matrix3f,
                    "Expected a org.joml.Matrix3f. Got %s instead.", scalarInstance.getClass().getName());

            Matrix3f matrix3f = (Matrix3f) scalarInstance;
            return new Object[]{matrix3f.m00, matrix3f.m01, matrix3f.m02,
                    matrix3f.m10, matrix3f.m11, matrix3f.m12,
                    matrix3f.m20, matrix3f.m21, matrix3f.m22};
        }
    },
    MAT4("m00", "m01", "m02", "m03",
                "m10", "m11", "m12", "m13",
                "m20", "m21", "m22", "m23",
                "m30", "m31", "m32", "m33") {
        @Override
        public Object newScalar(@NonNull Object @NonNull ... args) {
            if (args.length == 16) {
                for (int i = 0; i < args.length; i++) {
                    if (!(args[i] instanceof Float)) {
                        return null;
                    }
                }
                return new Matrix4f(
                        (float) args[0],
                        (float) args[1],
                        (float) args[2],
                        (float) args[3],
                        (float) args[4],
                        (float) args[5],
                        (float) args[6],
                        (float) args[7],
                        (float) args[8],
                        (float) args[9],
                        (float) args[10],
                        (float) args[11],
                        (float) args[12],
                        (float) args[13],
                        (float) args[14],
                        (float) args[15]);
            }
            return null;
        }

        @Override
        public @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance) {
            Preconditions.checkArgument(scalarInstance instanceof Matrix4f,
                    "Expected a org.joml.Matrix4f. Got %s instead.", scalarInstance.getClass().getName());

            Matrix4f matrix4f = (Matrix4f) scalarInstance;
            return new Object[]{matrix4f.m00(), matrix4f.m01(), matrix4f.m02(), matrix4f.m03(),
                    matrix4f.m10(), matrix4f.m11(), matrix4f.m12(), matrix4f.m13(),
                    matrix4f.m20(), matrix4f.m21(), matrix4f.m22(), matrix4f.m23(),
                    matrix4f.m30(), matrix4f.m31(), matrix4f.m32(), matrix4f.m33()};
        }
    };

    public final String[] fieldNames;
    private final Integer[] ordinals;

    ScalarType(String... fields) {
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
