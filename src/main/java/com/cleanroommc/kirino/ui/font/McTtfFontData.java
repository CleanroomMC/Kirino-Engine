package com.cleanroommc.kirino.ui.font;

import com.cleanroommc.mcttf.api.McTTF;
import com.cleanroommc.mcttf.extract.AssetSource;
import com.cleanroommc.mcttf.font.FontStyle;
import com.google.common.base.Preconditions;
import net.minecraft.client.resources.IResourcePack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;

public final class McTtfFontData {

    private static final Logger LOGGER = LogManager.getLogger("Kirino McTTF Font Data");

    private McTtfFontData() {
    }

    @NonNull
    static Path defaultCacheDirectory() {
        return Paths.get("").toAbsolutePath()
                .resolve("cache")
                .resolve("kirino")
                .resolve("mcttf_fonts");
    }

    public static byte @Nullable [] loadDefault(@NonNull IResourcePack resourcePack, @Nullable String loggerName) {
        Preconditions.checkNotNull(resourcePack);

        try (AssetSource source = new McResourcePackAssetSource(resourcePack)) {
            return load(source, defaultCacheDirectory(), "default", Preconditions.checkNotNull(McTTF.STANDARD_FONTS.get("default")), FontStyle.REGULAR, loggerName);
        } catch (Throwable failure) {
            if (loggerName == null) {
                LOGGER.error("Failed to prepare the default Minecraft font.", failure);
            } else {
                LOGGER.error("[{}] Failed to prepare the default Minecraft font.", loggerName, failure);
            }
            return null;
        }
    }

    /**
     * <p>Note: <code>source</code> will be closed automatically.</p>
     */
    public static byte @Nullable [] loadDefault(@NonNull AssetSource source, @Nullable String loggerName) {
        Preconditions.checkNotNull(source);

        try (source) {
            return load(source, defaultCacheDirectory(), "default", Preconditions.checkNotNull(McTTF.STANDARD_FONTS.get("default")), FontStyle.REGULAR, loggerName);
        } catch (Throwable failure) {
            if (loggerName == null) {
                LOGGER.error("Failed to prepare the default Minecraft font.", failure);
            } else {
                LOGGER.error("[{}] Failed to prepare the default Minecraft font.", loggerName, failure);
            }
            return null;
        }
    }

    static byte @NonNull [] load(
            @NonNull AssetSource source,
            @NonNull Path cacheDirectory,
            @NonNull String fontName,
            @NonNull String familyName,
            @NonNull FontStyle style,
            @Nullable String loggerName) throws IOException {

        String contentKey = McTtfCacheKey.create(
                source,
                fontName,
                familyName,
                style);

        byte[] bytes = readCachedFont(cacheDirectory, contentKey, loggerName);

        if (bytes == null) {
            bytes = McTTF.convertToBytes(
                    source,
                    fontName,
                    familyName,
                    style);

            if (bytes.length == 0) {
                throw new IOException("McTTF generated an empty font: " + fontName + " / " + style);
            }

            writeCachedFont(cacheDirectory, contentKey, bytes, loggerName);
        }

        if (bytes.length == 0) {
            throw new IOException("McTTF generated an empty font: " + fontName + " / " + style);
        }

        return bytes;
    }

    private static byte @Nullable [] readCachedFont(Path cacheDirectory, String contentKey, @Nullable String loggerName) {
        Path path = cacheDirectory.resolve(contentKey + ".bin");

        if (!Files.isRegularFile(path)) {
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(path);

            if (looksLikeSfnt(bytes)) {
                if (loggerName == null) {
                    LOGGER.info("Loaded Minecraft font from cache: {}", path);
                } else {
                    LOGGER.info("[{}] Loaded Minecraft font from cache: {}", loggerName, path);
                }
                return bytes;
            }

            if (loggerName == null) {
                LOGGER.warn("Ignoring invalid Minecraft font cache entry: {}", path);
            } else {
                LOGGER.warn("[{}] Ignoring invalid Minecraft font cache entry: {}", loggerName, path);
            }
        } catch (IOException e) {
            if (loggerName == null) {
                LOGGER.warn("Failed to read Minecraft font cache entry: {}", path, e);
            } else {
                LOGGER.warn("[{}] Failed to read Minecraft font cache entry: {}", loggerName, path, e);
            }
        }

        return null;
    }

    private static void writeCachedFont(Path cacheDirectory, String contentKey, byte[] bytes, @Nullable String loggerName) {
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

            if (loggerName == null) {
                LOGGER.info("Cached generated Minecraft font: {}", path);
            } else {
                LOGGER.info("[{}] Cached generated Minecraft font: {}", loggerName, path);
            }
        } catch (IOException e) {
            if (loggerName == null) {
                LOGGER.warn("Failed to write Minecraft font cache entry: {}", path, e);
            } else {
                LOGGER.warn("[{}] Failed to write Minecraft font cache entry: {}", loggerName, path, e);
            }
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
