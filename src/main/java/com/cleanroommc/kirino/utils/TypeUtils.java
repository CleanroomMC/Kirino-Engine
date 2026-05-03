package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class TypeUtils {

    private TypeUtils() {
    }

    /**
     * It ignores the difference between primitives and wrapped primitives, like <code>int</code> and <code>Integer</code>.
     */
    public static boolean looseTypeCheck(@NonNull Class<?> class1, @NonNull Class<?> class2) {
        Preconditions.checkNotNull(class1);
        Preconditions.checkNotNull(class2);

        if (class1.equals(class2)) {
            return true;
        } else if (isIntOrWrappedInt(class1) && isIntOrWrappedInt(class2)) {
            return true;
        } else if (isLongOrWrappedLong(class1) && isLongOrWrappedLong(class2)) {
            return true;
        } else if (isShortOrWrappedShort(class1) && isShortOrWrappedShort(class2)) {
            return true;
        } else if (isByteOrWrappedByte(class1) && isByteOrWrappedByte(class2)) {
            return true;
        } else if (isDoubleOrWrappedDouble(class1) && isDoubleOrWrappedDouble(class2)) {
            return true;
        } else if (isFloatOrWrappedFloat(class1) && isFloatOrWrappedFloat(class2)) {
            return true;
        } else if (isCharacterOrWrappedCharacter(class1) && isCharacterOrWrappedCharacter(class2)) {
            return true;
        } else {
            return isBooleanOrWrappedBoolean(class1) && isBooleanOrWrappedBoolean(class2);
        }
    }

    public static boolean isPrimitiveOrWrappedPrimitive(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.isPrimitive() || isWrappedPrimitive(clazz);
    }

    public static boolean isWrappedPrimitive(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Short.class ||
                clazz == Byte.class ||
                clazz == Double.class ||
                clazz == Float.class ||
                clazz == Character.class ||
                clazz == Boolean.class;
    }

    /**
     * @return It returns <code>null</code> if the input is not a primitive type.
     */
    @Nullable
    public static Class<?> toWrappedPrimitive(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return switch (clazz.getName()) {
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "short" -> Short.class;
            case "byte" -> Byte.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "char" -> Character.class;
            case "boolean" -> Boolean.class;
            default -> null;
        };
    }

    /**
     * @return It returns <code>null</code> if the input is not a wrapped primitive type.
     */
    @Nullable
    public static Class<?> toPrimitive(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        if (clazz == Integer.class) {
            return int.class;
        }
        if (clazz == Long.class) {
            return long.class;
        }
        if (clazz == Short.class) {
            return short.class;
        }
        if (clazz == Byte.class) {
            return byte.class;
        }
        if (clazz == Double.class) {
            return double.class;
        }
        if (clazz == Float.class) {
            return float.class;
        }
        if (clazz == Character.class) {
            return char.class;
        }
        if (clazz == Boolean.class) {
            return boolean.class;
        }
        return null;
    }

    public static boolean isIntOrWrappedInt(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("int") || clazz.equals(Integer.class);
    }

    public static boolean isLongOrWrappedLong(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("long") || clazz.equals(Long.class);
    }

    public static boolean isShortOrWrappedShort(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("short") || clazz.equals(Short.class);
    }

    public static boolean isByteOrWrappedByte(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("byte") || clazz.equals(Byte.class);
    }

    public static boolean isDoubleOrWrappedDouble(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("double") || clazz.equals(Double.class);
    }

    public static boolean isFloatOrWrappedFloat(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("float") || clazz.equals(Float.class);
    }

    public static boolean isCharacterOrWrappedCharacter(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("char") || clazz.equals(Character.class);
    }

    public static boolean isBooleanOrWrappedBoolean(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return clazz.getName().equals("boolean") || clazz.equals(Boolean.class);
    }
}
