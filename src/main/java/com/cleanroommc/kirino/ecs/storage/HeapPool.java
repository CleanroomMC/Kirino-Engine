package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.ComponentDescFlattened;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FlattenedField;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * It guarantees SoA memory layout.
 */
public final class HeapPool extends ArchetypeDataPool{
    private final List<byte[]> bytePool = new ArrayList<>();
    private final List<short[]> shortPool = new ArrayList<>();
    private final List<int[]> intPool = new ArrayList<>();
    private final List<long[]> longPool = new ArrayList<>();
    private final List<float[]> floatPool = new ArrayList<>();
    private final List<double[]> doublePool = new ArrayList<>();
    private final List<boolean[]> booleanPool = new ArrayList<>();

    public static class ComDataLocation {
        public final int byteArrFrom;
        public final int byteArrTo;
        public final int shortArrFrom;
        public final int shortArrTo;
        public final int intArrFrom;
        public final int intArrTo;
        public final int longArrFrom;
        public final int longArrTo;
        public final int floatArrFrom;
        public final int floatArrTo;
        public final int doubleArrFrom;
        public final int doubleArrTo;
        public final int booleanArrFrom;
        public final int booleanArrTo;
        public final ImmutableList<FlattenedScalarType> order;

        private ComDataLocation(ImmutableList<FlattenedScalarType> order,
                                int byteArrFrom, int byteArrTo,
                                int shortArrFrom, int shortArrTo,
                                int intArrFrom, int intArrTo,
                                int longArrFrom, int longArrTo,
                                int floatArrFrom, int floatArrTo,
                                int doubleArrFrom, int doubleArrTo,
                                int booleanArrFrom, int booleanArrTo) {
            this.byteArrFrom = byteArrFrom;
            this.byteArrTo = byteArrTo;
            this.shortArrFrom = shortArrFrom;
            this.shortArrTo = shortArrTo;
            this.intArrFrom = intArrFrom;
            this.intArrTo = intArrTo;
            this.longArrFrom = longArrFrom;
            this.longArrTo = longArrTo;
            this.floatArrFrom = floatArrFrom;
            this.floatArrTo = floatArrTo;
            this.doubleArrFrom = doubleArrFrom;
            this.doubleArrTo = doubleArrTo;
            this.booleanArrFrom = booleanArrFrom;
            this.booleanArrTo = booleanArrTo;
            this.order = order;
        }
    }

    private final Map<Class<? extends CleanComponent>, ComDataLocation> componentDataLocations = new HashMap<>();

    // key: entity id
    // value: array index
    private final BiMap<Integer, Integer> entityDataIndexes = HashBiMap.create();

