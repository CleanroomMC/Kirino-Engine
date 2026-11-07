package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.mcttf.api.McTTF;
import com.cleanroommc.mcttf.extract.AssetSource;
import com.cleanroommc.mcttf.font.FontStyle;
import com.google.common.base.Preconditions;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class McTtfFontManager {

    private record FontKey(@NonNull String fontName, @NonNull String familyName, @NonNull FontStyle style) {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino McTTF Font Manager");

    /**
     * To be injected by the owner before use.
     *
     * <p>When null, no instance name is added to log messages.</p>
     */
    public @Nullable String loggerName;

    private final Path cacheDirectory;

    private final FreeTypeManager freeTypeManager;
    private final @Nullable AssetSource resourceManagerAssetSource;
    private final AssetSource resourcePackAssetSource;
    private AssetSource assetSource;

    /**
     * The buffers must remain alive as long as any FT_Face created from them is alive.
     */
    private final Map<FontKey, ByteBuffer> fontCache = new HashMap<>();

    private boolean destroyed;

    public McTtfFontManager(
            @NonNull IResourcePack resourcePack,
            @NonNull FreeTypeManager freeTypeManager) {

        this(null, resourcePack, freeTypeManager, McTtfFontData.defaultCacheDirectory());
    }

    public McTtfFontManager(
            @NonNull IResourceManager resourceManager,
            @NonNull IResourcePack resourcePack,
            @NonNull FreeTypeManager freeTypeManager) {

        this(resourceManager, resourcePack, freeTypeManager, McTtfFontData.defaultCacheDirectory());
    }

    public McTtfFontManager(
            @Nullable IResourceManager resourceManager,
            @NonNull IResourcePack resourcePack,
            @NonNull FreeTypeManager freeTypeManager,
            @NonNull Path cacheDirectory) {

        Preconditions.checkNotNull(resourcePack);
        Preconditions.checkNotNull(freeTypeManager);
        Preconditions.checkNotNull(cacheDirectory);

        this.freeTypeManager = freeTypeManager;
        this.cacheDirectory = cacheDirectory;
        resourceManagerAssetSource = resourceManager == null ? null : new McResourceManagerAssetSource(resourceManager);
        resourcePackAssetSource = new McResourcePackAssetSource(resourcePack);
        assetSource = resourceManagerAssetSource != null ? resourceManagerAssetSource : resourcePackAssetSource;
    }

    public void enableResourceManagerAssetSource() {
        assetSource = Preconditions.checkNotNull(resourceManagerAssetSource,
                "No resource manager was supplied to this font manager.");
    }

    public void enableResourcePackAssetSource() {
        assetSource = resourcePackAssetSource;
    }

    /**
     * <p>Note: The returned buffer is owned by this manager and must not be
     * manually freed by the caller.</p>
     */
    @Nullable
    public ByteBuffer load(@NonNull String fontName, @NonNull FontStyle style) {
        Preconditions.checkNotNull(fontName);
        Preconditions.checkNotNull(style);

        String familyName = McTTF.STANDARD_FONTS.get(fontName);

        Preconditions.checkArgument(familyName != null,
                "Unknown standard Minecraft font: %s",
                fontName);

        return load(fontName, familyName, style);
    }

    /**
     * <p>Note: The returned buffer is owned by this manager and must not be
     * manually freed by the caller.</p>
     */
    @Nullable
    public ByteBuffer load(
            @NonNull String fontName,
            @NonNull String familyName,
            @NonNull FontStyle style) {

        Preconditions.checkState(!destroyed, "McFontManager has been destroyed.");
        Preconditions.checkNotNull(fontName);
        Preconditions.checkNotNull(familyName);
        Preconditions.checkNotNull(style);

        FontKey key = new FontKey(fontName, familyName, style);

        ByteBuffer cached = fontCache.get(key);

        if (cached != null) {
            return cached;
        }

        final ByteBuffer loaded;

        try {
            loaded = loadOrGenerate(key);
        } catch (Throwable e) {
            String msg = "Failed to load Minecraft font: " + fontName +  " / " + familyName + " / " + style;
            if (loggerName == null) {
                LOGGER.error(msg, e);
            } else {
                LOGGER.error("[{}] {}", loggerName, msg, e);
            }
            return null;
        }

        fontCache.put(key, loaded);
        return loaded;
    }

    /**
     * <p>Note: The returned buffer is owned by this manager and must not be
     * manually freed by the caller.</p>
     */
    @Nullable
    public ByteBuffer loadDefault() {
        return load("default", FontStyle.REGULAR);
    }

    /**
     * <p>Note: The returned buffer is owned by this manager and must not be
     * manually freed by the caller.</p>
     */
    @Nullable
    public ByteBuffer loadDefault(@NonNull FontStyle style) {
        return load("default", style);
    }

    @Nullable
    public FT_Face loadFace(
            @NonNull String fontName,
            @NonNull FontStyle style,
            long faceIndex,
            int pixelSize) {

        ByteBuffer buffer = load(fontName, style);
        if (buffer == null) {
            return null;
        }

        return freeTypeManager.load(buffer, faceIndex, pixelSize);
    }

    @Nullable
    public FT_Face loadDefaultFace(int pixelSize) {
        return loadFace("default", FontStyle.REGULAR, 0, pixelSize);
    }

    /**
     * <p>Note: Any FreeType faces backed by this memory are destroyed,
     * so does the native memory.</p>
     */
    public void unload(
            @NonNull String fontName,
            @NonNull FontStyle style) {

        Preconditions.checkNotNull(fontName);
        Preconditions.checkNotNull(style);

        String familyName = McTTF.STANDARD_FONTS.get(fontName);

        Preconditions.checkArgument(familyName != null,
                "Unknown standard Minecraft font: %s",
                fontName);

        unload(fontName, familyName, style);
    }

    /**
     * <p>Note: Any FreeType faces backed by this memory are destroyed,
     * so does the native memory.</p>
     */
    public void unload(
            @NonNull String fontName,
            @NonNull String familyName,
            @NonNull FontStyle style) {

        Preconditions.checkNotNull(fontName);
        Preconditions.checkNotNull(familyName);
        Preconditions.checkNotNull(style);

        FontKey key = new FontKey(fontName, familyName, style);

        ByteBuffer buffer = fontCache.remove(key);

        if (buffer == null) {
            return;
        }

        freeTypeManager.unload(buffer);
        MemoryUtil.memFree(buffer);
    }

    private void unloadAll() {
        for (ByteBuffer buffer : fontCache.values()) {
            freeTypeManager.unload(buffer);
            MemoryUtil.memFree(buffer);
        }

        fontCache.clear();
    }

    /**
     * It invalidates all in-memory Minecraft fonts.
     * The persistent content cache is retained.
     */
    public void invalidate() {
        Preconditions.checkState(!destroyed, "McFontManager has been destroyed.");

        unloadAll();
    }

    public void destroy() {
        Preconditions.checkState(!destroyed, "McFontManager has already been destroyed.");

        unloadAll();

        try {
            assetSource.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close Minecraft asset source.", e);
        }

        destroyed = true;
    }

    @NonNull
    private ByteBuffer loadOrGenerate(@NonNull FontKey key) throws IOException {
        byte[] bytes = McTtfFontData.load(
                assetSource,
                cacheDirectory,
                key.fontName(),
                key.familyName(),
                key.style(),
                loggerName);

        ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);

        try {
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (RuntimeException | Error e) {
            MemoryUtil.memFree(buffer);
            throw e;
        }
    }
}
