package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.ObjectCodec;
import com.cleanroommc.kirino.ecs.component.schema.meta.SchemaLayout;
import com.cleanroommc.kirino.ecs.component.schema.meta.SchemaCompiler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ComponentRegistry {
    private final FieldRegistry fieldRegistry;

    public ComponentRegistry(FieldRegistry fieldRegistry) {
        this.fieldRegistry = fieldRegistry;
    }

    private final Map<Class<?>, Data> componentDataByClass = new Reference2ObjectOpenHashMap<>();
    private final Map<String, Data> componentData = new Object2ObjectOpenHashMap<>();

    private boolean lockRegistry = false;

    /**
     * This method is the entry point to register components.
     * <code>memberLayout.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberLayout The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public synchronized void registerComponent(
            @NonNull String name,
            @NonNull Class<? extends CleanComponent> clazz,
            @NonNull MemberLayout memberLayout,
            @NonNull String @NonNull ... fieldTypeNames) {

        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!componentData.containsKey(name),
                "Component \"%s\" is already registered.", name);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(memberLayout);
        Preconditions.checkNotNull(fieldTypeNames);
        for (String fieldType : fieldTypeNames) {
            Preconditions.checkNotNull(fieldType);
        }

        Preconditions.checkArgument(!componentDataByClass.containsKey(clazz), "Component class %s is already registered.", clazz.getName());
        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldDef(fieldTypeName);
            Preconditions.checkArgument(field != null,
                    "Field type %s doesn't exist.", fieldTypeName);

            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(name, fields, fieldTypeNames);
        Data data = new Data(name, clazz, componentDesc, memberLayout,
            SchemaCompiler.compile(name, clazz, fields, memberLayout, fieldRegistry.structRegistry::getLayout));
        componentDataByClass.put(clazz, data);
        componentData.put(name, data);
    }

    /**
     * Register calls are no longer allowed after the lock call.
     */
    public synchronized void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Map<Data, ObjectCodec> prepared = new IdentityHashMap<>();
        ObjectCodec.Factory factory = new ObjectCodec.Factory();
        for (Data data : componentData.values()) {
            prepared.put(data, factory.prepare(data.layout));
        }
        prepared.forEach((data, codec) -> data.codec = codec);
        lockRegistry = true;
    }

    //<editor-fold desc="getters & queries">
    @NonNull
    public ImmutableMap<String, Data> getComponentDataMap() {
        return ImmutableMap.copyOf(componentData);
    }

    public boolean componentExists(@NonNull Class<? extends CleanComponent> clazz) {
        Preconditions.checkNotNull(clazz);

        return componentDataByClass.containsKey(clazz);
    }

    public boolean componentExists(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return componentData.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = componentData.get(name);
        if(data == null) {
            return null;
        }

        return data.memberLayout;
    }

    @Nullable
    public String getComponentName(@NonNull Class<? extends CleanComponent> clazz) {
        Preconditions.checkNotNull(clazz);

        Data data = componentDataByClass.get(clazz);
        if(data == null) {
            return null;
        }

        return data.name;
    }

    @Nullable
    public Class<? extends CleanComponent> getComponentClass(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = componentData.get(name);
        if(data == null) {
            return null;
        }

        return data.componentClass;
    }

    @Nullable
    public ComponentDesc getComponentDesc(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = componentData.get(name);
        if(data == null) {
            return null;
        }

        return data.componentDesc;
    }

    //</editor-fold>

    /**
     * @param name Component name must be valid
     * @param fieldAccessChain The field access chain must be valid
     */
    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        return getLayout(name).resolve(fieldAccessChain).ordinal();
    }

    public SchemaLayout getLayout(String name) {
        Data data = componentData.get(Objects.requireNonNull(name));
        Preconditions.checkArgument(data != null, "Component type %s doesn't exist.", name);
        return data.layout;
    }

    public SchemaLayout getLayout(Class<? extends CleanComponent> clazz) {
        Data data = componentDataByClass.get(Objects.requireNonNull(clazz));
        Preconditions.checkArgument(data != null, "Component class %s isn't registered.", clazz);
        return data.layout;
    }

    public ObjectCodec getCodec(Class<? extends CleanComponent> component) {
        Data data = componentDataByClass.get(Objects.requireNonNull(component));
        Preconditions.checkArgument(data != null, "Component class %s isn't registered.", component);
        return codec(data);
    }

    private ObjectCodec codec(Data data) {
        ObjectCodec codec = data.codec;
        return codec == null ? prepareCodec(data) : codec;
    }

    private synchronized ObjectCodec prepareCodec(Data data) {
        if (data.codec == null) {
            data.codec = new ObjectCodec(data.layout);
        }
        return data.codec;
    }

    // -----Component Construction-----

    /**
     * @param name Component name is not necessarily valid
     * @param args Arguments must match the flattened component one-by-one
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public CleanComponent newComponent(@NonNull String name, @NonNull Object @NonNull ... args) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(args);
        for (Object arg : args) {
            Objects.requireNonNull(arg);
        }
        Data data = componentData.get(name);
        return data == null ? null : (CleanComponent) codec(data).create(args);
    }

    // -----Component Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object @NonNull [] flattenComponent(@NonNull CleanComponent component) {
        Data data = componentDataByClass.get(Objects.requireNonNull(component).getClass());
        if (data == null) {
            throw new IllegalArgumentException("Component class isn't registered: " + component.getClass().getName());
        }
        return codec(data).flatten(component);
    }

    public static class Data {

        public final String name;
        public final Class<? extends CleanComponent> componentClass;
        public final ComponentDesc componentDesc;
        public final MemberLayout memberLayout;
        public final SchemaLayout layout;
        private volatile ObjectCodec codec;

        public Data(String name, Class<? extends CleanComponent> componentClass, ComponentDesc componentDesc, MemberLayout memberLayout, SchemaLayout layout) {
            this.name = name;
            this.componentClass = componentClass;
            this.componentDesc = componentDesc;
            this.memberLayout = memberLayout;
            this.layout = layout;
        }
    }
}
