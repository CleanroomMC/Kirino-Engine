package com.cleanroommc.kirino.simpletext;

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

public class FreeTypeManager {

    public static final Logger LOGGER = LogManager.getLogger("FreeTypeManager");

    public static final int DEFAULT_PIXEL_SIZE = 32;

    private long library = 0;
    private boolean initialized = false;
    private boolean destroyed = false;

    private final Map<String, FT_Face> FACE_CACHE = new HashMap<>();
    private final Map<ResourceLocation, ByteBuffer> FONT_DATA_CACHE = new HashMap<>();

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

            long library = pointer.get(0);

            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            IntBuffer patch = stack.mallocInt(1);

            FreeType.FT_Library_Version(library, major, minor, patch);
            LOGGER.info("Loaded FreeType " + major.get(0) + "." + minor.get(0) + "." + patch.get(0));

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

        if (FACE_CACHE.containsKey(key)) {
            return FACE_CACHE.get(key);
        }

        ByteBuffer fontBuffer = FONT_DATA_CACHE.computeIfAbsent(rl, FreeTypeManager::loadResource);

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

            FACE_CACHE.put(key, face);

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

        for (FT_Face face : FACE_CACHE.values()) {
            FreeType.FT_Done_Face(face);
        }
        FACE_CACHE.clear();

        for (ByteBuffer buf : FONT_DATA_CACHE.values()) {
            MemoryUtil.memFree(buf);
        }
        FONT_DATA_CACHE.clear();

        if (library != 0) {
            FreeType.FT_Done_FreeType(library);
            library = 0;
        }

        initialized = false;
        destroyed = true;
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
