package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.*;

final class LegacyRowsHeapPool {
    private final int initSize;
    private int currentSize;
    private final Map<Class<?>, ColumnBinding> byClass = new Reference2ObjectOpenHashMap<>();
    private final ColumnBinding[] bindings;
    private final HeapPrimitiveArray[] columns;
    private final CleanComponent[] insertionValues;
    private final Int2IntOpenHashMap entityRows = new Int2IntOpenHashMap();
    private final Int2IntOpenHashMap rowEntities = new Int2IntOpenHashMap();
    private final IntArrayList freeRows = new IntArrayList();
    private int rowLimit;
    private int executions;

    public LegacyRowsHeapPool(ComponentRegistry registry, List<Class<? extends CleanComponent>> components,
                    int initSize) {
        this.initSize = initSize;
        this.currentSize = initSize;
        entityRows.defaultReturnValue(-1);
        bindings = new ColumnBinding[components.size()];
        insertionValues = new CleanComponent[components.size()];
        List<HeapPrimitiveArray> allColumns = new ArrayList<>();
        for (int i = 0; i < components.size(); i++) {
            Class<? extends CleanComponent> type = components.get(i);
            ColumnBinding binding = new ColumnBinding(registry.getLayout(type), registry.getCodec(type), initSize, i);
            if (byClass.putIfAbsent(type, binding) != null) {
                throw new IllegalArgumentException("Duplicate component in archetype: " + type.getName());
            }
            bindings[i] = binding;
            allColumns.addAll(Arrays.asList(binding.columns));
        }
        columns = allColumns.toArray(HeapPrimitiveArray[]::new);
    }

    public int getCurrentSize() { return currentSize; }

    public boolean containsEntity(int entityID) {
        return entityRows.containsKey(entityID);
    }

    public CleanComponent getComponent(int entityID, Class<? extends CleanComponent> component) {
        ColumnBinding binding = binding(component);
        return (CleanComponent) binding.codec.read(binding, row(entityID));
    }

    public void setComponent(int entityID, CleanComponent component) {
        Objects.requireNonNull(component);
        ColumnBinding binding = binding(component.getClass());
        binding.codec.write(component, binding, row(entityID));
    }

    public synchronized void addEntity(int entityID, CleanComponent[] components) {
        requireStructuralAccess();
        Objects.requireNonNull(components);
        if (entityRows.containsKey(entityID)) {
            throw new IllegalArgumentException("Entity already exists: " + entityID);
        }

        int componentSize = components.length;
        if (componentSize != bindings.length) {
            throw new IllegalArgumentException("Component count doesn't match archetype");
        }
        try {
            for (int i = 0; i < componentSize; i++) {
                CleanComponent component = components[i];
                Objects.requireNonNull(component);
                ColumnBinding binding = binding(component.getClass());
                int index = binding.componentIndex;
                if (insertionValues[index] != null) {
                    throw new IllegalArgumentException("Duplicate component: " + component.getClass().getName());
                }
                binding.codec.validate(component);
                insertionValues[index] = component;
            }
            boolean reuse = !freeRows.isEmpty();
            int row = reuse ? freeRows.getInt(freeRows.size() - 1) : rowLimit;
            if (row >= currentSize) {
                resize(PoolCapacity.grow(currentSize, Math.incrementExact(row)));
            }
            for (int i = 0; i < bindings.length; i++) {
                ColumnBinding binding = bindings[i];

                binding.codec.write(insertionValues[i], binding, row);
            }
            entityRows.put(entityID, row);
            rowEntities.put(row, entityID);
            if (reuse) {
                freeRows.removeInt(freeRows.size() - 1);
            } else {
                rowLimit++;
            }
        } finally {
            Arrays.fill(insertionValues, null);
        }
    }

    public synchronized void removeEntity(int entityID) {
        requireStructuralAccess();
        int row = row(entityID);
        entityRows.remove(entityID);
        rowEntities.remove(row);
        if (row == rowLimit - 1) {
            rowLimit--;
            while (rowLimit > 0 && freeRows.rem(rowLimit - 1)) {
                rowLimit--;
            }
        } else {
            freeRows.add(row);
        }
    }

    synchronized void trimCapacity() {
        requireStructuralAccess();
        int target = PoolCapacity.shrink(currentSize, rowLimit, initSize);
        if (target != currentSize) {
            resize(target);
        }
    }

    private void resize(int size) {
        Object[] replacements = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            replacements[i] = columns[i].resizedArray(size);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i].replaceArray(replacements[i], size);
        }
        currentSize = size;
    }

    public synchronized void beginExecution() {
        executions++;
    }

    public synchronized void endExecution() {
        if (executions == 0) {
            throw new IllegalStateException("No active pool execution");
        }
        executions--;
    }

    private void requireStructuralAccess() {
        if (executions != 0) {
            throw new IllegalStateException("Structural changes aren't allowed while jobs use this pool");
        }
    }

    private int row(int entityID) {
        int row = entityRows.get(entityID);
        if (row < 0) {
            throw new IllegalArgumentException("Unknown entity: " + entityID);
        }
        return row;
    }

    private ColumnBinding binding(Class<?> component) {
        ColumnBinding binding = byClass.get(Objects.requireNonNull(component));
        if (binding == null) {
            throw new IllegalArgumentException("Component isn't stored in this archetype: " + component.getName());
        }
        return binding;
    }

    public PrimitiveArray getArray(Class<? extends CleanComponent> component, String... path) {
        ColumnBinding binding = binding(component);
        return binding.column(binding.layout.resolve(path));
    }

    public PrimitiveArray getArray(Class<? extends CleanComponent> component, SchemaLayout.Column column) {
        return binding(component).column(column);
    }

    public ArrayRange getArrayRange() {
        return new ArrayRange(0, rowLimit, new IntOpenHashSet(freeRows));
    }

    public int getEntityID(int index) {
        if (!rowEntities.containsKey(index)) {
            throw new IllegalStateException("No entity at index=" + index);
        }
        return rowEntities.get(index);
    }

    public String getSnapshot() {
        StringBuilder output = new StringBuilder("\n=====HeapPool Snapshot=====\n");
        for (ColumnBinding binding : bindings) {
            output.append("Component: ").append(binding.layout.name()).append('\n');
            for (SchemaLayout.Column column : binding.layout.columns().elements()) {
                output.append(String.join(".", column.path())).append(": [");
                PrimitiveArray array = binding.columns[column.ordinal()];
                for (int row = 0; row < Math.min(currentSize, 10); row++) {
                    if (row != 0) {
                        output.append(", ");
                    }
                    switch (column.type()) {
                        case BYTE -> output.append(array.getByte(row));
                        case SHORT -> output.append(array.getShort(row));
                        case INT -> output.append(array.getInt(row));
                        case LONG -> output.append(array.getLong(row));
                        case FLOAT -> output.append(array.getFloat(row));
                        case DOUBLE -> output.append(array.getDouble(row));
                        case BOOL -> output.append(array.getBool(row));
                    }
                }
                output.append("]\n");
            }
        }
        return output.toString();
    }
}
