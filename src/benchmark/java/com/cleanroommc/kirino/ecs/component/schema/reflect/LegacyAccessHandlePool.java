package com.cleanroommc.kirino.ecs.component.schema.reflect;

import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.HashMap;
import java.util.Map;

public class LegacyAccessHandlePool {

    private final Map<Class<?>, Element> membersMap = new Reference2ObjectOpenHashMap<>();

    public void register(@NonNull Class<?> clazz, @NonNull MemberLayout memberLayout) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(memberLayout);

        try {

            Element element = membersMap.computeIfAbsent(clazz, (cls) -> new Element());
            element.memberLayout = memberLayout;
            element.methodHandle = ReflectionUtils.getConstructor(clazz);

            for (String fieldName: memberLayout.fieldNames) {
                VarHandle fieldHandle = ReflectionUtils.getFieldHandleByNameIncludingSuperclasses(clazz, fieldName);
                MethodHandle setterHandle = fieldHandle.toMethodHandle(VarHandle.AccessMode.SET);
                element.setters.put(fieldName, setterHandle);
                MethodHandle getterHandle = fieldHandle.toMethodHandle(VarHandle.AccessMode.GET);
                element.getters.put(fieldName, getterHandle);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean classRegistered(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return membersMap.containsKey(clazz);
    }

    @Nullable
    public Object newClass(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        final Element element = membersMap.get(clazz);

        if (element == null) {
            return null;
        }

        MethodHandle ctor = element.methodHandle;
        try {
            return ctor.invoke();
        } catch (Throwable e) {
            return null;
        }
    }

    public void setFieldValue(@NonNull Class<?> clazz, @NonNull Object target, @NonNull String fieldName, @Nullable Object value) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(fieldName);

        final Element element = membersMap.get(clazz);

        if (element == null) {
            return;
        }

        MethodHandle setter = element.setters.get(fieldName);
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
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(fieldName);

        final Element element = membersMap.get(clazz);

        if (element == null) {
            return null;
        }

        MethodHandle getter = element.getters.get(fieldName);
        if (getter == null) {
            return null;
        }

        try {
            return getter.invoke(target);
        } catch (Throwable e) {
            return null;
        }
    }

    private static class Element {
        private MemberLayout memberLayout;
        private MethodHandle methodHandle;

        private final Map<String, MethodHandle> setters;
        private final Map<String, MethodHandle> getters;

        Element() {
            this.setters = new Object2ObjectOpenHashMap<>();
            this.getters = new Object2ObjectOpenHashMap<>();
        }

        Element(MemberLayout memberLayout, MethodHandle methodHandle, Map<String, MethodHandle> setters, Map<String, MethodHandle> getters) {
            this.memberLayout = memberLayout;
            this.methodHandle = methodHandle;
            this.setters = setters;
            this.getters = getters;
        }
    }
}
