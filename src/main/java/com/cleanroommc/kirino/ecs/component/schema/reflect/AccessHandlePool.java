package com.cleanroommc.kirino.ecs.component.schema.reflect;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;

public class AccessHandlePool {
    private final Map<Class<?>, MemberLayout> memberLayoutMap = new HashMap<>();
    private final Map<Class<?>, MethodHandle> constructorHandleMap = new HashMap<>();
    private final Map<Class<?>, Map<String, MethodHandle>> setterHandleMap = new HashMap<>();
    private final Map<Class<?>, Map<String, MethodHandle>> getterHandleMap = new HashMap<>();

    public void register(@NonNull Class<?> clazz, @NonNull MemberLayout memberLayout) {
        try {
            MethodHandle ctorHandle = ReflectionUtils.getConstructor(clazz);
            constructorHandleMap.put(clazz, ctorHandle);

            Map<String, MethodHandle> setters = setterHandleMap.computeIfAbsent(clazz, (key) -> new HashMap<>());
            Map<String, MethodHandle> getters = getterHandleMap.computeIfAbsent(clazz, (key) -> new HashMap<>());
            for (String fieldName: memberLayout.fieldNames) {
                VarHandle fieldHandle = ReflectionUtils.getFieldHandleByNameIncludingSuperclasses(clazz, fieldName);
                MethodHandle setterHandle = fieldHandle.toMethodHandle(VarHandle.AccessMode.SET);
                setters.put(fieldName, setterHandle);
                MethodHandle getterHandle = fieldHandle.toMethodHandle(VarHandle.AccessMode.GET);
                getters.put(fieldName, getterHandle);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        memberLayoutMap.put(clazz, memberLayout);
    }

    public boolean classRegistered(@NonNull Class<?> clazz) {
        return memberLayoutMap.containsKey(clazz);
    }

    @Nullable
    public Object newClass(@NonNull Class<?> clazz) {
        if (!classRegistered(clazz)) {
            return null;
        }

        MethodHandle ctor = constructorHandleMap.get(clazz);
        try {
            return ctor.invoke();
        } catch (Throwable e) {
            return null;
        }
    }

    public void setFieldValue(@NonNull Class<?> clazz, @NonNull Object target, @NonNull String fieldName, @Nullable Object value) {
        if (!classRegistered(clazz)) {
            return;
        }

        MethodHandle setter = setterHandleMap.get(clazz).get(fieldName);
        if (setter == null) {
            return;
        }

        try {
            setter.invoke(target, value);
        } catch (Throwable ignore) {
        }
    }

    @Nullable
    public Object getFieldValue(@NonNull Class<?> clazz, @NonNull Object target, @NonNull String fieldName) {
        if (!classRegistered(clazz)) {
            return null;
        }

        MethodHandle getter = getterHandleMap.get(clazz).get(fieldName);
        if (getter == null) {
            return null;
        }

        try {
            return getter.invoke(target);
        } catch (Throwable e) {
            return null;
        }
    }
}
