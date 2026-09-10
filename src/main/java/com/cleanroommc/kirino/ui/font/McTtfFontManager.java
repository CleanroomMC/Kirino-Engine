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
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public final class McTtfFontManager {

    private record FontKey(@NonNull String fontName, @NonNull String familyName, @NonNull FontStyle style) {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino McTTF Font Manager");

    private final Path cacheDirectory;

    private final FreeTypeManager freeTypeManager;
    private final AssetSource resourceManagerAssetSource;
    private final AssetSource resourcePackAssetSource;
    private AssetSource assetSource;

    /**
     * The buffers must remain alive as long as any FT_Face created from them is alive.
     */
    private final Map<FontKey, ByteBuffer> fontCache = new HashMap<>();

    private boolean destroyed;

    public McTtfFontManager(
            @NonNull IResourceManager resourceManager,
            @NonNull IResourcePack resourcePack,
            @NonNull FreeTypeManager freeTypeManager) {

        this(resourceManager, resourcePack, freeTypeManager, defaultCacheDirectory());
    }

    public McTtfFontManager(
            @NonNull IResourceManager resourceManager,
            @NonNull IResourcePack resourcePack,
            @NonNull FreeTypeManager freeTypeManager,
            @NonNull Path cacheDirectory) {

        Preconditions.checkNotNull(resourceManager);
        Preconditions.checkNotNull(resourcePack);
        Preconditions.checkNotNull(freeTypeManager);
        Preconditions.checkNotNull(cacheDirectory);

        this.freeTypeManager = freeTypeManager;
        this.cacheDirectory = cacheDirectory;
        resourceManagerAssetSource = new McResourceManagerAssetSource(resourceManager);
        resourcePackAssetSource = new McResourcePackAssetSource(resourcePack);
        assetSource = resourceManagerAssetSource;
    }

    @NonNull
    private static Path defaultCacheDirectory() {
        return Paths.get("").toAbsolutePath()
                .resolve("cache")
                .resolve("kirino")
                .resolve("font");
    }

    /**
     * <p>Note: {@link #resourceManagerAssetSource} is the default asset source.</p>
     */
    public void enableResourceManagerAssetSource() {
        assetSource = resourceManagerAssetSource;
    }

    /**
     * <p>Note: {@link #resourceManagerAssetSource} is the default asset source.</p>
     */
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
            LOGGER.error(msg, e);
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
        String contentKey = McTtfCacheKey.create(
                assetSource,
                key.fontName(),
                key.familyName(),
                key.style());

        byte[] bytes = readCachedFont(contentKey);

        if (bytes == null) {
            bytes = McTTF.convertToBytes(
                    assetSource,
                    key.fontName(),
                    key.familyName(),
                    key.style());

            if (bytes.length == 0) {
                throw new IOException("McTTF generated an empty font: " + key.fontName + " / " + key.style);
            }

            writeCachedFont(contentKey, bytes);
        }

        if (bytes.length == 0) {
            throw new IOException("McTTF generated an empty font: " + key.fontName + " / " + key.style);
        }

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

    private byte @Nullable [] readCachedFont(String contentKey) {
        Path path = cacheDirectory.resolve(contentKey + ".bin");

        if (!Files.isRegularFile(path)) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(path);

            if (looksLikeSfnt(bytes)) {
                LOGGER.info("Loaded Minecraft font from cache: {}", path);
                return bytes;
            }

            LOGGER.warn("Ignoring invalid Minecraft font cache entry: {}", path);
        } catch (IOException e) {
            LOGGER.warn("Failed to read Minecraft font cache entry: {}", path, e);
        }

        return null;
    }

    private void writeCachedFont(String contentKey, byte[] bytes) {
        Path path = cacheDirectory.resolve(contentKey + ".bin");
        Path temporary = null;

        try {
            Files.createDirectories(cacheDirectory);

            temporary = Files.createTempFile(cacheDirectory, contentKey + ".", ".tmp");

            Files.write(
                    temporary,
                    bytes,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            try {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("Cached generated Minecraft font: {}", path);
        } catch (IOException e) {
            LOGGER.warn("Failed to write Minecraft font cache entry: {}", path, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean looksLikeSfnt(byte[] bytes) {
        if (bytes.length < 12) {
            return false;
        }

        int signature = (bytes[0] & 0xFF) << 24
                | (bytes[1] & 0xFF) << 16
                | (bytes[2] & 0xFF) << 8
                | (bytes[3] & 0xFF);

        return signature == 0x00010000 // TrueType
                || signature == 0x4F54544F; // OTTO
    }
}