    private final List<Integer> freeIndexes = new ArrayList<>();
    private int indexCounter = 0;

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     * </ul>
     *
     * @param componentRegistry The component registry
     * @param components The component types for this archetype
     */
    @SuppressWarnings("DataFlowIssue")
    public HeapPool(ComponentRegistry componentRegistry, List<Class<? extends CleanComponent>> components, int initSize, int growStep, int shrinkStep) {
        super(componentRegistry, components, initSize, growStep, shrinkStep);

        int byteArrCount = 0;
        int shortArrCount = 0;
        int intArrCount = 0;
        int longArrCount = 0;
        int floatArrCount = 0;
        int doubleArrCount = 0;
        int booleanArrCount = 0;
        for (Class<? extends CleanComponent> clazz : components) {
            ComponentDescFlattened descFlattened = componentRegistry.getComponentDescFlattened(componentRegistry.getComponentName(clazz));

            int byteArrFrom = byteArrCount;
            int shortArrFrom = shortArrCount;
            int intArrFrom = intArrCount;
            int longArrFrom = longArrCount;
            int floatArrFrom = floatArrCount;
            int doubleArrFrom =  doubleArrCount;
            int booleanArrFrom = booleanArrCount;
            List<FlattenedScalarType> order = new ArrayList<>();

            for (FlattenedField flattenedField : descFlattened.fields) {
                for (FlattenedScalarType flattenedScalarType : flattenedField.scalarTypes) {
                    if (flattenedScalarType == FlattenedScalarType.BYTE) {
                        bytePool.add(new byte[initSize]);
                        byteArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                        shortPool.add(new short[initSize]);
                        shortArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.INT) {
                        intPool.add(new int[initSize]);
                        intArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                        longPool.add(new long[initSize]);
                        longArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                        floatPool.add(new float[initSize]);
                        floatArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                        doublePool.add(new double[initSize]);
                        doubleArrCount++;
                    } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                        booleanPool.add(new boolean[initSize]);
                        booleanArrCount++;
                    }
                    order.add(flattenedScalarType);
                }
            }

            int byteArrTo = byteArrCount;
            int shortArrTo = shortArrCount;
            int intArrTo = intArrCount;
            int longArrTo = longArrCount;
            int floatArrTo = floatArrCount;
            int doubleArrTo = doubleArrCount;
            int booleanArrTo = booleanArrCount;

            componentDataLocations.put(clazz, new ComDataLocation(ImmutableList.copyOf(order),
                    byteArrFrom, byteArrTo,
                    shortArrFrom, shortArrTo,
                    intArrFrom, intArrTo,
                    longArrFrom, longArrTo,
                    floatArrFrom, floatArrTo,
                    doubleArrFrom, doubleArrTo,
                    booleanArrFrom, booleanArrTo));
        }
    }

    @Override
    public boolean containsEntity(int entityID) {
        return entityDataIndexes.containsKey(entityID);
    }

    @NonNull
    @Override
    @SuppressWarnings("DataFlowIssue")
    public CleanComponent getComponent(int entityID, Class<? extends CleanComponent> component) {
        ComDataLocation location = componentDataLocations.get(component);
        int index = entityDataIndexes.get(entityID);

        String comName = componentRegistry.getComponentName(component);
        int unitCount = componentRegistry.getComponentDescFlattened(comName).getUnitCount();

        int argIndex = 0;
        Object[] args = new Object[unitCount];

        int byteArrIndex = location.byteArrFrom;
        int shortArrIndex = location.shortArrFrom;
        int intArrIndex = location.intArrFrom;
        int longArrIndex = location.longArrFrom;
        int floatArrIndex = location.floatArrFrom;
        int doubleArrIndex = location.doubleArrFrom;
        int booleanArrIndex = location.booleanArrFrom;
        for (FlattenedScalarType flattenedScalarType : location.order) {
            if (flattenedScalarType == FlattenedScalarType.BYTE) {
                args[argIndex] = bytePool.get(byteArrIndex)[index];
                byteArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                args[argIndex] = shortPool.get(shortArrIndex)[index];
                shortArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.INT) {
                args[argIndex] = intPool.get(intArrIndex)[index];
                intArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                args[argIndex] = longPool.get(longArrIndex)[index];
                longArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                args[argIndex] = floatPool.get(floatArrIndex)[index];
                floatArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                args[argIndex] = doublePool.get(doubleArrIndex)[index];
                doubleArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                args[argIndex] = booleanPool.get(booleanArrIndex)[index];
                booleanArrIndex++;
            }
            argIndex++;
        }

        return componentRegistry.newComponent(comName, args);
    }

    @Override
    public void setComponent(int entityID, CleanComponent component) {
        Object[] args = componentRegistry.flattenComponent(component);

        ComDataLocation location = componentDataLocations.get(component.getClass());
        int index = entityDataIndexes.get(entityID);

        int argIndex = 0;
        int byteArrIndex = location.byteArrFrom;
        int shortArrIndex = location.shortArrFrom;
        int intArrIndex = location.intArrFrom;
        int longArrIndex = location.longArrFrom;
        int floatArrIndex = location.floatArrFrom;
        int doubleArrIndex = location.doubleArrFrom;
        int booleanArrIndex = location.booleanArrFrom;
        for (FlattenedScalarType flattenedScalarType : location.order) {
            if (flattenedScalarType == FlattenedScalarType.BYTE) {
                bytePool.get(byteArrIndex)[index] = (Byte) args[argIndex];
                byteArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                shortPool.get(shortArrIndex)[index] = (Short) args[argIndex];
                shortArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.INT) {
                intPool.get(intArrIndex)[index] = (Integer) args[argIndex];
                intArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                longPool.get(longArrIndex)[index] = (Long) args[argIndex];
                longArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                floatPool.get(floatArrIndex)[index] = (Float) args[argIndex];
                floatArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                doublePool.get(doubleArrIndex)[index] = (Double) args[argIndex];
                doubleArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                booleanPool.get(booleanArrIndex)[index] = (Boolean) args[argIndex];
                booleanArrIndex++;
            }
            argIndex++;
        }
    }

    @Override
    public void addEntity(int entityID, List<CleanComponent> components) {
        int index;
        if (freeIndexes.isEmpty()) {
            // grow pool
            if (indexCounter >= currentSize) {
                currentSize += growStep;
                bytePool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                shortPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                intPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                floatPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                doublePool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                booleanPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
            }
            index = indexCounter++;
        } else {
            index = freeIndexes.removeLast();
        }

        entityDataIndexes.put(entityID, index);

        for (Class<? extends CleanComponent> clazz : this.components) {
            CleanComponent component = Objects.requireNonNull(components.stream().filter(c -> c.getClass().equals(clazz)).findFirst().orElse(null));

            Object[] args = componentRegistry.flattenComponent(component);
            ComDataLocation location = componentDataLocations.get(clazz);

            int argIndex = 0;
            int byteArrIndex = location.byteArrFrom;
            int shortArrIndex = location.shortArrFrom;
            int intArrIndex = location.intArrFrom;
            int longArrIndex = location.longArrFrom;
            int floatArrIndex = location.floatArrFrom;
            int doubleArrIndex = location.doubleArrFrom;
            int booleanArrIndex = location.booleanArrFrom;
            for (FlattenedScalarType flattenedScalarType : location.order) {
                if (flattenedScalarType == FlattenedScalarType.BYTE) {
                    bytePool.get(byteArrIndex)[index] = (Byte) args[argIndex];
                    byteArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                    shortPool.get(shortArrIndex)[index] = (Short) args[argIndex];
                    shortArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.INT) {
                    intPool.get(intArrIndex)[index] = (Integer) args[argIndex];
                    intArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                    longPool.get(longArrIndex)[index] = (Long) args[argIndex];
                    longArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                    floatPool.get(floatArrIndex)[index] = (Float) args[argIndex];
                    floatArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                    doublePool.get(doubleArrIndex)[index] = (Double) args[argIndex];
                    doubleArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                    booleanPool.get(booleanArrIndex)[index] = (Boolean) args[argIndex];
                    booleanArrIndex++;
                }
                argIndex++;
            }
        }
    }

    @Override
    public void removeEntity(int entityID) {
        int index = entityDataIndexes.remove(entityID);
        if (index == indexCounter - 1) {
            indexCounter--;
            List<Integer> freeIndexesToRemove = new ArrayList<>();
            while (freeIndexes.contains(indexCounter - 1)) {
                freeIndexesToRemove.add(--indexCounter);
            }
            for (Integer freeIndex : freeIndexesToRemove) {
                freeIndexes.remove(freeIndex);
            }
            // shrink pool
            if (indexCounter + shrinkStep <= currentSize) {
                if (currentSize - shrinkStep >= initSize) {
                    currentSize -= shrinkStep;
                    bytePool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    shortPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    intPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    floatPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    doublePool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                    booleanPool.replaceAll(original -> Arrays.copyOf(original, currentSize));
                }
            }
        } else {
            freeIndexes.add(index);
        }
    }

    @NonNull
    @Override
    public PrimitiveArray getArray(Class<? extends CleanComponent> component, String... fieldAccessChain) {
        int ordinal = componentRegistry.getFieldOrdinal(componentRegistry.getComponentName(component), fieldAccessChain);
        ComDataLocation location = componentDataLocations.get(component);

        int index = 0;
        int byteArrIndex = location.byteArrFrom;
        int shortArrIndex = location.shortArrFrom;
        int intArrIndex = location.intArrFrom;
        int longArrIndex = location.longArrFrom;
        int floatArrIndex = location.floatArrFrom;
        int doubleArrIndex = location.doubleArrFrom;
        int booleanArrIndex = location.booleanArrFrom;
        for (FlattenedScalarType flattenedScalarType : location.order) {
            if (flattenedScalarType == FlattenedScalarType.BYTE) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(bytePool.get(byteArrIndex));
                }
                byteArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(shortPool.get(shortArrIndex));
                }
                shortArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.INT) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(intPool.get(intArrIndex));
                }
                intArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(longPool.get(longArrIndex));
                }
                longArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(floatPool.get(floatArrIndex));
                }
                floatArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(doublePool.get(doubleArrIndex));
                }
                doubleArrIndex++;
            } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                if (index == ordinal) {
                    return new HeapPrimitiveArray(booleanPool.get(booleanArrIndex));
                }
                booleanArrIndex++;
            }
            index++;
        }

        throw new IllegalArgumentException("Unable to find such array.");
    }

    @NonNull
    @Override
    public ArrayRange getArrayRange() {
        return new ArrayRange(0, indexCounter, new HashSet<>(freeIndexes));
    }

    @NonNull
    @Override
    public Optional<Integer> getEntityID(int index) {
        return Optional.ofNullable(entityDataIndexes.inverse().get(index));
    }

    @Override
    public String getSnapshot() {
        int snapshotLength = Math.min(currentSize, 10);

        StringBuilder builder = new StringBuilder();
        builder.append("\n=====HeapPool Snapshot=====\n");
        int i = 0;
        for (Class<? extends CleanComponent> clazz : components) {
            ComDataLocation location = componentDataLocations.get(clazz);
            builder.append("[").append(i++).append("] ")
                    .append("Component name: ").append(componentRegistry.getComponentName(clazz))
                    .append("; Component class: ").append(clazz.getName()).append("\n");

            int byteArrIndex = location.byteArrFrom;
            int shortArrIndex = location.shortArrFrom;
            int intArrIndex = location.intArrFrom;
            int longArrIndex = location.longArrFrom;
            int floatArrIndex = location.floatArrFrom;
            int doubleArrIndex = location.doubleArrFrom;
            int booleanArrIndex = location.booleanArrFrom;
            int j = 0;
            for (FlattenedScalarType flattenedScalarType : location.order) {
                builder.append("  [").append(j++).append(" ").append(flattenedScalarType).append("] ");
                if (flattenedScalarType == FlattenedScalarType.BYTE) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(bytePool.get(byteArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    byteArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.SHORT) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(shortPool.get(shortArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    shortArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.INT) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(intPool.get(intArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    intArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.LONG) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(longPool.get(longArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    longArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.FLOAT) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(floatPool.get(floatArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    floatArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.DOUBLE) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(doublePool.get(doubleArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    doubleArrIndex++;
                } else if (flattenedScalarType == FlattenedScalarType.BOOL) {
                    for (int k = 0; k < snapshotLength; k++) {
                        builder.append(booleanPool.get(booleanArrIndex)[k]);
                        if (k != snapshotLength - 1) {
                            builder.append(", ");
                        }
                    }
                    booleanArrIndex++;
                }
                builder.append("\n");
            }
        }

        return builder.toString();
    }
}
