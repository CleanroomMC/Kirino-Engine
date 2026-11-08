package com.cleanroommc.kirino.ecs.component.schema.codec;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import org.joml.*;

public final class ScalarCodec {
    private ScalarCodec() {
    }

    public static Object create(ScalarType type, PrimitiveValues values, int start, int row) {
        return switch (type) {
            case BYTE -> values.getByte(start, row);
            case SHORT -> values.getShort(start, row);
            case INT -> values.getInt(start, row);
            case LONG -> values.getLong(start, row);
            case FLOAT -> values.getFloat(start, row);
            case DOUBLE -> values.getDouble(start, row);
            case BOOL -> values.getBool(start, row);
            case VEC2 -> new Vector2f(values.getFloat(start, row), values.getFloat(start + 1, row));
            case VEC3 -> new Vector3f(values.getFloat(start, row), values.getFloat(start + 1, row), values.getFloat(start + 2, row));
            case VEC4 ->
                new Vector4f(values.getFloat(start, row), values.getFloat(start + 1, row), values.getFloat(start + 2, row), values.getFloat(start + 3, row));
            case MAT3 -> new Matrix3f(values.getFloat(start, row), values.getFloat(start + 1, row), values.getFloat(start + 2, row),
                values.getFloat(start + 3, row), values.getFloat(start + 4, row), values.getFloat(start + 5, row),
                values.getFloat(start + 6, row), values.getFloat(start + 7, row), values.getFloat(start + 8, row));
            case MAT4 ->
                new Matrix4f(values.getFloat(start, row), values.getFloat(start + 1, row), values.getFloat(start + 2, row), values.getFloat(start + 3, row),
                    values.getFloat(start + 4, row), values.getFloat(start + 5, row), values.getFloat(start + 6, row), values.getFloat(start + 7, row),
                    values.getFloat(start + 8, row), values.getFloat(start + 9, row), values.getFloat(start + 10, row), values.getFloat(start + 11, row),
                    values.getFloat(start + 12, row), values.getFloat(start + 13, row), values.getFloat(start + 14, row), values.getFloat(start + 15, row));
        };
    }

    public static void write(ScalarType type, Object value, PrimitiveValues values, int start, int row) {
        switch (type) {
            case BYTE -> values.setByte(start, row, (byte) value);
            case SHORT -> values.setShort(start, row, (short) value);
            case INT -> values.setInt(start, row, (int) value);
            case LONG -> values.setLong(start, row, (long) value);
            case FLOAT -> values.setFloat(start, row, (float) value);
            case DOUBLE -> values.setDouble(start, row, (double) value);
            case BOOL -> values.setBool(start, row, (boolean) value);
            case VEC2 -> {
                Vector2f vector = (Vector2f) value;
                values.setFloat(start, row, vector.x);
                values.setFloat(start + 1, row, vector.y);
            }
            case VEC3 -> {
                Vector3f vector = (Vector3f) value;
                values.setFloat(start, row, vector.x);
                values.setFloat(start + 1, row, vector.y);
                values.setFloat(start + 2, row, vector.z);
            }
            case VEC4 -> {
                Vector4f vector = (Vector4f) value;
                values.setFloat(start, row, vector.x);
                values.setFloat(start + 1, row, vector.y);
                values.setFloat(start + 2, row, vector.z);
                values.setFloat(start + 3, row, vector.w);
            }
            case MAT3 -> {
                Matrix3f matrix = (Matrix3f) value;
                for (int column = 0; column < 3; column++) {
                    for (int matrixRow = 0; matrixRow < 3; matrixRow++) {
                        values.setFloat(start + column * 3 + matrixRow, row, matrix.get(column, matrixRow));
                    }
                }
            }
            case MAT4 -> {
                Matrix4f matrix = (Matrix4f) value;
                for (int column = 0; column < 4; column++) {
                    for (int matrixRow = 0; matrixRow < 4; matrixRow++) {
                        values.setFloat(start + column * 4 + matrixRow, row, matrix.get(column, matrixRow));
                    }
                }
            }
        }
    }
}
