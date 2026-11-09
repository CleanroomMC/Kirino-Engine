package com.cleanroommc.kirino.ecs;

final class PoolCapacity {
    static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;

    private PoolCapacity() {
    }

    static int grow(int capacity, int required) {
        if (required < 0 || required > MAX_CAPACITY) {
            throw new OutOfMemoryError("Required pool capacity exceeds the supported array length");
        }
        long geometric = (long) capacity + Math.max(1, capacity / 2);
        return (int) Math.min(MAX_CAPACITY, Math.max(geometric, required));
    }

    static int shrink(int capacity, int rowLimit, int minimum) {
        if (capacity <= minimum || rowLimit > capacity / 4) {
            return capacity;
        }
        return Math.max(minimum, rowLimit * 2);
    }
}
