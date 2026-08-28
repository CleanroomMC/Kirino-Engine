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
import java.util.Iterator;
import java.util.Map;

public final class FreeTypeManager {

    private record MemoryFaceKey(long address, int size, long faceIndex, int pixelSize) {
    }

    private FreeTypeManager() {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino FreeTypeManager");

    public static final int DEFAULT_PIXEL_SIZE = 32;

    private long library;
    private boolean initialized;
    private boolean destroyed;

    /**
     * <p>Note: The underlying ByteBuffer is not owned by this cache.</p>
     */
    private final Map<MemoryFaceKey, FT_Face> faceCache = new HashMap<>();

    /**
     * <p>Note: Unlike buffers passed to <code>load(ByteBuffer)</code>,
     * these buffers are owned by FreeTypeManager.</p>
     */
    private final Map<ResourceLocation, ByteBuffer> fontDataCache = new HashMap<>();

    /**
     * Can be called multiple times without crashing.
     * Later calls return directly.
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
    public FT_Face load(@NonNull ResourceLocation rl) {
        return load(rl, 0, DEFAULT_PIXEL_SIZE);
    }

    @NonNull
    public FT_Face load(@NonNull ResourceLocation rl, long faceIndex, int pixelSize) {
        Preconditions.checkState(initialized, "Must be initialized.");
        Preconditions.checkNotNull(rl);

        ByteBuffer fontBuffer = fontDataCache.computeIfAbsent(rl, FreeTypeManager::loadResource);

        return load(fontBuffer, faceIndex, pixelSize);
    }

    /**
     * Creates a FreeType face backed by an externally-owned native buffer (from LWJGL).
     *
     * <p>Note: The buffer must remain allocated until <code>unload(buffer)</code>, <code>unloadAll()</code>,
     * or <code>destroy()</code> has destroyed every face using it.</p>
     * <p>Note: This manager doesn't free the supplied buffer.</p>
     *
     * @param fontBuffer Buffer must be from LWJGL {@link MemoryUtil},
     *                   and the whole buffer is only used for the font data.
     */
    @NonNull
    public FT_Face load(@NonNull ByteBuffer fontBuffer) {
        return load(fontBuffer, 0, DEFAULT_PIXEL_SIZE);
    }

    /**
     * Creates a FreeType face backed by an externally-owned native buffer (from LWJGL).
     *
     * <p>Note: The buffer must remain allocated until <code>unload(buffer)</code>, <code>unloadAll()</code>,
     * or <code>destroy()</code> has destroyed every face using it.</p>
     * <p>Note: This manager doesn't free the supplied buffer.</p>
     *
     * @param fontBuffer Buffer must be from LWJGL {@link MemoryUtil},
     *                   and the whole buffer is only used for the font data.
     */
    @NonNull
    public FT_Face load(@NonNull ByteBuffer fontBuffer, long faceIndex, int pixelSize) {
        Preconditions.checkState(initialized, "Must be initialized.");
        Preconditions.checkNotNull(fontBuffer);
        Preconditions.checkArgument(fontBuffer.isDirect(),
                "Font buffer must be a direct ByteBuffer.");
        Preconditions.checkArgument(fontBuffer.capacity() > 0,
                "Font buffer must not be empty.");
        Preconditions.checkArgument(faceIndex >= 0,
                "Argument \"faceIndex\" must be >= 0.");
        Preconditions.checkArgument(pixelSize > 0,
                "Argument \"pixelSize\" must be > 0.");

        ByteBuffer view = fullView(fontBuffer);

        long address = MemoryUtil.memAddress(view);
        int size = view.remaining();

        MemoryFaceKey key = new MemoryFaceKey(address, size, faceIndex, pixelSize);

        FT_Face cached = faceCache.get(key);

        if (cached != null) {
            return cached;
        }

        FT_Face face = createFace(view, faceIndex, pixelSize);

        faceCache.put(key, face);

        return face;
    }

    /**
     * It destroys every FT_Face backed by the supplied native memory.
     */
    public void unload(@NonNull ByteBuffer fontBuffer) {
        Preconditions.checkNotNull(fontBuffer);

        if (!fontBuffer.isDirect()) {
            return;
        }

        ByteBuffer view = fullView(fontBuffer);

        long address = MemoryUtil.memAddress(view);

        Iterator<Map.Entry<MemoryFaceKey, FT_Face>> iterator = faceCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<MemoryFaceKey, FT_Face> entry = iterator.next();
            if (entry.getKey().address() != address) {
                continue;
            }

            FreeType.FT_Done_Face(entry.getValue());
            iterator.remove();
        }
    }

    /**
     * It destroys all faces backed by the resource and releases
     * the native font memory owned by this manager.
     */
    public void unload(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        ByteBuffer buffer = fontDataCache.remove(rl);

        if (buffer == null) {
            return;
        }

        unload(buffer);
        MemoryUtil.memFree(buffer);
    }

    /**
     * It destroys all currently created FT_Faces.
     *
     * <p>Note: External ByteBuffers remain owned by their callers.</p>
     */
    public void unloadAll() {
        for (FT_Face face : faceCache.values()) {
            FreeType.FT_Done_Face(face);
        }
        faceCache.clear();

        for (ByteBuffer buffer : fontDataCache.values()) {
            MemoryUtil.memFree(buffer);
        }
        fontDataCache.clear();
    }

    /**
     * No more access is allowed after this call.
     */
    public void destroy() {
        Preconditions.checkState(initialized, "Must be initialized.");

        unloadAll();

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
    private FT_Face createFace(@NonNull ByteBuffer fontBuffer, long faceIndex, int pixelSize) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);

            int error = FreeType.FT_New_Memory_Face(
                    library,
                    fontBuffer,
                    faceIndex,
                    pointer);

            if (error != FreeType.FT_Err_Ok) {
                throw new RuntimeException(String.format("Failed to create face (address=0x%s, faceIndex=%d): %s",
                        Long.toHexString(MemoryUtil.memAddress(fontBuffer)),
                        faceIndex,
                        FreeType.FT_Error_String(error)));
            }

            FT_Face face = FT_Face.create(pointer.get(0));

            error = FreeType.FT_Set_Pixel_Sizes(face, 0, pixelSize);

            if (error != FreeType.FT_Err_Ok) {
                FreeType.FT_Done_Face(face);
                throw new RuntimeException(String.format("Failed to set pixel size (faceIndex=%d, pixelSize=%d): %s",
                        faceIndex,
                        pixelSize,
                        FreeType.FT_Error_String(error)));
            }

            return face;
        }
    }

    @NonNull
    private static ByteBuffer fullView(@NonNull ByteBuffer buffer) {
        ByteBuffer view = buffer.duplicate();
        view.clear();
        return view;
    }

    @NonNull
    private static ByteBuffer loadResource(@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        try (var input = MinecraftResourceUtils.getInputStream(rl)) {
            byte[] bytes = input.readAllBytes();

            Preconditions.checkState(bytes.length > 0,
                    "Empty font resource: %s", rl);

            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.flip();

            return buffer;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load font resource: " + rl, e);
        }
    }
}
