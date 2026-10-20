package com.cleanroommc.kirino.gl.texture.atlas;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The atlas should be closed explicitly when not used.
 * Nevertheless, GL resources will be disposed automatically
 * as long as the GL resource initialization goes through <code>com.cleanroommc.kirino.gl</code>.
 * Pages and slots must not be used after closing the atlas.
 * Allocation, release, and closing must happen on the thread that owns the GL context.
 */
public abstract class AbstractPagedPlanarTextureAtlas<TPage> implements AutoCloseable {

    public record TextureData(@NonNull ByteBuffer byteBuffer, @NonNull Object payload) {

        public TextureData {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(payload);
        }
    }

    private record SlotRegion(int x, int y, int reservedWidth, int reservedHeight) {
    }

    private static final class PageMeta {

        private record FreeRect(int x, int y, int width, int height) {
        }

        private final List<FreeRect> freeRects = new ArrayList<>();

        PageMeta(int width, int height) {
            freeRects.add(new FreeRect(0, 0, width, height));
        }

        public boolean canAllocate(int width, int height) {
            for (FreeRect rect : freeRects) {
                if (rect.width >= width && rect.height >= height) {
                    return true;
                }
            }

            return false;
        }

        public boolean hasFreeSpace() {
            return !freeRects.isEmpty();
        }

        @NonNull
        public SlotRegion allocate(int width, int height) {
            for (int i = 0; i < freeRects.size(); i++) {
                FreeRect rect = freeRects.get(i);

                if (rect.width < width || rect.height < height) {
                    continue;
                }

                freeRects.remove(i);

                int x = rect.x;
                int y = rect.y;

                int remainingRightWidth = rect.width - width;
                int remainingBottomHeight = rect.height - height;

                if (remainingRightWidth > 0) {
                    freeRects.add(new FreeRect(
                            rect.x + width,
                            rect.y,
                            remainingRightWidth,
                            height));
                }

                if (remainingBottomHeight > 0) {
                    freeRects.add(new FreeRect(
                            rect.x,
                            rect.y + height,
                            rect.width,
                            remainingBottomHeight));
                }

                return new SlotRegion(x, y, width, height);
            }

            throw new IllegalStateException(
                    "There is no free region large enough for " + width + "x" + height + ".");
        }

        /**
         * Must not double free. Overlap is not allowed.
         */
        public void free(int x, int y, int width, int height) {
            Preconditions.checkArgument(width > 0,
                    "Argument \"width\" must be positive.");
            Preconditions.checkArgument(height > 0,
                    "Argument \"height\" must be positive.");

            FreeRect rect = new FreeRect(x, y, width, height);

            for (FreeRect freeRect : freeRects) {
                Preconditions.checkState(!intersects(rect, freeRect),
                        "Overlapping free region detected: %s (input) overlaps %s.",
                        rect.toString(), freeRect.toString());
            }

            freeRects.add(rect);

            mergeFreeRects();
        }

        private static boolean intersects(FreeRect a, FreeRect b) {
            return a.x < b.x + b.width &&
                    b.x < a.x + a.width &&
                    a.y < b.y + b.height &&
                    b.y < a.y + a.height;
        }

        @SuppressWarnings("SuspiciousListRemoveInLoop")
        private void mergeFreeRects() {
            boolean merged;

            do {
                merged = false;

                outer:
                for (int i = 0; i < freeRects.size(); i++) {
                    FreeRect a = freeRects.get(i);

                    for (int j = i + 1; j < freeRects.size(); j++) {
                        FreeRect b = freeRects.get(j);

                        FreeRect mergedRect = tryMerge(a, b);

                        if (mergedRect != null) {
                            freeRects.remove(j);
                            freeRects.remove(i);
                            freeRects.add(mergedRect);

                            merged = true;
                            break outer;
                        }
                    }
                }
            } while (merged);
        }

        @Nullable
        private static FreeRect tryMerge(FreeRect a, FreeRect b) {
            if (a.y == b.y && a.height == b.height) {
                if (a.x + a.width == b.x) {
                    return new FreeRect(
                            a.x,
                            a.y,
                            a.width + b.width,
                            a.height);
                }

                if (b.x + b.width == a.x) {
                    return new FreeRect(
                            b.x,
                            b.y,
                            a.width + b.width,
                            a.height);
                }
            }

            if (a.x == b.x && a.width == b.width) {
                if (a.y + a.height == b.y) {
                    return new FreeRect(
                            a.x,
                            a.y,
                            a.width,
                            a.height + b.height);
                }

                if (b.y + b.height == a.y) {
                    return new FreeRect(
                            b.x,
                            b.y,
                            b.width,
                            a.height + b.height);
                }
            }

            return null;
        }
    }

