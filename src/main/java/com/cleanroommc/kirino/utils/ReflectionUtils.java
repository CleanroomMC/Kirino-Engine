package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A set of utility functions to get a {@link MethodHandle} for methods/fields/constructors.
 * <br>
 * Whenever possible, cache the results in a {@code static final} field. Another way to cache it is to store them in a {@code record}
 * and store that reference in a {@code static final} field.
 *
 * @see com.cleanroommc.kirino.KirinoCore.MethodHolder An example of inlinable cached handles
 * @see <a href="https://jornvernee.github.io/methodhandles/2024/01/19/methodhandle-primer.html#method-handle-inlining">For more details about caching/inlining</a>
 */
public final class ReflectionUtils {
    private static final boolean isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();
    // Consider using ImagineBreaker if this lookup isn't privileged enough
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Get a lookup allowing private access.
     * @param clazz The class to allow private access in
     * @return A {@link java.lang.invoke.MethodHandles.Lookup} with private access to the target class
     */
    private static Optional<MethodHandles.Lookup> getPrivateLookup(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(clazz, LOOKUP);
        } catch (IllegalAccessException e) {
            return Optional.empty();
        }
        return Optional.of(lookup);
    }

    /**
     * Check if the target field is static.
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name, possibly null
     * @return An {@link Optional} containing whether the field is static, empty if the field was not found
     */
    private static Optional<Boolean> isStaticField(Class<?> clazz, String fieldName, @Nullable String obfFieldName) {
        Field field = findDeclaredField(clazz, fieldName, obfFieldName);
        if (field == null) {
            field = findField(clazz, fieldName, obfFieldName);
        }
        return Optional.ofNullable(field)
                .map(Field::getModifiers)
                .map(Modifier::isStatic);
    }

    /**
     * Check if the target method is static.
     * @param clazz The declaring class of the method
     * @param methodName The method name
     * @param obfMethodName The obfuscated method name, possibly null
     * @param params The method parameters
     * @return An {@link Optional} containing whether the method is static, empty if the method was not found
     */
    private static Optional<Boolean> isStaticMethod(Class<?> clazz, String methodName, @Nullable String obfMethodName, Class<?>... params) {
        Method method = findDeclaredMethod(clazz, methodName, obfMethodName, params);
        if (method == null) {
            method = findMethod(clazz, methodName, obfMethodName, params);
        }
        return Optional.ofNullable(method)
                .map(Method::getModifiers)
                .map(Modifier::isStatic);
    }

    public static VarHandle getFieldHandleByNameIncludingSuperclasses(Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Optional<MethodHandles.Lookup> lookupResult = getPrivateLookup(clazz);
        Preconditions.checkState(lookupResult.isPresent());

        MethodHandles.Lookup lookup = lookupResult.get();
        List<Field> fields = getAllFieldsIncludingSuperclasses(clazz);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return lookup.unreflectVarHandle(field);
            }
        }
        throw new NoSuchFieldException("Cannot find " + fieldName + " from " + clazz.getName() + " including its superclasses.");
    }

    public static List<Field> getAllFieldsIncludingSuperclasses(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field[] declaredFields = current.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            current = current.getSuperclass();
        }
        return fields;
    }

    //<editor-fold desc="find field">
    @Nullable
    public static Field findDeclaredField(Class<?> clazz, String fieldName, @Nullable String obfFieldName) {
        boolean hasObfName = obfFieldName != null && !obfFieldName.isEmpty() && !isDeobf;
        Field field = null;
        try {
            field = clazz.getDeclaredField(hasObfName ? obfFieldName : fieldName);
        } catch (NoSuchFieldException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    field = clazz.getDeclaredField(fieldName);
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        return field;
    }

    @Nullable
    public static Field findDeclaredField(Class<?> clazz, String fieldName) {
        return findDeclaredField(clazz, fieldName, null);
    }

    @Nullable
    public static Field findField(Class<?> clazz, String fieldName, @Nullable String obfFieldName) {
        boolean hasObfName = obfFieldName != null && !obfFieldName.isEmpty() && !isDeobf;
        Field field = null;
        try {
            field = clazz.getField(hasObfName ? obfFieldName : fieldName);
        } catch (NoSuchFieldException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    field = clazz.getField(fieldName);
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
        return field;
    }

    @Nullable
    public static Field findField(Class<?> clazz, String fieldName) {
       return findField(clazz, fieldName, null);
    }
    //</editor-fold>

    //<editor-fold desc="get getter">
    /**
     * The target field will be looked up and a getter for it will be returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name, possibly null
     * @param fieldClass The actual class of the field
     * @return A {@link MethodHandle} representing a getter for the field
     */
    @Nullable
    public static MethodHandle getFieldGetter(Class<?> clazz, String fieldName, @Nullable String obfFieldName, Class<?> fieldClass) {
        Optional<MethodHandles.Lookup> lookupResult = getPrivateLookup(clazz);
        Preconditions.checkState(lookupResult.isPresent());

        MethodHandles.Lookup lookup = lookupResult.get();
        Optional<Boolean> isStaticResult = isStaticField(clazz, fieldName, obfFieldName);
        // Didn't find the field
        if (isStaticResult.isEmpty()) {
            return null;
        }
        boolean isStatic = isStaticResult.get();

        boolean hasObfName = obfFieldName != null && !obfFieldName.isEmpty() && !isDeobf;
        MethodHandle handle = null;
        try {
            String name = hasObfName ? obfFieldName : fieldName;
            if (isStatic) {
                handle = lookup.findStaticGetter(clazz, name, fieldClass);
            } else {
                handle = lookup.findGetter(clazz, name, fieldClass);
            }
        } catch (NoSuchFieldException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    if (isStatic) {
                        handle = lookup.findStaticGetter(clazz, fieldName, fieldClass);
                    } else {
                        handle = lookup.findGetter(clazz, fieldName, fieldClass);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
        return handle;
    }

    /**
     * The target field will be looked up and a getter for it will be returned. Non-obfuscated version.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param fieldClass The actual class of the field
     * @return A {@link MethodHandle} representing a getter for the field
     */
    @Nullable
    public static MethodHandle getFieldGetter(Class<?> clazz, String fieldName, Class<?> fieldClass) {
        return getFieldGetter(clazz, fieldName, null, fieldClass);
    }
    //</editor-fold>

    //<editor-fold desc="get setter">
    /**
     * The target field will be looked up and a setter for it will be returned.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param obfFieldName The obfuscated field name, possibly null
     * @param fieldClass The actual class of the field
     * @return A {@link MethodHandle} representing a setter for the field
     */
    @Nullable
    public static MethodHandle getFieldSetter(Class<?> clazz, String fieldName, @Nullable String obfFieldName, Class<?> fieldClass) {
        Optional<MethodHandles.Lookup> lookupResult = getPrivateLookup(clazz);
        Preconditions.checkState(lookupResult.isPresent());

        MethodHandles.Lookup lookup = lookupResult.get();
        Optional<Boolean> isStaticResult = isStaticField(clazz, fieldName, obfFieldName);
        // Didn't find the field
        if (isStaticResult.isEmpty()) {
            return null;
        }

        boolean isStatic = isStaticResult.get();
        boolean hasObfName = obfFieldName != null && !obfFieldName.isEmpty() && !isDeobf;
        MethodHandle handle = null;
        try {
            String name = hasObfName ? obfFieldName : fieldName;
            if (isStatic) {
                handle = lookup.findStaticSetter(clazz, name, fieldClass);
            } else {
                handle = lookup.findSetter(clazz, name, fieldClass);
            }
        } catch (NoSuchFieldException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    if (isStatic) {
                        handle = lookup.findStaticSetter(clazz, fieldName, fieldClass);
                    } else {
                        handle = lookup.findSetter(clazz, fieldName, fieldClass);
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
        return handle;
    }

    /**
     * The target field will be looked up and a setter for it will be returned. Non-obfuscated version.
     *
     * @param clazz The declaring class of the field
     * @param fieldName The field name
     * @param fieldClass The actual class of the field
     * @return A {@link MethodHandle} representing a setter for the field
     */
    @Nullable
    public static MethodHandle getFieldSetter(Class<?> clazz, String fieldName, Class<?> fieldClass) {
        return getFieldSetter(clazz, fieldName, null, fieldClass);
    }
    //</editor-fold>

    //<editor-fold desc="find method">
    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, @Nullable String obfMethodName, Class<?>... params) {
        boolean hasObfName = obfMethodName != null && !obfMethodName.isEmpty() && !isDeobf;
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(hasObfName ? obfMethodName : methodName, params);
        } catch (NoSuchMethodException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    method = clazz.getDeclaredMethod(methodName, params);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return method;
    }

    @Nullable
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... params) {
        return findDeclaredMethod(clazz, methodName, null, params);
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, @Nullable String obfMethodName, Class<?>... params) {
        boolean hasObfName = obfMethodName != null && !obfMethodName.isEmpty() && !isDeobf;
        Method method = null;
        try {
            method = clazz.getMethod(hasObfName ? obfMethodName : methodName, params);
        } catch (NoSuchMethodException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    method = clazz.getMethod(methodName, params);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return method;
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... params) {
        return findMethod(clazz, methodName, null, params);
    }
    //</editor-fold>

    //<editor-fold desc="get method handle">
    /**
     * The target method will be looked up and returned.
     * <br><br>
     * This method does not find any method outlined by {@link java.lang.invoke.MethodHandles.Lookup#findSpecial(Class, String, MethodType, Class)}.
     *
     * @param clazz The declaring class of the method
     * @param methodName The method name
     * @param obfMethodName The obfuscated method name, possibly null
     * @param returnClass The class of the return value of the method
     * @param params The class(es) of the parameters of the method
     * @return A {@link MethodHandle} representing the method
     */
    @Nullable
    public static MethodHandle getMethod(Class<?> clazz, String methodName, @Nullable String obfMethodName,
                                         Class<?> returnClass, Class<?>... params) {
        Optional<MethodHandles.Lookup> lookupResult = getPrivateLookup(clazz);
        Preconditions.checkState(lookupResult.isPresent());

        MethodHandles.Lookup lookup = lookupResult.get();
        Optional<Boolean> isStaticResult = isStaticMethod(clazz, methodName, obfMethodName, params);
        // Didn't find the method
        if (isStaticResult.isEmpty()) {
            return null;
        }
        boolean isStatic = isStaticResult.get();

        MethodType methodType = MethodType.methodType(returnClass, params);
        boolean hasObfName = obfMethodName != null && !obfMethodName.isEmpty() && !isDeobf;
        MethodHandle handle = null;
        try {
            String name = hasObfName ? obfMethodName : methodName;
            if (isStatic) {
                handle = lookup.findStatic(clazz, name, methodType);
            } else {
                handle = lookup.findVirtual(clazz, name, methodType);
            }
        } catch (NoSuchMethodException e) {
            try {
                // Tried obf name, now try deobf name
                if (hasObfName) {
                    if (isStatic) {
                        handle = lookup.findStatic(clazz, methodName, methodType);
                    } else {
                        handle = lookup.findVirtual(clazz, methodName, methodType);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                return null;
            }
        } catch (IllegalAccessException e) {
            return null;
        }
        return handle;
    }

    @Nullable
    public static MethodHandle getMethod(Class<?> clazz, String methodName,
                                         Class<?> returnClass, Class<?>... params) {
        return getMethod(clazz, methodName, null, returnClass, params);
    }
    //</editor-fold>

    //<editor-fold desc="get constructor handle">
    @Nullable
    public static MethodHandle getConstructor(Class<?> clazz, Class<?>... params) {
        Optional<MethodHandles.Lookup> lookupResult = getPrivateLookup(clazz);
        Preconditions.checkState(lookupResult.isPresent());

        MethodHandles.Lookup lookup = lookupResult.get();
        MethodHandle handle;
        try {
            handle = lookup.findConstructor(clazz, MethodType.methodType(void.class, params));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return null;
        }
        return handle;
    }
    //</editor-fold>
}
