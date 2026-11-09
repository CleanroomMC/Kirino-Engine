package com.cleanroommc.kirino.ecs;

final class PoolRows {
    int limit;
    int[] nextFree;
    int[] previousFree;
    private int freeHead = -1;

    PoolRows(int capacity) {
        nextFree = new int[capacity];
        previousFree = new int[capacity];
    }

    int nextRow() {
        return freeHead < 0 ? limit : freeHead;
    }

    boolean isOccupied(int row) {
        return row >= 0 && row < limit && nextFree[row] == 0;
    }

    void occupy(int row) {
        if (row == limit) {
            limit++;
        } else {
            unlink(row);
        }
    }

    void release(int row) {
        if (row == limit - 1) {
            limit--;
            while (limit > 0 && nextFree[limit - 1] != 0) {
                unlink(limit - 1);
                limit--;
            }
        } else {
            nextFree[row] = freeHead + 2;
            previousFree[row] = 0;
            if (freeHead >= 0) {
                previousFree[freeHead] = row + 1;
            }
            freeHead = row;
        }
    }

    private void unlink(int row) {
        int previous = previousFree[row] - 1;
        int next = nextFree[row] - 2;
        if (previous < 0) {
            freeHead = next;
        } else {
            nextFree[previous] = next + 2;
        }
        if (next >= 0) {
            previousFree[next] = previous + 1;
        }
        nextFree[row] = 0;
        previousFree[row] = 0;
    }
}