    private final int pageWidth;
    private final int pageHeight;

    private final boolean powerOfTwoSlots;

    // these two share indices
    private final List<TPage> pages = new ArrayList<>();
    private final List<PageMeta> metas = new ArrayList<>();

    // element: page index
    private final Deque<Integer> pagesWithSpace = new ArrayDeque<>();

    // key: slot id
    private final Map<Long, Consumer<SlotHandle<TPage>>> releaseListeners = new HashMap<>();

    private final Supplier<TPage> pageFactory;
    protected final PayloadInterpreter payloadInterpreter;

    private long nextSlotId = 1L;
    private int pageCount = 0;

    public AbstractPagedPlanarTextureAtlas(
            @NonNull Supplier<TPage> pageFactory,
            int pageWidth,
            int pageHeight,
            boolean powerOfTwoSlots,
            @NonNull PayloadInterpreter payloadInterpreter) {

        Preconditions.checkNotNull(pageFactory);
        Preconditions.checkNotNull(payloadInterpreter);
        Preconditions.checkArgument(pageWidth > 0,
                "Argument \"pageWidth\" must be positive.");
        Preconditions.checkArgument(pageHeight > 0,
                "Argument \"pageHeight\" must be positive.");

        if (powerOfTwoSlots) {
            Preconditions.checkArgument(Integer.bitCount(pageWidth) == 1,
                    "Argument \"pageWidth\" must be power-of-two when \"powerOfTwoSlots\" is enabled.");
            Preconditions.checkArgument(Integer.bitCount(pageHeight) == 1,
                    "Argument \"pageHeight\" must be power-of-two when \"powerOfTwoSlots\" is enabled.");
        }

        this.pageFactory = pageFactory;
        this.payloadInterpreter = payloadInterpreter;
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.powerOfTwoSlots = powerOfTwoSlots;
    }

    private static int roundUpToPowerOfTwo(int value) {
        Preconditions.checkArgument(value > 0,
                "Argument \"value\" must be positive.");
        Preconditions.checkArgument(value <= (1 << 30),
                "Argument \"value\"=%s is too large to round up to a positive power of two.",
                value);

        if (Integer.bitCount(value) == 1) {
            return value;
        }

        return Integer.highestOneBit(value) << 1;
    }

    @NonNull
    public SlotHandle<TPage> allocate(@NonNull ByteBuffer byteBuffer, @NonNull Object payload) {
        Preconditions.checkNotNull(byteBuffer);
        Preconditions.checkNotNull(payload);

        return allocate(new TextureData(byteBuffer, payload));
    }

    @NonNull
    public SlotHandle<TPage> allocate(@NonNull TextureData data) {
        Preconditions.checkNotNull(data);

        int width = payloadInterpreter.width(data.payload());
        int height = payloadInterpreter.height(data.payload());

        Preconditions.checkArgument(width > 0,
                "Image width must be positive.");
        Preconditions.checkArgument(height > 0,
                "Image height must be positive.");
        Preconditions.checkArgument(width <= pageWidth,
                "Image width=%s exceeds atlas page width=%s.", width, pageWidth);
        Preconditions.checkArgument(height <= pageHeight,
                "Image height=%s exceeds atlas page height=%s.", height, pageHeight);

        int potWidth = powerOfTwoSlots ? roundUpToPowerOfTwo(width) : -1;
        int potHeight = powerOfTwoSlots ? roundUpToPowerOfTwo(height) : -1;

        if (powerOfTwoSlots) {
            Preconditions.checkArgument(potWidth <= pageWidth,
                    "Image power-of-two reserved width=%s exceeds atlas page width=%s.", potWidth, pageWidth);
            Preconditions.checkArgument(potHeight <= pageHeight,
                    "Image power-of-two reserved height=%s exceeds atlas page height=%s.", potHeight, pageHeight);
        }

        Integer pageIndex;
        if (powerOfTwoSlots) {
            pageIndex = findPageWithSpace(potWidth, potHeight);
        } else {
            pageIndex = findPageWithSpace(width, height);
        }

        if (pageIndex == null) {
            allocPage();
            pageIndex = pageCount - 1;
        }

        PageMeta meta = metas.get(pageIndex);
        SlotRegion region;
        if (powerOfTwoSlots) {
            region = meta.allocate(potWidth, potHeight);
        } else {
            region = meta.allocate(width, height);
        }

        if (!meta.hasFreeSpace()) {
            pagesWithSpace.remove(pageIndex);
        }

        long slotId = nextSlotId++;

        SlotHandle<TPage> slot = new SlotHandle<>(
                slotId,
                pageIndex,
                region.x,
                region.y,
                width,
                height,
                region.reservedWidth,
                region.reservedHeight,
                pages.get(pageIndex),
                this);

        try {
            uploadSection(slot, data);
        } catch (RuntimeException | Error exception) {
            slot.release();
            throw exception;
        }

        return slot;
    }

