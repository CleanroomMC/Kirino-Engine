package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.AccessHandlePool;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ComponentRegistry {
    private final FieldRegistry fieldRegistry;

    public ComponentRegistry(FieldRegistry fieldRegistry) {
        this.fieldRegistry = fieldRegistry;
    }

    private final BiMap<String, Class<? extends CleanComponent>> comNameClassMapping = HashBiMap.create();
    private final Map<String, ComponentDesc> componentDescMap = new HashMap<>();
    private final Map<String, ComponentDescFlattened> componentDescFlattenedMap = new HashMap<>();
    private final Map<String, MemberLayout> classMemberLayoutMap = new HashMap<>();

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
    public void registerComponent(
            @NonNull String name,
            @NonNull Class<? extends CleanComponent> clazz,
            @NonNull MemberLayout memberLayout,
            @NonNull String @NonNull ... fieldTypeNames) {

        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!comNameClassMapping.containsKey(name),
                "Component \"%s\" is already registered.", name);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(memberLayout);
        Preconditions.checkNotNull(fieldTypeNames);
        for (String fieldType : fieldTypeNames) {
            Preconditions.checkNotNull(fieldType);
        }

        comNameClassMapping.put(name, clazz);
        classMemberLayoutMap.put(name, memberLayout);

        List<FieldDef> fields = new ArrayList<>();
        for (String fieldTypeName : fieldTypeNames) {
            FieldDef field = fieldRegistry.getFieldDef(fieldTypeName);
            Preconditions.checkArgument(field != null,
                    "Field type %s doesn't exist.", fieldTypeName);

            fields.add(field);
        }

        ComponentDesc componentDesc = new ComponentDesc(name, fields, fieldTypeNames);
        componentDescMap.put(name, componentDesc);
        componentDescFlattenedMap.put(name, new ComponentDescFlattened(componentDesc, fieldRegistry));
    }

    /**
     * Register call is no longer allowed after the lock call.
     */
    public void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");

        lockRegistry = true;
    }

    //<editor-fold desc="getters & queries">
    @NonNull
    public ImmutableMap<String, ComponentDesc> getComponentDescMap() {
        return ImmutableMap.copyOf(componentDescMap);
    }

    @NonNull
    public ImmutableMap<String, ComponentDescFlattened> getComponentDescFlattenedMap() {
        return ImmutableMap.copyOf(componentDescFlattenedMap);
    }

    public boolean componentExists(@NonNull Class<? extends CleanComponent> clazz) {
        Preconditions.checkNotNull(clazz);

        // BiMap itself has an inverse cache
        return comNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean componentExists(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return comNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return classMemberLayoutMap.get(name);
    }

    @Nullable
    public String getComponentName(@NonNull Class<? extends CleanComponent> clazz) {
        Preconditions.checkNotNull(clazz);

        // BiMap itself has an inverse cache
        return comNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<? extends CleanComponent> getComponentClass(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return comNameClassMapping.get(name);
    }

    @Nullable
    public ComponentDesc getComponentDesc(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return componentDescMap.get(name);
    }

    @Nullable
    public ComponentDescFlattened getComponentDescFlattened(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return componentDescFlattenedMap.get(name);
    }
    //</editor-fold>

    private static String formatFieldAccessChain(String... fieldAccessChain) {
        if (fieldAccessChain.length == 0) {
            return "\"\"";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\".");
        for (int i = 0; i < fieldAccessChain.length; i++) {
            builder.append(fieldAccessChain[i]);
            if (i != fieldAccessChain.length - 1) {
                builder.append(".");
            }
        }
        builder.append("\"");
        return builder.toString();
    }

    /**
     * @param name Component name must be valid
     * @param fieldAccessChain The field access chain must be valid
     */
    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(fieldAccessChain);
        for (String field : fieldAccessChain) {
            Preconditions.checkNotNull(field);
        }
        Preconditions.checkArgument(componentExists(name),
                "Component type %s doesn't exist.", name);
        Preconditions.checkArgument(fieldAccessChain.length != 0,
                "The given \"fieldAccessChain\"=%s must not be empty.",
                formatFieldAccessChain(fieldAccessChain));

        MemberLayout memberLayout = getClassMemberLayout(name);
        int index = 0;
        boolean match = false;
        while (!match && index < memberLayout.fieldNames.size()) {
            if (memberLayout.fieldNames.get(index).equals(fieldAccessChain[0])) {
                match = true;
            } else {
                index++;
            }
        }

        Preconditions.checkArgument(match,
                "Can't find a field (from component \"%s\") that matches the \"fieldAccessChain\"=%s.",
                name, formatFieldAccessChain(fieldAccessChain));

        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        int ordinal = 0;
        for (int i = 0; i < index; i++) {
            ordinal += componentDescFlattened.fields.get(i).getUnitCount();
        }

        ComponentDesc componentDesc = getComponentDesc(name);
        FieldDef fieldDef = componentDesc.fields.get(index);
        // scalar field
        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            // non flattenable: early escape
            if (fieldDef.scalarType.isSingular()) {
                if (fieldAccessChain.length == 1) {
                    return ordinal;
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field (for component \"%s\").",
                            formatFieldAccessChain(fieldAccessChain), name));
                }
            // flattenable
            } else {
                if (fieldAccessChain.length == 1) {
                    throw new IllegalArgumentException(String.format(
                            "The given \"fieldAccessChain\"=%s can't reach the deepest field (for component \"%s\").",
                            formatFieldAccessChain(fieldAccessChain), name));
                } else if (fieldAccessChain.length == 2) {
                    int ordinalOffset = fieldDef.scalarType.ordinalOffsetOfField(fieldAccessChain[1]);
                    if (ordinalOffset == -1) {
                        StringBuilder errorMsg = new StringBuilder();
                        errorMsg.append(String.format("The given \"fieldAccessChain\"=%s is invalid (for component \"%s\").",
                                formatFieldAccessChain(fieldAccessChain),
                                name));
                        errorMsg.append(String.format(" Failed to query the ordinal offset of \"%s\" in the scalar type \"%s\".",
                                fieldAccessChain[1],
                                fieldDef.scalarType));
                        if (Arrays.stream(fieldDef.scalarType.fieldNames).noneMatch((str -> str.equals(fieldAccessChain[1])))) {
                            errorMsg.append(String.format(" The scalar type \"%s\" doesn't contain field \"%s\".",
                                    fieldDef.scalarType,
                                    fieldAccessChain[1]));
                        }
                        throw new IllegalArgumentException(errorMsg.toString());
                    }
                    return ordinal + ordinalOffset;
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field (for component \"%s\").",
                            formatFieldAccessChain(fieldAccessChain), name));
                }
            }
        // struct field
        } else {
            if (fieldAccessChain.length == 1) {
                throw new IllegalArgumentException(String.format(
                        "The given \"fieldAccessChain\"=%s can't reach the deepest field (for component \"%s\").",
                        formatFieldAccessChain(fieldAccessChain), name));
            }
            String[] newFieldAccessChain = new String[fieldAccessChain.length - 1];
            System.arraycopy(fieldAccessChain, 1, newFieldAccessChain, 0, newFieldAccessChain.length);
            return ordinal + fieldRegistry.structRegistry.getFieldOrdinal(fieldDef.structTypeName, newFieldAccessChain);
        }
    }

    // -----Component Construction-----

    private final AccessHandlePool componentAccessHandlePool = new AccessHandlePool();

    /**
     * @param name Component name is not necessarily valid
     * @param args Arguments must match the flattened component one-by-one
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public CleanComponent newComponent(@NonNull String name, @NonNull Object @NonNull ... args) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(args);
        for (Object arg : args) {
            Preconditions.checkNotNull(arg);
        }

        if (!componentExists(name)) {
            return null;
        }

        ComponentDesc componentDesc = getComponentDesc(name);
        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        Class<? extends CleanComponent> componentClass = getComponentClass(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!componentAccessHandlePool.classRegistered(componentClass)) {
            componentAccessHandlePool.register(componentClass, memberLayout);
        }

        Object output = componentAccessHandlePool.newClass(componentClass);

        int index = 0;
        for (int i = 0; i < componentDesc.fields.size(); i++) {
            String fieldTypeName = componentDesc.fieldTypeNames.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = componentDescFlattened.fields.get(i).getUnitCount();
            Object value = fieldRegistry.newField(fieldTypeName, Arrays.copyOfRange(args, index, index + unitCount));
            index += unitCount;

            componentAccessHandlePool.setFieldValue(componentClass, output, fieldName, value);
        }

        return (CleanComponent) output;
    }

    // -----Component Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object @NonNull [] flattenComponent(@NonNull CleanComponent component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(componentExists(component.getClass()),
                "Component class %s isn't registered.", component.getClass().getName());

        String name = getComponentName(component.getClass());

        ComponentDesc componentDesc = getComponentDesc(name);
        ComponentDescFlattened componentDescFlattened = getComponentDescFlattened(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!componentAccessHandlePool.classRegistered(component.getClass())) {
            componentAccessHandlePool.register(component.getClass(), memberLayout);
        }

        Object[] args = new Object[componentDescFlattened.getUnitCount()];

        int index = 0;
        for (int i = 0; i < componentDesc.fields.size(); i++) {
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = componentDescFlattened.fields.get(i).getUnitCount();
            Object[] _args = fieldRegistry.flattenField(componentAccessHandlePool.getFieldValue(component.getClass(), component, fieldName));
            System.arraycopy(_args, 0, args, index, unitCount);
            index += unitCount;
        }

        return args;
    }
}
