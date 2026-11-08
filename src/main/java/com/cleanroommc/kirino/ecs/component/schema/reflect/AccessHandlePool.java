package com.cleanroommc.kirino.ecs.component.schema.reflect;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class AccessHandlePool {
    private final Map<Class<?>, Access> membersMap = new Reference2ObjectOpenHashMap<>();

    public void register(Class<?> clazz, MemberLayout memberLayout) {
        Access access = prepare(clazz, memberLayout);
        membersMap.put(clazz, access);
    }

    public static Access prepare(Class<?> clazz, MemberLayout memberLayout) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(memberLayout);
        try {
            if (Modifier.isAbstract(clazz.getModifiers()) || clazz.isInterface()) {
                throw new IllegalArgumentException("Class must be concrete: " + clazz.getName());
            }
            MethodHandle constructor = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                .findConstructor(clazz, MethodType.methodType(void.class));
            String[] fieldNames = memberLayout.fieldNames.elements();
            MethodHandle[] getters = new MethodHandle[fieldNames.length];
            MethodHandle[] primitiveGetters = new MethodHandle[fieldNames.length];
            MethodHandle[] primitiveSetters = new MethodHandle[fieldNames.length];
            MethodHandle[] setters = new MethodHandle[fieldNames.length];
            Class<?>[] types = new Class<?>[fieldNames.length];
            ImmutableMap.Builder<String, Integer> indexes = ImmutableMap.builder();
            for (int i = 0; i < fieldNames.length; i++) {
                String name = fieldNames[i];
                Field javaField = findField(clazz, name);
                VarHandle field = MethodHandles.privateLookupIn(javaField.getDeclaringClass(), MethodHandles.lookup())
                    .unreflectVarHandle(javaField);
                if (field.coordinateTypes().size() != 1 || !field.isAccessModeSupported(VarHandle.AccessMode.SET)) {
                    throw new IllegalArgumentException("Field must be a mutable instance field: " + clazz.getName() + "." + name);
                }
                getters[i] = field.toMethodHandle(VarHandle.AccessMode.GET).asType(MethodType.methodType(Object.class, Object.class));
                setters[i] = field.toMethodHandle(VarHandle.AccessMode.SET).asType(MethodType.methodType(void.class, Object.class, Object.class));
                Class<?> primitive = MethodType.methodType(field.varType()).unwrap().returnType();
                primitiveGetters[i] = field.toMethodHandle(VarHandle.AccessMode.GET).asType(MethodType.methodType(primitive, Object.class));
                primitiveSetters[i] = field.toMethodHandle(VarHandle.AccessMode.SET).asType(MethodType.methodType(void.class, Object.class, primitive));
                types[i] = field.varType();
                indexes.put(name, i);
            }
            return new Access(clazz, constructor.asType(MethodType.methodType(Object.class)), getters,
                setters, types, indexes.buildOrThrow(), primitiveGetters, primitiveSetters);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot prepare fields of " + clazz.getName(), e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Class<?> current = clazz; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(clazz.getName() + "." + name);
    }

    public boolean classRegistered(Class<?> clazz) {
        return membersMap.containsKey(Objects.requireNonNull(clazz));
    }

    public Object newClass(Class<?> clazz) {
        Access access = membersMap.get(Objects.requireNonNull(clazz));
        return access == null ? null : access.newInstance();
    }

    public void setFieldValue(Class<?> clazz, Object target, String fieldName, Object value) {
        requireAccess(clazz).set(Objects.requireNonNull(target), Objects.requireNonNull(fieldName), value);
    }

    public Object getFieldValue(Class<?> clazz, Object target, String fieldName) {
        return requireAccess(clazz).get(Objects.requireNonNull(target), Objects.requireNonNull(fieldName));
    }

    private Access requireAccess(Class<?> clazz) {
        Access access = membersMap.get(Objects.requireNonNull(clazz));
        if (access == null) {
            throw new IllegalArgumentException("Class isn't registered: " + clazz.getName());
        }
        return access;
    }

    public static final class Access {
        private final MethodHandle[] primitiveGetters;
        private final MethodHandle[] primitiveSetters;
        private final Class<?> javaClass;
        private final MethodHandle constructor;
        private final MethodHandle[] getters;
        private final MethodHandle[] setters;
        private final Class<?>[] types;
        private final ImmutableMap<String, Integer> indexes;

        private Access(Class<?> javaClass, MethodHandle constructor, MethodHandle[] getters,
                       MethodHandle[] setters, Class<?>[] types, ImmutableMap<String, Integer> indexes,
                       MethodHandle[] primitiveGetters, MethodHandle[] primitiveSetters) {
            this.primitiveGetters = primitiveGetters;
            this.primitiveSetters = primitiveSetters;
            this.javaClass = javaClass;
            this.constructor = constructor;
            this.getters = getters;
            this.setters = setters;
            this.types = types;
            this.indexes = indexes;
        }

        public Class<?> fieldType(int index) {
            return types[index];
        }

        public Object newInstance() {
            try {
                return (Object) constructor.invokeExact();
            } catch (Throwable e) {
                throw failure("construct", e);
            }
        }

        public Object get(Object target, String name) {
            return get(target, index(name));
        }

        public void set(Object target, String name, Object value) {
            set(target, index(name), value);
        }

        public Object get(Object target, int index) {
            try {
                return (Object) getters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void set(Object target, int index, Object value) {
            try {
                setters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public byte getByte(Object target, int index) {
            try {
                return (byte) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setByte(Object target, int index, byte value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public short getShort(Object target, int index) {
            try {
                return (short) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setShort(Object target, int index, short value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public int getInt(Object target, int index) {
            try {
                return (int) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setInt(Object target, int index, int value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public long getLong(Object target, int index) {
            try {
                return (long) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setLong(Object target, int index, long value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public float getFloat(Object target, int index) {
            try {
                return (float) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setFloat(Object target, int index, float value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public double getDouble(Object target, int index) {
            try {
                return (double) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setDouble(Object target, int index, double value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        public boolean getBool(Object target, int index) {
            try {
                return (boolean) primitiveGetters[index].invokeExact(target);
            } catch (Throwable e) {
                throw failure("read field " + index, e);
            }
        }

        public void setBool(Object target, int index, boolean value) {
            try {
                primitiveSetters[index].invokeExact(target, value);
            } catch (Throwable e) {
                throw failure("write field " + index, e);
            }
        }

        private int index(String name) {
            Integer index = indexes.get(name);
            if (index == null) {
                throw new IllegalArgumentException("Unknown field " + javaClass.getName() + "." + name);
            }
            return index;
        }

        private RuntimeException failure(String operation, Throwable cause) {
            if (cause instanceof Error error) {
                throw error;
            }
            return new IllegalStateException("Cannot " + operation + " in " + javaClass.getName(), cause);
        }
    }
}
