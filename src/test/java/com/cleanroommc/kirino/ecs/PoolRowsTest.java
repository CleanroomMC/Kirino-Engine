package com.cleanroommc.kirino.ecs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PoolRowsTest {
    @Test
    void tailCollapse_unlinksMiddleAndHead_preservesRemainingLifoOrder() {
        PoolRows rows = new PoolRows(8);
        for (int row = 0; row < 8; row++) rows.occupy(rows.nextRow());
        rows.release(5);
        rows.release(1);
        rows.release(3);
        rows.release(7);
        rows.release(6);
        assertEquals(5, rows.limit);
        assertEquals(3, rows.nextRow());
        rows.release(4);
        assertEquals(3, rows.limit);
        assertEquals(1, rows.nextRow());
        rows.occupy(rows.nextRow());
        assertEquals(3, rows.nextRow());
        assertTrue(rows.isOccupied(0));
        assertTrue(rows.isOccupied(1));
        assertTrue(rows.isOccupied(2));
        assertFalse(rows.isOccupied(-1));
        assertFalse(rows.isOccupied(3));
        assertFalse(rows.isOccupied(8));
    }

    @Test
    void randomReuseAndTailCollapse_matchesListModelWithoutChangingLiveRows() {
        int capacity = 127;
        PoolRows rows = new PoolRows(capacity);
        boolean[] live = new boolean[capacity];
        List<Integer> holes = new ArrayList<>();
        Random random = new Random(0x726f7773L);
        int limit = 0;
        int count = 0;
        for (int step = 0; step < 30000; step++) {
            if (count == 0 || count < capacity && random.nextBoolean()) {
                int expected = holes.isEmpty() ? limit++ : holes.removeLast();
                int actual = rows.nextRow();
                assertEquals(expected, actual, "step=" + step);
                rows.occupy(actual);
                live[expected] = true;
                count++;
            } else {
                int row;
                do { row = random.nextInt(limit); } while (!live[row]);
                live[row] = false;
                if (row == limit - 1) {
                    limit--;
                    while (limit > 0 && !live[limit - 1]) {
                        assertTrue(holes.remove(Integer.valueOf(limit - 1)));
                        limit--;
                    }
                } else {
                    holes.add(row);
                }
                rows.release(row);
                count--;
            }
            assertEquals(limit, rows.limit);
            assertEquals(holes.isEmpty() ? limit : holes.getLast(), rows.nextRow());
            for (int row = 0; row < capacity; row++) {
                assertEquals(live[row], rows.isOccupied(row), "step=" + step + ", row=" + row);
            }
        }
    }
}
