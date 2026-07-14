package com.cleanroommc.kirino.ecs.component.schema.def.field;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarConstructor;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarDeconstructor;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.utils.PrimitiveTypeUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FieldRegistry {
    public final StructRegistry structRegistry;

    public FieldRegistry(StructRegistry structRegistry) {
        this.structRegistry = structRegistry;
    }

    private final BiMap<String, Class<?>> fieldTypeNameClassMapping = HashBiMap.create();
    private final Map<String, FieldDef> fieldDefMap = new HashMap<>();

    private boolean lockRegistry = false;

    /**
     * This method is the entry point to register field types.
     *
     * @param name The registry name of the field
     * @param clazz The corresponding class of the field
     * @param fieldDef The actual field type layout
     */
    public void registerFieldType(
            @NonNull String name,
            @NonNull Class<?> clazz,
            @NonNull FieldDef fieldDef) {

        Preconditions.checkState(!lockRegistry, "The registry is already locked!");
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!fieldTypeNameClassMapping.containsKey(name),
                "Field \"%s\" is already registered.", name);
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(fieldDef);

        fieldTypeNameClassMapping.put(name, clazz);
        fieldDefMap.put(name, fieldDef);
    }

    /**
     * Register calls are no longer allowed after the lock call.
     */
    public void lock() {
        Preconditions.checkState(!lockRegistry, "The registry is already locked!");

        lockRegistry = true;
    }

    //<editor-fold desc="getters & queries">
    /**
     * Similar to {@link #fieldTypeExists(Class)} but accepts a class string instead to avoid class loading.
     */
    public boolean fieldTypeExists_ClassName(@NonNull String className) {
        Preconditions.checkNotNull(className);

        // BiMap itself has an inverse cache
        return fieldTypeNameClassMapping
                .inverse()
                .keySet()
                .stream()
                .anyMatch(c -> c.getName().equals(className));
    }

    public boolean fieldTypeExists(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        // BiMap itself has an inverse cache
        return fieldTypeNameClassMapping.inverse().containsKey(clazz);
    }

    public boolean fieldTypeExists(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return fieldTypeNameClassMapping.containsKey(name);
    }

    /**
     * Similar to {@link #getFieldTypeName(Class)} but accepts a class string instead to avoid class loading.
     */
    @Nullable
    public String getFieldTypeName_ClassName(@NonNull String className) {
        Preconditions.checkNotNull(className);

        // BiMap itself has an inverse cache
        return fieldTypeNameClassMapping
                .inverse()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().getName().equals(className))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public String getFieldTypeName(@NonNull Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        // BiMap itself has an inverse cache
        return fieldTypeNameClassMapping.inverse().get(clazz);
    }

    @Nullable
    public Class<?> getFieldClass(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return fieldTypeNameClassMapping.get(name);
    }

    @Nullable
    public FieldDef getFieldDef(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return fieldDefMap.get(name);
    }
    //</editor-fold>

    @NonNull
    public FlattenedField flatten(@NonNull FieldDef fieldDef) {
        Preconditions.checkNotNull(fieldDef);

        return new FlattenedField(fieldDef, structRegistry);
    }

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

        if (!fieldTypeExists(name)) {
            return null;
        }

        FieldDef fieldDef = getFieldDef(name);
        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            return ScalarConstructor.newScalar(fieldDef.scalarType, args);
        } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
            return structRegistry.newStruct(fieldDef.structTypeName, args);
        }

        return null;
    }

    // -----Field Deconstruction-----

    @SuppressWarnings("DataFlowIssue")
    public @NonNull Object @NonNull[] flattenField(@NonNull Object fieldInstance) {
        Preconditions.checkNotNull(fieldInstance);

        Class<?> fieldClass = fieldInstance.getClass();
        if (PrimitiveTypeUtils.isWrappedPrimitive(fieldClass)) {
            // force primitive types cuz we use primitive types by default
            // see CleanECSRuntime's constructor
            fieldClass = PrimitiveTypeUtils.toPrimitive(fieldInstance.getClass());
        }

        Preconditions.checkArgument(fieldTypeExists(fieldClass),
                "Field class %s isn't registered.", fieldInstance.getClass().getName());

        String name = getFieldTypeName(fieldClass);
        FieldDef fieldDef = getFieldDef(name);
        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            return ScalarDeconstructor.flattenScalar(fieldDef.scalarType, fieldInstance);
        } else if (fieldDef.fieldKind == FieldKind.STRUCT) {
            return structRegistry.flattenStruct(fieldInstance);
        }

        throw new IllegalStateException("Invalid field kind."); // impossible
    }
}
