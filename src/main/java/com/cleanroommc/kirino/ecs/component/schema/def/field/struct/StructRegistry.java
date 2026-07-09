package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldKind;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarConstructor;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarDeconstructor;
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

public class StructRegistry {
    private final BiMap<String, Class<?>> structTypeNameClassMapping = HashBiMap.create();
    private final Map<String, StructDef> structDefMap = new HashMap<>();
    private final Map<String, Integer> structUnitCountMap = new HashMap<>();
    private final Map<String, MemberLayout> classMemberLayoutMap = new HashMap<>();

    /**
     * This method is the entry point to register struct types.
     * <code>memberLayout.fieldNames</code> must match <code>structDef.fields</code> one by one.
     *
     * @param name The registry name of the struct
     * @param clazz The corresponding class of the struct
     * @param memberLayout The metadata of the struct class
     * @param structDef The actual struct type layout
     */
    public void registerStructType(String name, Class<?> clazz, MemberLayout memberLayout, StructDef structDef) {
        structTypeNameClassMapping.put(name, clazz);
        structDefMap.put(name, structDef);
        classMemberLayoutMap.put(name, memberLayout);
    }

    public ImmutableMap<String, StructDef> getStructDefMap() {
        return ImmutableMap.copyOf(structDefMap);
    }

    public boolean structTypeExists(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean structTypeExists(String name) {
        return structTypeNameClassMapping.containsKey(name);
    }

    @Nullable
    public MemberLayout getClassMemberLayout(String name) {
        return classMemberLayoutMap.get(name);
    }

    @Nullable
    public String getStructTypeName(Class<?> clazz) {
        return structTypeNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<?> getStructClass(String name) {
        return structTypeNameClassMapping.get(name);
    }

    @Nullable
    public StructDef getStructDef(String name) {
        return structDefMap.get(name);
    }

    @SuppressWarnings("DataFlowIssue")
    public int flattenedUnitCount(String name) {
        Preconditions.checkArgument(structTypeExists(name),
                "Struct type %s doesn't exist.", name);

        Integer fetched = structUnitCountMap.get(name);
        if (fetched != null) {
            return fetched;
        }

        int unitCount = 0;
        for (FieldDef fieldDef : getStructDef(name).fields) {
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount += FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount += flattenedUnitCount(fieldDef.structTypeName);
            }
        }

        structUnitCountMap.put(name, unitCount);
        return unitCount;
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
        Preconditions.checkArgument(structTypeExists(name),
                "Struct type %s doesn't exist.", name);
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

        StructDef structDef = getStructDef(name);

        int ordinal = 0;
        for (int i = 0; i < index; i++) {
            if (structDef.fields.get(i).fieldKind == FieldKind.SCALAR) {
                ordinal += FlattenedScalarType.flattenedUnitCount(structDef.fields.get(i).scalarType);
            } else {
                ordinal += flattenedUnitCount(structDef.fields.get(i).structTypeName);
            }
        }

        FieldDef fieldDef = structDef.fields.get(index);
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
            return ordinal + getFieldOrdinal(structDef.fields.get(index).structTypeName, newFieldAccessChain);
        }
    }

    // -----Struct Construction-----

    private final AccessHandlePool structAccessHandlePool = new AccessHandlePool();

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public Object newStruct(String name, Object... args) {
        if (!structTypeExists(name)) {
            return null;
        }

        StructDef structDef = getStructDef(name);
        Class<?> structClass = getStructClass(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!structAccessHandlePool.classRegistered(structClass)) {
            structAccessHandlePool.register(structClass, memberLayout);
        }

        Object output = structAccessHandlePool.newClass(structClass);

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            Object value = null;
            int unitCount = 0;
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                value = ScalarConstructor.newScalar(fieldDef.scalarType, Arrays.copyOfRange(args, index, index + unitCount));
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount = flattenedUnitCount(fieldDef.structTypeName);
                value = newStruct(fieldDef.structTypeName, Arrays.copyOfRange(args, index, index + unitCount));
            }
            index += unitCount;

            structAccessHandlePool.setFieldValue(structClass, output, fieldName, value);
        }

        return output;
    }

    // -----Struct Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object[] flattenStruct(@NonNull Object structInstance) {
        Preconditions.checkNotNull(structInstance);
        Preconditions.checkArgument(structTypeExists(structInstance.getClass()),
                "Struct class %s isn't registered.", structInstance.getClass().getName());

        String name = getStructTypeName(structInstance.getClass());

        StructDef structDef = getStructDef(name);
        MemberLayout memberLayout = getClassMemberLayout(name);

        if (!structAccessHandlePool.classRegistered(structInstance.getClass())) {
            structAccessHandlePool.register(structInstance.getClass(), memberLayout);
        }

        Object[] args = new Object[flattenedUnitCount(name)];

        int index = 0;
        for (int i = 0; i < structDef.fields.size(); i++) {
            FieldDef fieldDef = structDef.fields.get(i);
            String fieldName = memberLayout.fieldNames.get(i);

            int unitCount = 0;
            Object[] _args = null;
            if (fieldDef.fieldKind == FieldKind.SCALAR) {
                unitCount = FlattenedScalarType.flattenedUnitCount(fieldDef.scalarType);
                _args = ScalarDeconstructor.flattenScalar(fieldDef.scalarType, structAccessHandlePool.getFieldValue(structInstance.getClass(), structInstance, fieldName));
            } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
                unitCount = flattenedUnitCount(fieldDef.structTypeName);
                _args = flattenStruct(structAccessHandlePool.getFieldValue(structInstance.getClass(), structInstance, fieldName));
            }
            System.arraycopy(_args, 0, args, index, unitCount);
            index += unitCount;
        }

        return args;
    }
}
