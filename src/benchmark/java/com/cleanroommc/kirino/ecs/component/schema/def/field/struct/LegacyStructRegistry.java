package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.component.schema.reflect.LegacyAccessHandlePool;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class LegacyStructRegistry {

    private final Map<Class<?>, Data> structDataByClass = new Reference2ObjectOpenHashMap<>();

    private final Map<String, Data> structData = new Object2ObjectOpenHashMap<>();

    private boolean lockRegistry = false;

    
    public void registerStructType(
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

    
    public void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");

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

    @SuppressWarnings("DataFlowIssue")
    public int flattenedUnitCount(@NonNull String name) {
        Preconditions.checkNotNull(name);
        final Data registryData = structData.get(name);
        Preconditions.checkArgument(registryData != null,
            "Struct type %s doesn't exist.", name);

        return flattenedUnitCount(registryData);
    }

    private int flattenedUnitCount(Data registryData) {
        int fetched = registryData.unit;
        if (fetched != -1) {
            return fetched;
        }

        int unitCount = 0;
        for (FieldDef fieldDef : registryData.structDef.fields) {
            switch (fieldDef.fieldKind) {
                case STRUCT -> {
                    unitCount += flattenedUnitCount(fieldDef.structTypeName);
                }
                case SCALAR -> {
                    unitCount += FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                }
            }
        }

        registryData.unit = unitCount;
        return unitCount;
    }

    private static String formatFieldAccessChain(String... fieldAccessChain) {
        return fieldAccessChain.length == 0 ? "\"\"" : "\"." + String.join(".", fieldAccessChain) + "\"";
    }

    
    @SuppressWarnings("DataFlowIssue")
    public int getFieldOrdinal(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(fieldAccessChain);
        for (String field : fieldAccessChain) {
            Preconditions.checkNotNull(field);
        }
        Data registryData = structData.get(name);
        Preconditions.checkArgument(registryData != null,
            "Struct type %s doesn't exist.", name);
        if (fieldAccessChain.length == 0) {
            throw new IllegalArgumentException(String.format(
                "The given \"fieldAccessChain\"=%s must not be empty.", formatFieldAccessChain(fieldAccessChain)));
        }

        String accessChain = fieldAccessChain[0];

        MemberLayout memberLayout = registryData.memberLayout;
        int index = 0;
        boolean match = false;
        while (!match && index < memberLayout.fieldNames.size()) {
            if (memberLayout.fieldNames.get(index).equals(accessChain)) {
                match = true;
            } else {
                index++;
            }
        }

        if (!match) {
            throw new IllegalArgumentException(String.format(
                "Can't find a field (from component \"%s\") that matches the \"fieldAccessChain\"=%s.", name, formatFieldAccessChain(fieldAccessChain)));
        }

        StructDef structDef = registryData.structDef;
        List<FieldDef> fields = structDef.fields;

        int ordinal = 0;
        for (int i = 0; i < index; i++) {
            FieldDef fieldDef = fields.get(i);

            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                ordinal += FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
            } else {
                ordinal += flattenedUnitCount(fieldDef.structTypeName);
            }
        }

        FieldDef fieldDef = structDef.fields.get(index);
        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            if (fieldDef.scalarType.isSingular()) {

                if(fieldAccessChain.length != 1) {
                    throw new IllegalArgumentException(String.format(
                        "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field (for component \"%s\").",
                        formatFieldAccessChain(fieldAccessChain), name));
                }

                return ordinal;
            } else {
                if(fieldAccessChain.length == 1) {
                    throw new IllegalArgumentException(String.format(
                        "The given \"fieldAccessChain\"=%s can't reach the deepest field (for component \"%s\").",
                        formatFieldAccessChain(fieldAccessChain), name));
                }

                if(fieldAccessChain.length != 2) {
                    throw new IllegalArgumentException(String.format(
                        "The given \"fieldAccessChain\"=%s provides redundant terms after the deepest field (for component \"%s\").",
                        formatFieldAccessChain(fieldAccessChain),
                        name));
                }

                String accessChain2 = fieldAccessChain[1];
                int ordinalOffset = fieldDef.scalarType.ordinalOffsetOfField(accessChain2);
                if(ordinalOffset != -1) {
                    return ordinal + ordinalOffset;
                }

                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append(String.format("The given \"fieldAccessChain\"=%s is invalid (for component \"%s\").",
                    formatFieldAccessChain(fieldAccessChain),
                    name));
                errorMsg.append(String.format(" Failed to query the ordinal offset of \"%s\" in the scalar type \"%s\".",
                    fieldAccessChain[1],
                    fieldDef.scalarType));
                if (Arrays.stream(fieldDef.scalarType.fieldNames).noneMatch((str -> str.equals(accessChain2)))) {
                    errorMsg.append(String.format(" The scalar type \"%s\" doesn't contain field \"%s\".",
                        fieldDef.scalarType,
                        fieldAccessChain[1]));
                }
                throw new IllegalArgumentException(errorMsg.toString());
            }
        } else {
            if (fieldAccessChain.length == 1) {
                throw new IllegalArgumentException(String.format(
                    "The given \"fieldAccessChain\"=%s can't reach the deepest field (for component \"%s\").",
                    formatFieldAccessChain(fieldAccessChain),
                    name));
            }

            String[] newFieldAccessChain = new String[fieldAccessChain.length - 1];
            System.arraycopy(fieldAccessChain, 1, newFieldAccessChain, 0, newFieldAccessChain.length);
            return ordinal + getFieldOrdinal(structDef.fields.get(index).structTypeName, newFieldAccessChain);
        }
    }

    private final LegacyAccessHandlePool structLegacyAccessHandlePool = new LegacyAccessHandlePool();

    
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public Object newStruct(@NonNull String name, @NonNull Object @NonNull ... args) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(args);
        for (Object arg : args) {
            Preconditions.checkNotNull(arg);
        }

        Data registryData = structData.get(name);
        if (registryData == null) {
            return null;
        }

        StructDef structDef = registryData.structDef;
        Class<?> structClass = registryData.structClass;
        MemberLayout memberLayout = registryData.memberLayout;

        if (!structLegacyAccessHandlePool.classRegistered(structClass)) {
            structLegacyAccessHandlePool.register(structClass, memberLayout);
        }

        Object output = structLegacyAccessHandlePool.newClass(structClass);

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            Object value = null;
            int unitCount = 0;

            switch (fieldDef.fieldKind) {
                case SCALAR -> {
                    unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                    value = fieldDef.scalarType.newScalar(Arrays.copyOfRange(args, index, index + unitCount));
                }
                case STRUCT -> {
                    unitCount = flattenedUnitCount(fieldDef.structTypeName);
                    value = newStruct(fieldDef.structTypeName, Arrays.copyOfRange(args, index, index + unitCount));
                }
            }

            index += unitCount;

            structLegacyAccessHandlePool.setFieldValue(structClass, output, fieldName, value);
        }

        return output;
    }

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object @NonNull [] flattenStruct(@NonNull Object structInstance) {
        Preconditions.checkNotNull(structInstance);
        Class<?> structClass = structInstance.getClass();
        Data registryData = structDataByClass.get(structClass);
        if (registryData == null) {
            throw new IllegalArgumentException("Struct class " + structInstance.getClass().getName() + " isn't registered.");
        }

        StructDef structDef = registryData.structDef;
        MemberLayout memberLayout = registryData.memberLayout;

        if (!structLegacyAccessHandlePool.classRegistered(structClass)) {
            structLegacyAccessHandlePool.register(structClass, memberLayout);
        }

        Object[] args = new Object[flattenedUnitCount(registryData)];

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = 0;
            Object[] _args = null;
            switch (fieldDef.fieldKind) {
                case SCALAR -> {
                    unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                    _args = fieldDef.scalarType.flattenScalar(structLegacyAccessHandlePool.getFieldValue(structClass, structInstance, fieldName));
                }
                case STRUCT -> {
                    unitCount = flattenedUnitCount(fieldDef.structTypeName);
                    _args = flattenStruct(structLegacyAccessHandlePool.getFieldValue(structClass, structInstance, fieldName));
                }
            }

            System.arraycopy(_args, 0, args, index, unitCount);
            index += unitCount;
        }

        return args;
    }

    public static class Data {
        private final String name;
        private final Class<?> structClass;
        private final StructDef structDef;
        private final MemberLayout memberLayout;
        private int unit;

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
