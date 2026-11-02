package com.cleanroommc.kirino.gl.texture.atlas;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DArrayAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public class Tex2DArrayAtlas extends AbstractPagedPlanarTextureAtlas<Tex2DArrayAtlas.LayerPage> {

    private static final int DEFAULT_INITIAL_LAYER_CAPACITY = 4;

    @Override
    public void close() {
        storage.texture.texture.disposeManually();
    }

    public static final class LayerPage {

        private final SharedStorage storage;
        private final int layer;

        private LayerPage(@NonNull SharedStorage storage, int layer) {
            Preconditions.checkNotNull(storage);

            this.storage = storage;
            this.layer = layer;
        }

        /**
         * <p>Note: Must not cache it as it might be replaced when the atlas grows.</p>
         */
        @NonNull
        public Texture2DArrayAccessor getTexture() {
            return storage.texture;
        }

        public int getLayer() {
            return layer;
        }
    }

    private static final class SharedStorage {

        private final int pageWidth;
        private final int pageHeight;
        private final int maxLayerCount;
        private final TextureFormat format;
        private final FilterMode filterModeMin;
        private final FilterMode filterModeMag;
        private final WrapMode wrapModeS;
        private final WrapMode wrapModeT;
        private final int unpackAlignment;

        private Texture2DArrayAccessor texture;

        private int layerCapacity;
        private int allocatedPageCount = 0;

        private SharedStorage(
                int pageWidth,
                int pageHeight,
                int initialLayerCapacity,
                @NonNull TextureFormat format,
                @NonNull FilterMode filterModeMin,
                @NonNull FilterMode filterModeMag,
                @NonNull WrapMode wrapModeS,
                @NonNull WrapMode wrapModeT,
                int unpackAlignment) {

            Preconditions.checkNotNull(format);
            Preconditions.checkNotNull(filterModeMin);
            Preconditions.checkNotNull(filterModeMag);
            Preconditions.checkNotNull(wrapModeS);
            Preconditions.checkNotNull(wrapModeT);
            Preconditions.checkArgument(unpackAlignment == 1 || unpackAlignment == 2 || unpackAlignment == 4 || unpackAlignment == 8,
                    "Argument \"unpackAlignment\"=%s must be 1, 2, 4, or 8.", unpackAlignment);
            Preconditions.checkArgument(pageWidth > 0,
                    "Argument \"pageWidth\" must be positive.");
            Preconditions.checkArgument(pageHeight > 0,
                    "Argument \"pageHeight\" must be positive.");
            Preconditions.checkArgument(initialLayerCapacity > 0,
                    "Argument \"initialLayerCapacity\" must be positive.");

            this.format = format;
            this.filterModeMin = filterModeMin;
            this.filterModeMag = filterModeMag;
            this.wrapModeS = wrapModeS;
            this.wrapModeT = wrapModeT;
            this.unpackAlignment = unpackAlignment;
            this.pageWidth = pageWidth;
            this.pageHeight = pageHeight;
            maxLayerCount = KirinoClientCore.GL_DEVICE_INFO.getMaxArrayTextureLayers();

            Preconditions.checkArgument(initialLayerCapacity <= maxLayerCount,
                    "Initial layer capacity=%s exceeds maximum=%s.",
                    initialLayerCapacity,
                    maxLayerCount);

            layerCapacity = initialLayerCapacity;
            texture = createTexture(initialLayerCapacity);
        }

        @NonNull
        private LayerPage allocatePage() {
            int layer = allocatedPageCount;
            ensureCapacity(layer + 1);
            allocatedPageCount++;
            return new LayerPage(this, layer);
        }

        private void ensureCapacity(int requiredLayerCount) {
            Preconditions.checkArgument(requiredLayerCount > 0);

            if (requiredLayerCount <= layerCapacity) {
                return;
            }

            Preconditions.checkState(requiredLayerCount <= maxLayerCount,
                    "Required layer count=%s exceeds maximum=%s.",
                    requiredLayerCount,
                    maxLayerCount);

            int newCapacity = layerCapacity;

            while (newCapacity < requiredLayerCount) {
                if (newCapacity > maxLayerCount / 2) {
                    newCapacity = maxLayerCount;
                } else {
                    newCapacity *= 2;
                }
            }

            grow(newCapacity);
        }

        private void grow(int newCapacity) {
            Preconditions.checkArgument(newCapacity > layerCapacity);
            Preconditions.checkArgument(newCapacity <= maxLayerCount);

            Texture2DArrayAccessor oldTexture = texture;
            Texture2DArrayAccessor newTexture = createTexture(newCapacity);

            if (allocatedPageCount > 0) {
                GL43.glCopyImageSubData(
                        oldTexture.textureID(),
                        oldTexture.target(),
                        0,
                        0,
                        0,
                        0,
                        newTexture.textureID(),
                        newTexture.target(),
                        0,
                        0,
                        0,
                        0,
                        pageWidth,
                        pageHeight,
                        allocatedPageCount);
            }

            texture = newTexture;
            layerCapacity = newCapacity;

            oldTexture.texture.disposeManually();
        }

        @NonNull
        private Texture2DArrayAccessor createTexture(int layers) {
            Texture2DArrayAccessor result = new Texture2DArrayAccessor(true, GLTexture.newDsaTex2DArray(
                    pageWidth,
                    pageHeight,
                    layers));

            result.highlevel().allocEmpty(false, format);
            result.highlevel().clearLevel(0);

            result.setCommonParams(filterModeMin, filterModeMag, wrapModeS, wrapModeT);

            return result;
        }
    }

    private final SharedStorage storage;

    /**
     * <p>Note: Must not cache it as it might be replaced when the atlas grows.</p>
     */
    @NonNull
    public Texture2DArrayAccessor getTexture() {
        return storage.texture;
    }

    public Tex2DArrayAtlas(
            int pageWidth,
            int pageHeight,
            @NonNull TextureFormat format,
            @NonNull FilterMode filterModeMin,
            @NonNull FilterMode filterModeMag,
            @NonNull WrapMode wrapModeS,
            @NonNull WrapMode wrapModeT,
            int unpackAlignment,
            @NonNull PayloadInterpreter payloadInterpreter) {

        this(pageWidth, pageHeight, DEFAULT_INITIAL_LAYER_CAPACITY, false, format,
                filterModeMin, filterModeMag, wrapModeS, wrapModeT, unpackAlignment, payloadInterpreter);
    }

    public Tex2DArrayAtlas(
            int pageWidth,
            int pageHeight,
            int initialLayerCapacity,
            @NonNull TextureFormat format,
            @NonNull FilterMode filterModeMin,
            @NonNull FilterMode filterModeMag,
            @NonNull WrapMode wrapModeS,
            @NonNull WrapMode wrapModeT,
            int unpackAlignment,
            @NonNull PayloadInterpreter payloadInterpreter) {

        this(pageWidth, pageHeight, initialLayerCapacity, false, format,
                filterModeMin, filterModeMag, wrapModeS, wrapModeT, unpackAlignment, payloadInterpreter);
    }

    public Tex2DArrayAtlas(
            int pageWidth,
            int pageHeight,
            int initialLayerCapacity,
            boolean powerOfTwoSlots,
            @NonNull TextureFormat format,
            @NonNull FilterMode filterModeMin,
            @NonNull FilterMode filterModeMag,
            @NonNull WrapMode wrapModeS,
            @NonNull WrapMode wrapModeT,
            int unpackAlignment,
            @NonNull PayloadInterpreter payloadInterpreter) {

        this(new SharedStorage(pageWidth, pageHeight, initialLayerCapacity, format, filterModeMin, filterModeMag, wrapModeS, wrapModeT, unpackAlignment),
                pageWidth, pageHeight, powerOfTwoSlots, payloadInterpreter);
    }

    private Tex2DArrayAtlas(
            @NonNull SharedStorage storage,
            int pageWidth,
            int pageHeight,
            boolean powerOfTwoSlots,
            @NonNull PayloadInterpreter payloadInterpreter) {

        super(storage::allocatePage, pageWidth, pageHeight, powerOfTwoSlots, payloadInterpreter);

        this.storage = storage;
    }

    @Override
    protected void initPage(@NonNull LayerPage page, int width, int height) {
        // NO OP
    }

    @Override
    protected void uploadSection(@NonNull SlotHandle<LayerPage> slot, @NonNull TextureData data) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(data);

        LayerPage page = slot.getPage();
        ByteBuffer pixels = payloadInterpreter.pixels(data);

        Preconditions.checkNotNull(pixels);
        Preconditions.checkArgument(pixels.isDirect(),
                "Interpreted pixels must be stored in a direct buffer.");
        Preconditions.checkState(page.getLayer() == slot.getPageIndex(),
                "Layer=%s must match page index=%s.", page.getLayer(), slot.getPageIndex());

        // potential stall; doesn't matter anyway
        int previousUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, storage.unpackAlignment);
        try {
            page.getTexture().highlevel().uploadSubImage(
                    0,
                    slot.getX(),
                    slot.getY(),
                    page.getLayer(),
                    slot.getWidth(),
                    slot.getHeight(),
                    1,
                    pixels);
        } finally {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
        }
    }
}
