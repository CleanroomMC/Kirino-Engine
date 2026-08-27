package com.cleanroommc.kirino.ui.simpletext.atlas;

import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DArrayAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

public class Tex2DArrayGlyphAtlas extends AbstractPagedAtlas<Tex2DArrayGlyphAtlas.LayerPage, SDFBitmap> {

    private static final int DEFAULT_INITIAL_LAYER_CAPACITY = 4;

    @Override
    public void close() {
        GLResourceManager.disposeEarly(storage.texture.texture);
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

        private Texture2DArrayAccessor texture;

        private int layerCapacity;
        private int allocatedPageCount = 0;

        private SharedStorage(int pageWidth, int pageHeight, int initialLayerCapacity) {
            Preconditions.checkArgument(pageWidth > 0,
                    "Argument \"pageWidth\" must be positive.");
            Preconditions.checkArgument(pageHeight > 0,
                    "Argument \"pageHeight\" must be positive.");
            Preconditions.checkArgument(initialLayerCapacity > 0,
                    "Argument \"initialLayerCapacity\" must be positive.");

            this.pageWidth = pageWidth;
            this.pageHeight = pageHeight;
            // todo: move to gl device info
            maxLayerCount = GL11.glGetInteger(GL30.GL_MAX_ARRAY_TEXTURE_LAYERS);

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

            GLResourceManager.disposeEarly(oldTexture.texture);
        }

        @NonNull
        private Texture2DArrayAccessor createTexture(int layers) {
            Texture2DArrayAccessor result = new Texture2DArrayAccessor(true, GLTexture.newDsaTex2DArray(
                    pageWidth,
                    pageHeight,
                    layers));

            result.highlevel().allocEmpty(false, TextureFormat.R8_UNORM);
            result.clearTexImage(0, TextureFormat.R8_UNORM.format, TextureFormat.R8_UNORM.type, null);
            result.setCommonParams(FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);

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

    public Tex2DArrayGlyphAtlas(int pageWidth, int pageHeight) {
        this(pageWidth, pageHeight, DEFAULT_INITIAL_LAYER_CAPACITY);
    }

    public Tex2DArrayGlyphAtlas(int pageWidth, int pageHeight, int initialLayerCapacity) {
        this(new SharedStorage(pageWidth, pageHeight, initialLayerCapacity), pageWidth, pageHeight);
    }

    private Tex2DArrayGlyphAtlas(@NonNull SharedStorage storage, int pageWidth, int pageHeight) {
        super(storage::allocatePage, pageWidth, pageHeight, true);

        this.storage = storage;
    }

    @Override
    void initPage(@NonNull LayerPage page, int width, int height) {
        // NO OP
    }

    @Override
    void uploadSection(@NonNull SlotHandle<LayerPage> slot, @NonNull SDFBitmap bitmap) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(bitmap);
        Preconditions.checkArgument(slot.getWidth() == bitmap.width(),
                "Slot width=%s must match bitmap width=%s.", slot.getWidth(), bitmap.width());
        Preconditions.checkArgument(slot.getHeight() == bitmap.height(),
                "Slot height=%s must match bitmap height=%s.", slot.getHeight(), bitmap.height());

        LayerPage page = slot.getPage();

        Preconditions.checkState(page.getLayer() == slot.getPageIndex(),
                "Layer=%s must match page index=%s.", page.getLayer(), slot.getPageIndex());

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        page.getTexture().texSubImage3D(
                0,
                slot.getX(),
                slot.getY(),
                page.getLayer(),
                slot.getWidth(),
                slot.getHeight(),
                1,
                TextureFormat.R8_UNORM.format,
                TextureFormat.R8_UNORM.type,
                bitmap.byteBuffer());
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
    }
}
