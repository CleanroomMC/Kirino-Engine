package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.BufferView;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>A buffer storage consists of multiple pages (every page is an individual coherent-persistently-mapped buffer).</p>
 * <p>Every page is supposed to be huge in size, and a new page will be allocated when this buffer storage is full.
 * Every page will be split into slots dynamically, and slots can be freed to upload new data.</p>
 *
 * <p>Note: This class is not thread-safe. Allocation and release must happen on the thread that owns
 * the OpenGL context.</p>
 */
public class BufferStorage<T extends BufferView> {

    private final int pageSize;
    private final boolean dsa;
    private final boolean validatePageViewMode;

    private final List<T> pages = new ArrayList<>();
    private final List<PageMeta> metas = new ArrayList<>();
    private final Supplier<T> pageFactory;

    private final Deque<Integer> pagesWithSpace = new ArrayDeque<>();

    // key: slot id
    private final Map<Long, SlotHandle<T>> activeSlots = new HashMap<>();

    private long nextSlotId = 1L;

    /**
     * Creates a buffer storage using the legacy target-bound allocation path.
     */
    public BufferStorage(Supplier<T> pageFactory, int pageSize) {
        this(pageFactory, pageSize, false, false);
    }

    /**
     * Creates a buffer storage using either legacy target-bound or DSA operations.
     *
     * <p>When <code>dsa</code> is true, <code>pageFactory</code> must create DSA-enabled views backed
     * by instantiated buffers (DSA-created or bound once).</p>
     */
    public BufferStorage(Supplier<T> pageFactory, int pageSize, boolean dsa) {
        this(pageFactory, pageSize, dsa, true);
    }

    private BufferStorage(
            Supplier<T> pageFactory,
            int pageSize,
            boolean dsa,
            boolean validatePageViewMode) {

        Preconditions.checkNotNull(pageFactory);
        Preconditions.checkArgument(pageSize > 0,
                "Argument \"pageSize\" must be positive.");

        this.pageSize = pageSize;
        this.dsa = dsa;
        this.validatePageViewMode = validatePageViewMode;
        this.pageFactory = pageFactory;
    }

    /**
     * It needs to be called on the main thread that has the GL context.
     *
     * <p>Note: It won't change GL buffer binding.</p>
     */
    private void allocPage() {
        T view = pageFactory.get();
        Preconditions.checkNotNull(view);
        if (validatePageViewMode) {
            Preconditions.checkState(view.isDsaView() == dsa,
                    "The page BufferView DSA mode must match its BufferStorage.");
        }

        if (dsa) {
            allocPageStorage(view);
        } else {
            int currentBufferID = view.fetchCurrentBoundBufferID();
            view.bind();
            allocPageStorage(view);
            view.bind(currentBufferID);
        }

        int pageIndex = pages.size();
        pages.add(view);
        metas.add(new PageMeta(pageSize));
        pagesWithSpace.addLast(pageIndex);
    }

    private void allocPageStorage(T view) {
        view.allocPersistent(
                pageSize,
                MapBufferAccessBit.WRITE_BIT,
                MapBufferAccessBit.MAP_PERSISTENT_BIT,
                MapBufferAccessBit.MAP_COHERENT_BIT);
        view.mapPersistent(
                0,
                pageSize,
                MapBufferAccessBit.WRITE_BIT,
                MapBufferAccessBit.MAP_PERSISTENT_BIT,
                MapBufferAccessBit.MAP_COHERENT_BIT);
    }

