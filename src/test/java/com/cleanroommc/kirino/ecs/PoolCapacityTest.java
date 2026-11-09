package com.cleanroommc.kirino.ecs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class PoolCapacityTest {
    @ParameterizedTest
    @CsvSource({"0,1,1", "1,2,2", "2,3,3", "3,4,4", "100,101,150", "100,1000,1000",
        "1500000000,1500000001,2147483639", "2147483638,2147483639,2147483639"})
    void grow_validRequest_growsGeometricallyWithoutOverflow(int current, int required, int expected) {
        assertEquals(expected, PoolCapacity.grow(current, required));
    }

    @Test
    void grow_unrepresentableLength_rejectsBeforeAllocation() {
        assertThrows(OutOfMemoryError.class, () -> PoolCapacity.grow(PoolCapacity.MAX_CAPACITY,
            PoolCapacity.MAX_CAPACITY + 1));
        assertThrows(OutOfMemoryError.class, () -> PoolCapacity.grow(100, -1));
    }

    @ParameterizedTest
    @CsvSource({"100,0,100,100", "100,0,0,0", "100,26,0,100", "100,25,0,50",
        "100,24,60,60", "100,0,10,10", "3,1,0,3", "4,1,0,2",
        "2147483639,536870909,0,1073741818"})
    void compact_sparseRows_keepsMinimumAndGrowthHeadroom(int current, int limit, int minimum, int expected) {
        assertEquals(expected, PoolCapacity.shrink(current, limit, minimum));
    }
}
