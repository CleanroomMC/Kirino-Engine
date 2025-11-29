package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.BufferView;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>A buffer storage consists of several pages (every page is an individual coherent-persistently-mapped buffer).</p>
 * <p>Every page should be huge in size, and a new page will be allocated when this buffer storage is full.
 * Every page will be split into slots dynamically, and slots can be freed to upload new data.</p>
 */
public class BufferStorage<T extends BufferView> {
    private final int pageSize;

    private final List<T> pages = new ArrayList<>();
    private final List<PageMeta> metas = new ArrayList<>();
    private final Supplier<T> constructor;

    private final Deque<Integer> pagesWithSpace = new ArrayDeque<>();

    // key: slot id
    private final ConcurrentHashMap<Long, Consumer<SlotHandle<T>>> releaseListeners = new ConcurrentHashMap<>();

    private long nextSlotId = 1L;
    private int pageCount = 0;

    public BufferStorage(Supplier<T> constructor, int pageSize) {
        this.pageSize = pageSize;
        this.constructor = constructor;
    }

    /**
     * It needs to be called on the main thread that has the GL context. It won't change GL buffer binding.
     */
    public synchronized void allocPage() {
        T view = constructor.get();
        int currentBufferID = view.fetchCurrentBufferID();
        view.bind();
        view.allocPersistent(pageSize, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        view.mapPersistent(0, pageSize, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        view.bind(currentBufferID);

        pages.add(view);
        metas.add(new PageMeta(pageSize));
        pagesWithSpace.addLast(pageCount);
        pageCount++;
    }

    /**
     * Allocate a slot with the given size in bytes.
     * <br>
     * Thread-safety is guaranteed.
     *
     * @param size The size by bytes
     * @return A slot handle
     */
    @NonNull
    public SlotHandle<T> allocate(int size) {
        Preconditions.checkArgument(size > 0,
                "Argument \"size\" must be positive.");

        synchronized (this) {
            Integer pageIndex = findPageWithSpace(size);
            if (pageIndex == null) {
                allocPage();
                pageIndex = pageCount - 1;
            }

            PageMeta meta = metas.get(pageIndex);
            SlotRegion region = meta.allocate(size);
            if (!meta.hasFreeSpace()) {
                pagesWithSpace.remove(pageIndex);
            }

            final long slotId = nextSlotId++;
            return new SlotHandle<>(slotId, pageIndex, region.offset, region.length, pages.get(pageIndex), this);
        }
    }

    protected void releaseSlot(@NonNull SlotHandle<T> slot) {
        Preconditions.checkNotNull(slot);

        synchronized (this) {
            int pageIndex = slot.pageIndex;
            Preconditions.checkElementIndex(pageIndex, metas.size());

            PageMeta meta = metas.get(pageIndex);
            meta.free(slot.offset, slot.size);

            if (meta.hasFreeSpace() && !pagesWithSpace.contains(pageIndex)) {
                pagesWithSpace.addLast(pageIndex);
            }
        }

        Consumer<SlotHandle<T>> listener = releaseListeners.remove(slot.slotId);
        if (listener != null) {
            listener.accept(slot);
        }
    }

    public synchronized T getPage(int index) {
        Preconditions.checkElementIndex(index, pages.size());

        return pages.get(index);
    }

    public synchronized int getPageCount() {
        return pageCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    private Integer findPageWithSpace(int size) {
        Iterator<Integer> iter = pagesWithSpace.iterator();
        while (iter.hasNext()) {
            int index = iter.next();
            PageMeta meta = metas.get(index);
            if (meta.maxFree >= size) {
                return index;
            }
        }
        return null;
    }

    protected void registerReleaseListener(long slotId, Consumer<SlotHandle<T>> listener) {
        releaseListeners.put(slotId, listener);
    }

    protected void unregisterReleaseListener(long slotId) {
        releaseListeners.remove(slotId);
    }

    public static class SlotHandle<T extends BufferView> {
        private final long slotId;
        private final int pageIndex;
        private final int offset;
        private final int size;
        private final T view;
        private final BufferStorage<T> owner;
        private volatile boolean released = false;

        SlotHandle(long slotId, int pageIndex, int offset, int size, T view, BufferStorage<T> owner) {
            this.slotId = slotId;
            this.pageIndex = pageIndex;
            this.offset = offset;
            this.size = size;
            this.view = view;
            this.owner = owner;
        }

        public long getSlotId() {
            return slotId;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public int getOffset() {
            return offset;
        }

        public int getSize() {
            return size;
        }

        public T getView() {
            return view;
        }

        public void setReleaseCallback(Consumer<SlotHandle<T>> callback) {
            owner.registerReleaseListener(slotId, callback);
        }

        /**
         * Thread-safety is guaranteed.
         */
        public void release() {
            if (released) {
                return;
            }

            released = true;
            owner.releaseSlot(this);
        }
    }

    /**
     * It manages the free ranges of a page.
     */
    private static class PageMeta {
        // key: offset
        // value: length
        private final TreeMap<Integer, Integer> freeRanges = new TreeMap<>();
        private int maxFree = 0;
        private final int capacity;

        PageMeta(int capacity) {
            this.capacity = capacity;
            freeRanges.put(0, capacity);
            maxFree = capacity;
        }

        synchronized SlotRegion allocate(int size) {
            Map.Entry<Integer, Integer> candidate = null;
            for (Map.Entry<Integer, Integer> entry : freeRanges.entrySet()) {
                if (entry.getValue() >= size) {
                    candidate = entry;
                    break;
                }
            }

            Preconditions.checkState(candidate != null,
                    "There is no free region large enough (target size: %s) to be allocated.", size);

            int offset = candidate.getKey();
            int length = candidate.getValue();
            if (length == size) {
                freeRanges.remove(offset);
            } else {
                // shrink the free range
                freeRanges.remove(offset);
                freeRanges.put(offset + size, length - size);
            }

            recalcMaxFree();

            return new SlotRegion(offset, size);
        }

        synchronized void free(int offset, int length) {
            Preconditions.checkArgument(offset >= 0,
                    "Argument \"offset\" must be greater than or equal to zero.");
            Preconditions.checkArgument(length >= 0,
                    "Argument \"length\" must be greater than or equal to zero.");

            int start = offset;
            int end = offset + length;

            Integer lowerKey = freeRanges.floorKey(start - 1);
            if (lowerKey != null) {
                int lowerOffset = lowerKey;
                int lowerLength = freeRanges.get(lowerOffset);
                int lowerEnd = lowerOffset + lowerLength;
                if (lowerEnd >= start) {
                    start = Math.min(start, lowerOffset);
                    end = Math.max(end, lowerEnd);
                    freeRanges.remove(lowerOffset);
                }
            }

            Integer key = freeRanges.ceilingKey(start);
            while (key != null) {
                int kOffset = key;
                int kLength = freeRanges.get(kOffset);
                if (kOffset <= end) {
                    end = Math.max(end, kOffset + kLength);
                    freeRanges.remove(kOffset);
                    key = freeRanges.ceilingKey(start);
                } else {
                    break;
                }
            }

            freeRanges.put(start, end - start);

            recalcMaxFree();
        }

        synchronized boolean hasFreeSpace() {
            return !freeRanges.isEmpty();
        }

        synchronized void recalcMaxFree() {
            int max = 0;
            for (int length : freeRanges.values()) {
                if (length > max) {
                    max = length;
                }
            }
            this.maxFree = max;
        }
    }

    /**
     * It represents a region inside a page.
     */
    private record SlotRegion(int offset, int length) {
    }
}