    /**
     * Allocates a slot with the given size in bytes.
     *
     * @param size The size by bytes
     * @return A slot handle
     */
    @NonNull
    public SlotHandle<T> allocate(int size) {
        Preconditions.checkArgument(size > 0,
                "Argument \"size\" must be positive.");
        Preconditions.checkArgument(size <= pageSize,
                "Argument \"size\"=%s must be smaller than or equal to the page size=%s.", size, pageSize);

        Integer pageIndex = findPageWithSpace(size);
        if (pageIndex == null) {
            allocPage();
            pageIndex = pages.size() - 1;
        }

        PageMeta meta = metas.get(pageIndex);
        SlotRegion region = meta.allocate(size);
        if (!meta.hasFreeSpace()) {
            pagesWithSpace.remove(pageIndex);
        }

        long slotId = nextSlotId++;
        SlotHandle<T> slot = new SlotHandle<>(
                slotId,
                pageIndex,
                region.offset,
                region.length,
                pages.get(pageIndex),
                this);
        activeSlots.put(slotId, slot);
        return slot;
    }

    protected void releaseSlot(@NonNull SlotHandle<T> slot) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkArgument(slot.owner == this,
                "Argument \"slot\" is owned by another BufferStorage.");
        Preconditions.checkState(activeSlots.remove(slot.slotId) == slot,
                "SlotHandle (id=%s) is not an active slot of this BufferStorage.", slot.slotId);

        int pageIndex = slot.pageIndex;
        Preconditions.checkElementIndex(pageIndex, metas.size());

        PageMeta meta = metas.get(pageIndex);
        boolean hadFreeSpace = meta.hasFreeSpace();

        meta.free(slot.offset, slot.size);

        if (!hadFreeSpace && meta.hasFreeSpace()) {
            pagesWithSpace.addLast(pageIndex);
        }

        Consumer<SlotHandle<T>> listener = slot.releaseCallback;
        slot.releaseCallback = null;
        if (listener != null) {
            listener.accept(slot);
        }
    }

    @NonNull
    public T getPage(int index) {
        Preconditions.checkElementIndex(index, pages.size());

        return pages.get(index);
    }

    public int getPageCount() {
        return pages.size();
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isDsa() {
        return dsa;
    }

    private Integer findPageWithSpace(int size) {
        for (int index : pagesWithSpace) {
            PageMeta meta = metas.get(index);
            if (meta.maxFree >= size) {
                return index;
            }
        }
        return null;
    }

    /**
     * Registers the callback directly on an active slot.
     */
    void registerReleaseListener(long slotId, Consumer<SlotHandle<T>> listener) {
        Preconditions.checkNotNull(listener);

        SlotHandle<T> slot = activeSlots.get(slotId);
        Preconditions.checkState(slot != null,
                "SlotHandle (id=%s) must be active when modifying the callback.", slotId);

        slot.releaseCallback = listener;
    }

    /**
     * Removes the callback from an active slot.
     */
    void unregisterReleaseListener(long slotId) {
        SlotHandle<T> slot = activeSlots.get(slotId);
        if (slot != null) {
            slot.releaseCallback = null;
        }
    }

    public static class SlotHandle<T extends BufferView> {

        private final long slotId;
        private final int pageIndex;
        private final int offset;
        private final int size;
        private final T view;
        private final BufferStorage<T> owner;
        private Consumer<SlotHandle<T>> releaseCallback;
        private boolean released = false;

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

        @NonNull
        public T getView() {
            return view;
        }

        public void setReleaseCallback(@NonNull Consumer<SlotHandle<T>> callback) {
            Preconditions.checkState(!released,
                    "SlotHandle (id=%s) must be unreleased when modifying the callback.", slotId);
            Preconditions.checkNotNull(callback);

            owner.registerReleaseListener(slotId, callback);
        }

        public void removeReleaseCallback() {
            Preconditions.checkState(!released,
                    "SlotHandle (id=%s) must be unreleased when modifying the callback.", slotId);

            owner.unregisterReleaseListener(slotId);
        }

        public void release() {
            if (released) {
                return;
            }

            released = true;
            owner.releaseSlot(this);
        }

        public boolean isReleased() {
            return released;
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

        PageMeta(int capacity) {
            freeRanges.put(0, capacity);
            maxFree = capacity;
        }

        SlotRegion allocate(int size) {
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

        void free(int offset, int length) {
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

        boolean hasFreeSpace() {
            return !freeRanges.isEmpty();
        }

        private void recalcMaxFree() {
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