    public TPage getPage(int index) {
        Preconditions.checkElementIndex(index, pages.size());

        return pages.get(index);
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getPageWidth() {
        return pageWidth;
    }

    public int getPageHeight() {
        return pageHeight;
    }

    protected abstract void initPage(@NonNull TPage page, int width, int height);

    protected abstract void uploadSection(@NonNull SlotHandle<TPage> slot, @NonNull TextureData data);

    private void allocPage() {
        TPage page = pageFactory.get();
        Preconditions.checkNotNull(page);

        initPage(page, pageWidth, pageHeight);

        pages.add(page);
        metas.add(new PageMeta(pageWidth, pageHeight));
        pagesWithSpace.addLast(pageCount);

        pageCount++;
    }

    private void releaseSlot(@NonNull SlotHandle<TPage> slot) {
        Preconditions.checkNotNull(slot);

        int pageIndex = slot.pageIndex;
        Preconditions.checkElementIndex(pageIndex, metas.size());

        PageMeta meta = metas.get(pageIndex);
        boolean hadFreeSpace = meta.hasFreeSpace();

        meta.free(slot.x, slot.y, slot.reservedWidth, slot.reservedHeight);

        if (!hadFreeSpace && meta.hasFreeSpace()) {
            pagesWithSpace.addLast(pageIndex);
        }

        Consumer<SlotHandle<TPage>> listener = releaseListeners.remove(slot.slotId);

        if (listener != null) {
            listener.accept(slot);
        }
    }

    @Nullable
    private Integer findPageWithSpace(int width, int height) {
        for (int index : pagesWithSpace) {
            PageMeta meta = metas.get(index);
            if (meta.canAllocate(width, height)) {
                return index;
            }
        }

        return null;
    }

    private void registerReleaseListener(long slotId, Consumer<SlotHandle<TPage>> listener) {
        releaseListeners.put(slotId, listener);
    }

    private void unregisterReleaseListener(long slotId) {
        releaseListeners.remove(slotId);
    }

    public static final class SlotHandle<T> {

        private final long slotId;

        private final int pageIndex;

        private final int x;
        private final int y;

        private final int width;
        private final int height;

        private final int reservedWidth;
        private final int reservedHeight;

        private final T page;
        private final AbstractPagedPlanarTextureAtlas<T> owner;

        private boolean released = false;

        private SlotHandle(
                long slotId,
                int pageIndex,
                int x,
                int y,
                int width,
                int height,
                int reservedWidth,
                int reservedHeight,
                T page,
                AbstractPagedPlanarTextureAtlas<T> owner) {

            this.slotId = slotId;
            this.pageIndex = pageIndex;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.reservedWidth = reservedWidth;
            this.reservedHeight = reservedHeight;
            this.page = page;
            this.owner = owner;
        }

        public long getSlotId() {
            return slotId;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getReservedWidth() {
            return reservedWidth;
        }

        public int getReservedHeight() {
            return reservedHeight;
        }

        public T getPage() {
            return page;
        }

        public float u0(int pageWidth, float pixelOffset) {
            return (x + pixelOffset) / pageWidth;
        }

        public float v0(int pageHeight, float pixelOffset) {
            return (y + pixelOffset) / pageHeight;
        }

        public float u1(int pageWidth, float pixelOffset) {
            return (x + width - pixelOffset) / pageWidth;
        }

        public float v1(int pageHeight, float pixelOffset) {
            return (y + height - pixelOffset) / pageHeight;
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
}
