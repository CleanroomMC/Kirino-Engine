package com.cleanroommc.kirino.ui.simpletext.backend.freetype;

import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public final class FreeTypeManager {

    private FreeTypeManager() {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino FreeTypeManager");

    public static final int DEFAULT_PIXEL_SIZE = 32;

    private long library = 0;
    private boolean initialized = false;
    private boolean destroyed = false;

    private final Map<String, FT_Face> faceCache = new HashMap<>();
    private final Map<ResourceLocation, ByteBuffer> fontDataCache = new HashMap<>();

    /**
     * Can be called multiple times without crashing. Later calls will return directly.
     */
    public void init() {
        Preconditions.checkState(!destroyed,
                "Not allowed to re-init after the destroy call.");

        if (library != 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);

            int error = FreeType.FT_Init_FreeType(pointer);
            if (error != FreeType.FT_Err_Ok) {
                throw new IllegalStateException("Failed to initialize FreeType: " + FreeType.FT_Error_String(error));
            }

            library = pointer.get(0);

            Preconditions.checkState(library != 0, "FreeType library pointer must not be 0.");

            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            IntBuffer patch = stack.mallocInt(1);

            FreeType.FT_Library_Version(library, major, minor, patch);
            LOGGER.info("Loaded FreeType {}.{}.{} Lib Pointer: 0x{}",
                    major.get(0),
                    minor.get(0),
                    patch.get(0),
                    Long.toHexString(library));

            initialized = true;
        }
    }

    @NonNull
    public FT_Face load(ResourceLocation rl) {
        Preconditions.checkState(initialized, "Must be initialized.");

        return load(rl, 0, DEFAULT_PIXEL_SIZE);
    }

    @NonNull
    public FT_Face load(ResourceLocation rl, long faceIndex, int pixelSize) {
        Preconditions.checkState(initialized, "Must be initialized.");

        String key = rl.toString() + "#" + faceIndex + "#" + pixelSize;

        if (faceCache.containsKey(key)) {
            return faceCache.get(key);
        }

        ByteBuffer fontBuffer = fontDataCache.computeIfAbsent(rl, FreeTypeManager::loadResource);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);

            int error = FreeType.FT_New_Memory_Face(
                    library,
                    fontBuffer,
                    faceIndex,
                    pointer);

            if (error != FreeType.FT_Err_Ok) {
                throw new RuntimeException(String.format("Failed to create face (rl=%s, faceIndex=%d): %d",
                        rl.toString(),
                        faceIndex,
                        error));
            }

            FT_Face face = FT_Face.create(pointer.get(0));

            error = FreeType.FT_Set_Pixel_Sizes(face, 0, pixelSize);

            if (error != FreeType.FT_Err_Ok) {
                throw new RuntimeException(String.format("Failed to set pixel size (rl=%s, faceIndex=%d, pixelSize=%d): %d",
                        rl.toString(),
                        faceIndex,
                        pixelSize,
                        error));
            }

            faceCache.put(key, face);

            return face;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * No more access is allowed after the destroy call. Must be placed at the end.
     *
     * <p><b>Note</b>: Must not be accessed by clients!</p>
     */
    public void destroy() {
        Preconditions.checkState(initialized, "Must be initialized.");

        for (FT_Face face : faceCache.values()) {
            FreeType.FT_Done_Face(face);
        }
        faceCache.clear();

        for (ByteBuffer buf : fontDataCache.values()) {
            MemoryUtil.memFree(buf);
        }
        fontDataCache.clear();

        long lib = library;

        if (library != 0) {
            FreeType.FT_Done_FreeType(library);
            library = 0;
        }

        initialized = false;
        destroyed = true;

        LOGGER.info("Destroyed FreeType Lib 0x{}.", Long.toHexString(lib));
    }

    @NonNull
    private static ByteBuffer loadResource(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        try {
            byte[] bytes = MinecraftResourceUtils.getInputStream(rl).readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            return buffer;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
