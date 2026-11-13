package com.cleanroommc.kirino.ecs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

class StructRegistry {

    private final Map<Class<?>, Data> structDataByClass = new Reference2ObjectOpenHashMap<>();

    private final Map<String, Data> structData = new Object2ObjectOpenHashMap<>();

    private boolean lockRegistry = false;

    /**
     * This method is the entry point to register struct types.
     * <code>memberLayout.fieldNames</code> must match <code>structDef.fields</code> one by one.
     *
     * @param name         The registry name of the struct
     * @param clazz        The corresponding class of the struct
     * @param memberLayout The metadata of the struct class
     * @param structDef    The actual struct type layout
     */
    public synchronized void registerStructType(
        @NonNull String name,
        @NonNull Class<?> clazz,
        @NonNull MemberLayout memberLayout,
        @NonNull StructDef structDef) {

        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!structData.containsKey(name),
            "Struct \"%s\" is already registered.", name);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(memberLayout);
        Preconditions.checkNotNull(structDef);
        Preconditions.checkArgument(!structDataByClass.containsKey(clazz),
            "Struct class %s is already registered.", clazz.getName());

        final Data registryData = new Data(name, clazz, structDef, memberLayout, -1);

        structData.put(name, registryData);
        structDataByClass.put(clazz, registryData);
    }

    /**
     * Register calls are no longer allowed after the lock call.
     */
    public synchronized void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Map<Data, ObjectCodec> prepared = new Reference2ObjectOpenHashMap<>();
        ObjectCodec.Factory factory = new ObjectCodec.Factory();
        for (Data data : structData.values()) {
            prepared.put(data, factory.prepare(getLayout(data.name)));
        }
        prepared.forEach((data, codec) -> {
            data.codec = codec;
            data.unit = data.layout.unitCount();
        });
        lockRegistry = true;
    }

    @NotNull
    public ImmutableMap<String, Data> getStructDataMap() {
        return ImmutableMap.copyOf(structData);
    }

    public boolean structTypeExists(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return structDataByClass.containsKey(clazz);
    }

    public boolean structTypeExists(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return structData.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = structData.get(name);
        if (data == null) {
            return null;
        }

        return data.memberLayout;
    }

    @Nullable
    public String getStructTypeName(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        Data data = structDataByClass.get(clazz);
        return data == null ? null : data.name;
    }

    @Nullable
    public Class<?> getStructClass(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = structData.get(name);
        return data == null ? null : data.structClass;
    }

    @Nullable
    public StructDef getStructDef(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = structData.get(name);
        if (data == null) {
            return null;
        }

        return data.structDef;
    }
    //</editor-fold>

    @SuppressWarnings("DataFlowIssue")
    public int flattenedUnitCount(@NonNull String name) {
        Preconditions.checkNotNull(name);
        final Data registryData = structData.get(name);
        Preconditions.checkArgument(registryData != null,
            "Struct type %s doesn't exist.", name);

        return flattenedUnitCount(registryData);
    }

    private int flattenedUnitCount(Data registryData) {
        int count = registryData.unit;
        if (count == -1) {
            count = getLayout(registryData.name).unitCount();
            registryData.unit = count;
        }
        return count;
    }

    public SchemaLayout getLayout(String name) {
        Data data = structData.get(Objects.requireNonNull(name));
        Preconditions.checkArgument(data != null, "Struct type %s doesn't exist.", name);
        SchemaLayout layout = data.layout;
        return layout == null ? prepareLayout(name) : layout;
    }

    private synchronized SchemaLayout prepareLayout(String name) {
        Map<String, SchemaLayout> compiled = new Object2ObjectLinkedOpenHashMap<>();
        SchemaLayout layout = compileLayout(name, compiled, new ObjectLinkedOpenHashSet<>());
        compiled.forEach((key, value) -> {
            Data data = structData.get(key);
            data.unit = value.unitCount();
            data.layout = value;
        });
        return layout;
    }

    private SchemaLayout compileLayout(String name, Map<String, SchemaLayout> compiled, Set<String> visiting) {
        Data data = structData.get(name);
        Preconditions.checkArgument(data != null, "Struct type %s doesn't exist.", name);
        if (data.layout != null) {
            return data.layout;
        }
        if (compiled.containsKey(name)) {
            return compiled.get(name);
        }
        if (!visiting.add(name)) {
            throw new IllegalArgumentException("Cyclic struct definition: " + String.join(" -> ", visiting) + " -> " + name);
        }
        SchemaLayout layout = SchemaCompiler.compile(name, data.structClass, data.structDef.fields, data.memberLayout,
            nested -> compileLayout(nested, compiled, visiting));
        visiting.remove(name);
        compiled.put(name, layout);
        return layout;
    }

    private ObjectCodec codec(Data data) {
        ObjectCodec codec = data.codec;
        return codec == null ? prepareCodec(data) : codec;
    }

    private synchronized ObjectCodec prepareCodec(Data data) {
        if (data.codec == null) {
            data.codec = new ObjectCodec(getLayout(data.name));
        }
        return data.codec;
    }

    /**
     * @param name             Struct name must be valid
     * @param fieldAccessChain The field access chain must be valid
     */
    public int getFieldOrdinal(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        return getLayout(name).resolve(fieldAccessChain).ordinal();
    }

    // -----Struct Construction-----

    /**
     * @param name Struct name is not necessarily valid
     * @param args Arguments must match the flattened struct one-by-one
     */
    @Nullable
    public Object newStruct(@NonNull String name, @NonNull Object @NonNull ... args) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(args);
        for (Object arg : args) {
            Objects.requireNonNull(arg);
        }
        Data data = structData.get(name);
        return data == null ? null : codec(data).create(args);
    }

    // -----Struct Deconstruction-----

    public @NonNull Object @NonNull [] flattenStruct(@NonNull Object structInstance) {
        Data data = structDataByClass.get(Objects.requireNonNull(structInstance).getClass());
        if (data == null) {
            throw new IllegalArgumentException("Struct class isn't registered: " + structInstance.getClass().getName());
        }
        return codec(data).flatten(structInstance);
    }

    public static class Data {
        private final String name;
        private final Class<?> structClass;
        private final StructDef structDef;
        private final MemberLayout memberLayout;
        private volatile int unit;
        private volatile SchemaLayout layout;
        private volatile ObjectCodec codec;

        public Data(String name, Class<?> structClass, StructDef structDef, MemberLayout memberLayout, int unit) {
            this.name = name;
            this.structClass = structClass;
            this.structDef = structDef;
            this.memberLayout = memberLayout;
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public Class<?> getStructClass() {
            return structClass;
        }

        public int getUnit() {
            return unit;
        }

        public MemberLayout getMemberLayout() {
            return memberLayout;
        }

        public StructDef getStructDef() {
            return structDef;
        }

        @Override
        public int hashCode() {
            int hash = 31 * structDef.hashCode();
            hash = 31 * hash + memberLayout.hashCode();
            hash = 31 * hash + unit;
            return hash;
        }

        public final Data copy() {
            return new Data(name, structClass, structDef, memberLayout, unit);
        }
    }
}
