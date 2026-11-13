package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.jspecify.annotations.Nullable;

import java.util.*;

final class HeapPool extends ArchetypeDataPool {
    private final Map<Class<?>, ColumnBinding> byClass = new Reference2ObjectOpenHashMap<>();
    private final ColumnBinding[] bindings;
    private final HeapPrimitiveArray[] columns;
    private final CleanComponent[] insertionValues;
    private final Int2IntOpenHashMap entityRows = new Int2IntOpenHashMap();
    private final Int2IntOpenHashMap rowEntities = new Int2IntOpenHashMap();
    private final PoolRows rows;
    private final Map<HeapPool, TransferPlan> transfers = new Reference2ObjectOpenHashMap<>();
    private int executions;

    public HeapPool(ComponentRegistry registry, List<Class<? extends CleanComponent>> components,
                    int initSize) {
        super(registry, components, initSize);
        entityRows.defaultReturnValue(-1);
        rows = new PoolRows(initSize);
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

    @Override
    public boolean containsEntity(int entityID) {
        return entityRows.containsKey(entityID);
    }

    @Override
    int getEntityRow(int entityID) {
        return row(entityID);
    }

    @Override
    public CleanComponent getComponent(int entityID, Class<? extends CleanComponent> component) {
        ColumnBinding binding = binding(component);
        return (CleanComponent) binding.codec.read(binding, row(entityID));
    }

    @Override
    public void setComponent(int entityID, CleanComponent component) {
        Objects.requireNonNull(component);
        ColumnBinding binding = binding(component.getClass());
        binding.codec.write(component, binding, row(entityID));
    }

    @Override
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
            int row = nextInsertionRow();
            for (int i = 0; i < bindings.length; i++) {
                ColumnBinding binding = bindings[i];

                binding.codec.write(insertionValues[i], binding, row);
            }
            publishEntity(entityID, row);
        } finally {
            Arrays.fill(insertionValues, null);
        }
    }

    @Override
    synchronized void addEntityFrom(int entityID, ArchetypeDataPool source, @Nullable CleanComponent addition) {
        requireStructuralAccess();
        if (!(source instanceof HeapPool sourcePool) || source.componentRegistry != componentRegistry) {
            throw new IllegalArgumentException("Transfer requires heap pools from the same component registry");
        }
        sourcePool.requireStructuralAccess();
        if (entityRows.containsKey(entityID)) {
            throw new IllegalArgumentException("Entity already exists: " + entityID);
        }
        int sourceRow = sourcePool.row(entityID);
        TransferPlan plan = transfers.get(sourcePool);
        if (plan == null) {
            plan = new TransferPlan(sourcePool, this);
            transfers.put(sourcePool, plan);
        }
        if (plan.addition == null) {
            if (addition != null) {
                throw new IllegalArgumentException("REMOVE transfer doesn't accept an added component");
            }
        } else {
            if (addition == null || addition.getClass() != plan.addition.layout.javaClass()) {
                throw new IllegalArgumentException("Added component doesn't match target archetype");
            }
            plan.addition.codec.validate(addition);
        }
        int targetRow = nextInsertionRow();
        if (plan.addition != null) {
            plan.addition.codec.write(addition, plan.addition, targetRow);
        }
        for (int column = 0; column < plan.sources.length; column++) {
            plan.sources[column].copyTo(sourceRow, plan.targets[column], targetRow);
        }
        publishEntity(entityID, targetRow);
    }

    private int nextInsertionRow() {
        int row = rows.nextRow();
        if (row >= currentSize) {
            resize(PoolCapacity.grow(currentSize, Math.incrementExact(row)));
        }
        return row;
    }

    private void publishEntity(int entityID, int row) {
        entityRows.put(entityID, row);
        rowEntities.put(row, entityID);
        rows.occupy(row);
    }

    private static final class TransferPlan {
        final HeapPrimitiveArray[] sources;
        final HeapPrimitiveArray[] targets;
        final ColumnBinding addition;

        TransferPlan(HeapPool source, HeapPool target) {
            ColumnBinding missing = null;
            int count = 0;
            for (ColumnBinding destination : target.bindings) {
                ColumnBinding origin = source.byClass.get(destination.layout.javaClass());
                if (origin == null) {
                    if (missing != null) {
                        throw new IllegalArgumentException("Transfer adds more than one component");
                    }
                    missing = destination;
                } else {
                    if (origin.layout != destination.layout) {
                        throw new IllegalArgumentException("Shared component layouts don't match");
                    }
                    count = Math.addExact(count, origin.columns.length);
                }
            }
            if (missing == null ? source.bindings.length != target.bindings.length + 1
                : source.bindings.length != target.bindings.length - 1) {
                throw new IllegalArgumentException("Transfer must add or remove exactly one component");
            }
            addition = missing;
            sources = new HeapPrimitiveArray[count];
            targets = new HeapPrimitiveArray[count];
            int offset = 0;
            for (ColumnBinding destination : target.bindings) {
                ColumnBinding origin = source.byClass.get(destination.layout.javaClass());
                if (origin != null) {
                    for (int column = 0; column < origin.columns.length; column++) {
                        if (origin.columns[column].type() != destination.columns[column].type()) {
                            throw new IllegalArgumentException("Shared column types don't match");
                        }
                        sources[offset] = origin.columns[column];
                        targets[offset] = destination.columns[column];
                        offset++;
                    }
                }
            }
        }
    }

    @Override
    public synchronized void removeEntity(int entityID) {
        requireStructuralAccess();
        int row = row(entityID);
        entityRows.remove(entityID);
        rowEntities.remove(row);
        rows.release(row);
    }

    @Override
    synchronized void trimCapacity() {
        requireStructuralAccess();
        int target = PoolCapacity.shrink(currentSize, rows.limit, initSize);
        if (target != currentSize) {
            resize(target);
        }
    }

    @Override
    synchronized void reserveCapacity(int capacity) {
        requireStructuralAccess();
        PoolCapacity.validateRequested(capacity);
        if (capacity > currentSize) {
            resize(capacity);
        }
    }

    private void resize(int size) {
        int[] nextFree = Arrays.copyOf(rows.nextFree, size);
        int[] previousFree = Arrays.copyOf(rows.previousFree, size);
        Object[] replacements = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            replacements[i] = columns[i].resizedArray(size);
        }
        for (int i = 0; i < columns.length; i++) {
            columns[i].replaceArray(replacements[i], size);
        }
        rows.nextFree = nextFree;
        rows.previousFree = previousFree;
        currentSize = size;
    }

    @Override
    public synchronized void beginExecution() {
        executions++;
    }

    @Override
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

    @Override
    public PrimitiveArray getArray(Class<? extends CleanComponent> component, String... path) {
        ColumnBinding binding = binding(component);
        return binding.column(binding.layout.resolve(path));
    }

    @Override
    public PrimitiveArray getArray(Class<? extends CleanComponent> component, SchemaLayout.Column column) {
        return binding(component).column(column);
    }

    @Override
    public PoolRows getRows() {
        return rows;
    }

    @Override
    public int getEntityID(int index) {
        if (!rowEntities.containsKey(index)) {
            throw new IllegalStateException("No entity at index=" + index);
        }
        return rowEntities.get(index);
    }

    @Override
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
