package com.cleanroommc.kirino.ecs.component.schema.def.field;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.utils.PrimitiveTypeUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class FieldRegistry {
    public final StructRegistry structRegistry;

    public FieldRegistry(StructRegistry structRegistry) {
        this.structRegistry = structRegistry;
    }

    private final Map<String, Data> fieldData = new Object2ObjectOpenHashMap<>();
    private final Map<Class<?>, Data> fieldDataByClass = new Reference2ObjectOpenHashMap<>();

    private boolean lockRegistry = false;

    /**
     * This method is the entry point to register field types.
     *
     * @param name     The registry name of the field
     * @param clazz    The corresponding class of the field
     * @param fieldDef The actual field type layout
     */
    public void registerFieldType(
        @NonNull String name,
        @NonNull Class<?> clazz,
        @NonNull FieldDef fieldDef) {

        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!fieldData.containsKey(name),
            "Field \"%s\" is already registered.", name);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(fieldDef);
        Preconditions.checkArgument(!fieldDataByClass.containsKey(clazz),
            "Field class %s is already registered.", clazz.getName());

        Data data = new Data(name, clazz, fieldDef);
        fieldData.put(name, data);
        fieldDataByClass.put(clazz, data);

        KirinoCommonCore.LOGGER.info("Registered field {} of type {} ", name, clazz.getSimpleName());
    }

    /**
     * Register calls are no longer allowed after the lock call.
     */
    public void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");

        for (Data data : fieldData.values()) {
            FieldDef definition = data.definition;
            Class<?> expected = definition.fieldKind == FieldKind.SCALAR ? definition.scalarType.clazz
                : structRegistry.getLayout(definition.structTypeName).javaClass();
            Preconditions.checkArgument(data.clazz == expected,
                "Field type %s has an incompatible Java class.", data.name);
        }
        lockRegistry = true;
    }

    //<editor-fold desc="getters & queries">

    /**
     * Similar to {@link #fieldTypeExists(Class)} but accepts a class string instead to avoid class loading.
     */
    public boolean fieldTypeExists_ClassName(@NonNull String className) {
        Preconditions.checkNotNull(className);

        return getFieldTypeName_ClassName(className) != null;
    }

    public boolean fieldTypeExists(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        return fieldDataByClass.containsKey(clazz);
    }

    public boolean fieldTypeExists(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return fieldData.containsKey(name);
    }

    /**
     * Similar to {@link #getFieldTypeName(Class)} but accepts a class string instead to avoid class loading.
     */
    @Nullable
    public String getFieldTypeName_ClassName(@NonNull String className) {
        Preconditions.checkNotNull(className);

        for (Data data : fieldDataByClass.values()) {
            if (data.clazz.getName().equals(className)) {
                return data.name;
            }
        }
        return null;
    }

    @Nullable
    public String getFieldTypeName(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        Data data = fieldDataByClass.get(clazz);
        return data == null ? null : data.name;
    }

    @Nullable
    public Class<?> getFieldClass(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = fieldData.get(name);
        return data == null ? null : data.clazz;
    }

    @Nullable
    public FieldDef getFieldDef(@NonNull String name) {
        Preconditions.checkNotNull(name);

        Data data = fieldData.get(name);
        return data == null ? null : data.definition;
    }

    @Nullable
    public FieldDef getFieldDef(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        Data data = fieldDataByClass.get(clazz);
        return data == null ? null : data.definition;
    }
    //</editor-fold>


    // -----Field Construction-----

    /**
     * @param name Field name is not necessarily valid
     * @param args Arguments must match the flattened field one-by-one
     */
    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public Object newField(@NonNull String name, @NonNull Object @NonNull ... args) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(args);
        for (Object arg : args) {
            Preconditions.checkNotNull(arg);
        }

        FieldDef fieldDef = getFieldDef(name);
        if (fieldDef == null) {
            return null;
        }

        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            return fieldDef.scalarType.newScalar(args);
        } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
            return structRegistry.newStruct(fieldDef.structTypeName, args);
        }

        return null;
    }

    // -----Field Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object @NonNull [] flattenField(@NonNull Object fieldInstance) {
        Preconditions.checkNotNull(fieldInstance);

        Class<?> fieldClass = fieldInstance.getClass();
        if (PrimitiveTypeUtils.isWrappedPrimitive(fieldClass)) {
            // force primitive types cuz we use primitive types by default
            // see CleanECSRuntime's constructor
            fieldClass = PrimitiveTypeUtils.toPrimitive(fieldInstance.getClass());
        }

        FieldDef fieldDef = getFieldDef(fieldClass);
        if (fieldDef == null) {
            throw new IllegalArgumentException("Field class " + fieldInstance.getClass().getName() + " isn't registered.");
        }

        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            return fieldDef.scalarType.flattenScalar(fieldInstance);
        } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
            return structRegistry.flattenStruct(fieldInstance);
        }

        throw new IllegalStateException("Invalid field kind."); // impossible
    }

    private record Data(String name, Class<?> clazz, FieldDef definition) {
    }
}
