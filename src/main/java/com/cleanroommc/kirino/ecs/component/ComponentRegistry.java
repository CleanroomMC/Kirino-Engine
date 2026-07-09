package com.cleanroommc.kirino.ecs.component;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
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

    /**
     * This method is the entry point to register components.
     * <code>memberLayout.fieldNames</code> must match <code>fieldTypeNames</code> one by one.
     *
     * @param name The registry name of the component
     * @param clazz The corresponding class of the component
     * @param memberLayout The metadata of the component class
     * @param fieldTypeNames The field registry names of the component
     */
    public void registerComponent(String name, Class<? extends CleanComponent> clazz, MemberLayout memberLayout, String... fieldTypeNames) {
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

    public ImmutableMap<String, ComponentDesc> getComponentDescMap() {
        return ImmutableMap.copyOf(componentDescMap);
    }

    public ImmutableMap<String, ComponentDescFlattened> getComponentDescFlattenedMap() {
        return ImmutableMap.copyOf(componentDescFlattenedMap);
    }

    public boolean componentExists(Class<? extends CleanComponent> clazz) {
        return comNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean componentExists(String name) {
        return comNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(String name) {
        return classMemberLayoutMap.get(name);
    }

    @Nullable
    public String getComponentName(Class<? extends CleanComponent> clazz) {
        return comNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<? extends CleanComponent> getComponentClass(String name) {
        return comNameClassMapping.get(name);
    }

    @Nullable
    public ComponentDesc getComponentDesc(String name) {
        return componentDescMap.get(name);
    }

    @Nullable
    public ComponentDescFlattened getComponentDescFlattened(String name) {
        return componentDescFlattenedMap.get(name);
    }

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

    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(String name, String... fieldAccessChain) {
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
                "Can't find a field that matches the \"fieldAccessChain\"=%s.",
                formatFieldAccessChain(fieldAccessChain));

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
            if (fieldDef.scalarType == ScalarType.BYTE ||
                    fieldDef.scalarType == ScalarType.SHORT ||
                    fieldDef.scalarType == ScalarType.INT ||
                    fieldDef.scalarType == ScalarType.LONG ||
                    fieldDef.scalarType == ScalarType.FLOAT ||
                    fieldDef.scalarType == ScalarType.DOUBLE ||
                    fieldDef.scalarType == ScalarType.BOOL) {
                if (fieldAccessChain.length == 1) {
                    return ordinal;
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field.",
                            formatFieldAccessChain(fieldAccessChain)));
                }
            // flattenable
            } else {
                if (fieldAccessChain.length == 1) {
                    throw new IllegalArgumentException(String.format(
                            "The given \"fieldAccessChain\"=%s can't reach the deepest field.",
                            formatFieldAccessChain(fieldAccessChain)));
                } else if (fieldAccessChain.length == 2) {
                    int ordinalOffset = fieldDef.scalarType.ordinalOffsetOfField(fieldAccessChain[1]);
                    if (ordinalOffset == -1) {
                        StringBuilder errorMsg = new StringBuilder();
                        errorMsg.append(String.format("The given \"fieldAccessChain\"=%s is invalid.", formatFieldAccessChain(fieldAccessChain)));
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
                            "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field.",
                            formatFieldAccessChain(fieldAccessChain)));
                }
            }
        // struct field
        } else {
            if (fieldAccessChain.length == 1) {
                throw new IllegalArgumentException(String.format(
                        "The given \"fieldAccessChain\"=%s can't reach the deepest field.",
                        formatFieldAccessChain(fieldAccessChain)));
            }
            String[] newFieldAccessChain = new String[fieldAccessChain.length - 1];
            System.arraycopy(fieldAccessChain, 1, newFieldAccessChain, 0, newFieldAccessChain.length);
            return ordinal + fieldRegistry.structRegistry.getFieldOrdinal(fieldDef.structTypeName, newFieldAccessChain);
        }
    }

    // -----Component Construction-----

    private final AccessHandlePool componentAccessHandlePool = new AccessHandlePool();

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public CleanComponent newComponent(String name, Object... args) {
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
    public @NonNull Object[] flattenComponent(@NonNull CleanComponent component) {
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
