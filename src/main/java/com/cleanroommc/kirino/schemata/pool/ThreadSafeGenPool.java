package com.cleanroommc.kirino.schemata.pool;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.locks.ReentrantLock;

public abstract class ThreadSafeGenPool<T> {
    private final T[] slots;
    private final int[] freeIndices;
    private int freeTop;

    private final boolean[] lended;
    private final Handle<T>[] handles;

    private final ReentrantLock lock = new ReentrantLock();

    private long gen;

    private final int capacity;

    @SuppressWarnings("unchecked")
    public ThreadSafeGenPool(int capacity) {
        this.capacity = capacity;
        slots = (T[]) new Object[capacity];
        freeIndices = new int[capacity];
        lended = new boolean[capacity];
        handles = (Handle<T>[]) new Handle[capacity];
        for (int i = 0; i < capacity; i++) {
            handles[i] = new Handle<>(this, i);
            slots[i] = newObject(handles[i]);
            freeIndices[i] = i;
            lended[i] = false;
        }
        freeTop = capacity - 1;
        gen = 0;
    }

    public int capacity() {
        return capacity;
    }

    public int remainingFree() {
        lock.lock();
        try {
            return freeTop + 1;
        } finally {
            lock.unlock();
        }
    }

    public void nextGen() {
        lock.lock();
        try {
            gen++;
            freeTop = capacity - 1;
            for (int i = 0; i < capacity; i++) {
                if (lended[i]) {
                    handles[i] = new Handle<>(this, i);
                    slots[i] = newObject(handles[i]);
                } else {
                    handles[i].reset(gen);
                }
                lended[i] = false;
                freeIndices[i] = i;
            }
        } finally {
            lock.unlock();
        }
    }

    @NonNull
    public T lend() {
        lock.lock();
        try {
            if (freeTop >= 0) {
                int index = freeIndices[freeTop--];
                lended[index] = true;
                Handle<T> handle = handles[index];
                handle.reset(gen);
                return slots[index];
            }
        } finally {
            lock.unlock();
        }
        Handle<T> tempHandle = new Handle<>(null, -1);
        return newObject(tempHandle);
    }

    @NonNull
    public abstract T newObject(@NonNull Handle<T> handle);

    public static class Handle<T> {
        private final ThreadSafeGenPool<T> pool;
        private final int index;
        private boolean recycled;
        private long gen;

        public Handle(ThreadSafeGenPool<T> pool, int index) {
            this.pool = pool;
            this.index = index;
        }

        public void recycle() {
            if (!isInPool()) {
                return;
            }
            pool.lock.lock();
            try {
                if (gen != pool.gen) {
                    return;
                }
                if (recycled) {
                    return;
                }
                recycled = true;
                pool.lended[index] = false;
                pool.freeIndices[++pool.freeTop] = index;
            } finally {
                pool.lock.unlock();
            }
        }

        public boolean isInPool() {
            return pool != null && index != -1;
        }

        private void reset(long gen) {
            this.gen = gen;
            recycled = false;
        }
    }
}
