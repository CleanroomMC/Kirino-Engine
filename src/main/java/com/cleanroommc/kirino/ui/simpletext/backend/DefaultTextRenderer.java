package com.cleanroommc.kirino.ui.simpletext.backend;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.InterpretationType;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.cleanroommc.kirino.ui.simpletext.ST_Bitmap;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextConsumer;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.AbstractPagedAtlas;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGenerator;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGeneratorBruteForceImpl;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGeneratorPool;
import com.cleanroommc.kirino.utils.ForkJoinPoolUtils;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class DefaultTextRenderer implements SimpleTextConsumer, AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger("Kirino SimpleText DefaultTextRenderer");

    private static final int INSTANCE_STRIDE = 48;
    private static final int ATLAS_TEXTURE_UNIT_INDEX = 6;
    private static final int ATLAS_TEXTURE_UNIT = GL13.GL_TEXTURE0 + ATLAS_TEXTURE_UNIT_INDEX;

    private static final AttributeLayout ATTRIBUTE_LAYOUT = createAttributeLayout();

    private static AttributeLayout createAttributeLayout() {
        AttributeLayout layout = new AttributeLayout();
        layout.push(new Stride(INSTANCE_STRIDE)
                .push(new Slot(Type.FLOAT, 4).setDivisor(1)) // uv 16
                .push(new Slot(Type.FLOAT, 4).setDivisor(1)) // rect 16
                .push(new Slot(Type.FLOAT, 1).setDivisor(1)) // size 4
                .push(new Slot(Type.INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND)) // color 4
                .push(new Slot(Type.UNSIGNED_INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND)) // page 4
                .push(new Slot(Type.INT, 1).setDivisor(1).setInterpretationType(InterpretationType.TO_INT_KIND))); // hint 4
        return layout;
    }

    private record GlyphEntry(AbstractPagedAtlas.@NonNull SlotHandle<Tex2DArrayGlyphAtlas.LayerPage> slot) {

        private GlyphEntry {
            Preconditions.checkNotNull(slot);
        }
    }

    private record GlyphBuildResult(int glyphIndex, @Nullable SDFBitmap bitmap, @Nullable Throwable failure) {

        @NonNull
        private static GlyphBuildResult success(int glyphIndex, @NonNull SDFBitmap bitmap) {
            Preconditions.checkNotNull(bitmap);

            return new GlyphBuildResult(
                    glyphIndex,
                    bitmap,
                    null);
        }

        @NonNull
        private static GlyphBuildResult failure(int glyphIndex, @NonNull Throwable failure) {
            Preconditions.checkNotNull(failure);

            return new GlyphBuildResult(
                    glyphIndex,
                    null,
                    failure);
        }
    }

    private final SimpleTextRuntime context;
    private final Tex2DArrayGlyphAtlas glyphAtlas;

    private final int sdfPadding;
    private final int sdfSpread;
    private final int maxResidentGlyphs;

    // key: glyph index
    private final LinkedHashMap<Integer, GlyphEntry> glyphCache = new LinkedHashMap<>(128, 0.75f, true);

    private final Set<Integer> failedGlyphHistory = new HashSet<>();
    private final Set<Integer> emptyGlyphHistory = new HashSet<>();

    private final ForkJoinPool workerPool;
    private final SDFGeneratorPool generatorPool;

    private final VBOView instanceVbo;
    private final VAO vao;
    private final ShaderProgram program;

    private final int scaledResLoc;
    private final int tickLoc;

    private boolean closed;

    /**
     * It allocates worker pool for itself. Must call {@link #close()} in the end.
     */
    public DefaultTextRenderer(
            @NonNull SimpleTextRuntime context,
            @NonNull Tex2DArrayGlyphAtlas glyphAtlas,
            @NonNull ImmediateShaderAccess shaderAccess,
            int maxResidentGlyphs) {

        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(glyphAtlas);

        this.context = context;
        this.glyphAtlas = glyphAtlas;

        Preconditions.checkNotNull(shaderAccess);
        Preconditions.checkArgument(maxResidentGlyphs > 0,
                "Argument \"maxResidentGlyphs\" must be positive.");

        sdfPadding = context.getConfig().sdfPadding();
        sdfSpread = context.getConfig().sdfSpread();
        this.maxResidentGlyphs = maxResidentGlyphs;

        Preconditions.checkState(sdfPadding > 0,
                "SDF padding=%s must be positive.", sdfPadding);
        Preconditions.checkState(sdfSpread > 0,
                "SDF spread=%s must be positive.", sdfSpread);
        Preconditions.checkState(sdfPadding >= sdfSpread,
                "SDF padding=%s must be >= spread=%s.",
                sdfPadding, sdfSpread);

        instanceVbo = new VBOView(new GLBuffer());
        vao = new VAO(ATTRIBUTE_LAYOUT, null, instanceVbo);

        Shader vert = shaderAccess.makeShader(new ResourceLocation("kirino:shaders/simpletext_font.vert"));
        Shader frag = shaderAccess.makeShader(new ResourceLocation("kirino:shaders/simpletext_font.frag"));

        shaderAccess.submitToGL(vert, frag);
        program = shaderAccess.makeProgram(vert, frag);

        scaledResLoc = GL20.glGetUniformLocation(program.getProgramID(), "scaledRes");
        int atlasLoc = GL20.glGetUniformLocation(program.getProgramID(), "atlas");
        tickLoc = GL20.glGetUniformLocation(program.getProgramID(), "tick");
        int sdfSpreadLoc = GL20.glGetUniformLocation(program.getProgramID(), "sdfSpread");

        Preconditions.checkState(scaledResLoc >= 0);
        Preconditions.checkState(atlasLoc >= 0);
        // "tick" could be optimized => tickLoc == -1
        // "sdfSpread" could be optimized => sdfSpreadLoc == -1

        program.use();
        GL20.glUniform1i(atlasLoc, ATLAS_TEXTURE_UNIT_INDEX);
        if (sdfSpreadLoc >= 0) {
            GL30.glUniform1ui(sdfSpreadLoc, sdfSpread);
        }
        program.use0();

        int[] outParallelism = new int[1];
        workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
        generatorPool = new SDFGeneratorPool(outParallelism[0], () -> new SDFGeneratorBruteForceImpl(sdfPadding, sdfSpread));
    }

    //<editor-fold desc="LRU related utils">
    private void makeRoomForInsertion(@NonNull Set<Integer> pinnedGlyphs) {
        while (glyphCache.size() >= maxResidentGlyphs) {
            if (!evictOne(pinnedGlyphs)) {
                return;
            }
        }
    }

    private void trimCacheToCapacity(@NonNull Set<Integer> pinnedGlyphs) {
        while (glyphCache.size() > maxResidentGlyphs) {
            if (!evictOne(pinnedGlyphs)) {
                return;
            }
        }
    }

    private boolean evictOne(@NonNull Set<Integer> pinnedGlyphs) {
        Iterator<Map.Entry<Integer, GlyphEntry>> iterator = glyphCache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, GlyphEntry> candidate = iterator.next();

            if (pinnedGlyphs.contains(candidate.getKey())) {
                continue;
            }

            iterator.remove();
            candidate.getValue().slot.release();

            return true;
        }

        return false;
    }
    //</editor-fold>

    //<editor-fold desc="glyph related utils">
    private void markGlyphFailure(int glyphIndex, @NonNull String message, @Nullable Throwable throwable) {
        if (!failedGlyphHistory.add(glyphIndex)) {
            return;
        }

        if (throwable == null) {
            LOGGER.warn("{} Glyph index={}", message, glyphIndex);
        } else {
            final String out = message + " Glyph index=" + glyphIndex;
            LOGGER.warn(out, throwable);
        }
    }

    private static boolean isUsableBitmap(@Nullable ST_Bitmap bitmap) {
        return bitmap != null &&
                bitmap.width() > 0 &&
                bitmap.height() > 0 &&
                bitmap.byteBuffer() != null;
    }

    private boolean canFitGeneratedBitmap(@NonNull ST_Bitmap source) {
        int paddingBothSides = sdfPadding * 2;
        int outputWidth = source.width() + paddingBothSides;
        int outputHeight = source.height() + paddingBothSides;
        return outputWidth <= glyphAtlas.getPageWidth() && outputHeight <= glyphAtlas.getPageHeight();
    }

    private static void safeClose(@Nullable ST_Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        try {
            bitmap.close();
        } catch (Throwable t) {
            LOGGER.error("Failed to close SimpleText bitmap.", t);
        }
    }
    //</editor-fold>

    @Override
    public void consume(@NonNull TextCommandList commandList) {
        Preconditions.checkNotNull(commandList);
        Preconditions.checkState(!closed, "DefaultTextRenderer has already been closed.");

        if (commandList.size() == 0) {
            return;
        }

        // only generate non-zero and usable glyphs
        generateMissingGlyphs(collectFrameGlyphs(commandList));

        // upload everything including zero glyphs, empty glyphs, failed glyphs
        int instanceCount = uploadInstances(commandList);
        if (instanceCount > 0) {
            draw(instanceCount);
        }

        trimCacheToCapacity(Collections.emptySet());
    }

    @NonNull
    private LinkedHashSet<Integer> collectFrameGlyphs(@NonNull TextCommandList commandList) {
        LinkedHashSet<Integer> glyphs = new LinkedHashSet<>();

        for (int i = 0; i < commandList.size(); i++) {
            int glyph = commandList.glyphIndex(i);
            if (glyph != 0) {
                glyphs.add(glyph);
            }
        }

        return glyphs;
    }

    private void generateMissingGlyphs(@NonNull LinkedHashSet<Integer> frameGlyphs) {
        LinkedHashMap<Integer, CompletableFuture<GlyphBuildResult>> jobs = new LinkedHashMap<>();

        for (int glyphIndex : frameGlyphs) {
            if (glyphCache.containsKey(glyphIndex)
                    || failedGlyphHistory.contains(glyphIndex)
                    || emptyGlyphHistory.contains(glyphIndex)) {
                continue;
            }

            ST_Bitmap source;

            try {
                source = context.getFont().loadGlyph(glyphIndex, context.getConfig().payload(), null);
            } catch (Throwable t) {
                markGlyphFailure(glyphIndex, "Glyph bitmap loading threw an exception.", t);
                continue;
            }

            if (source == ST_Bitmap.EMPTY) {
                safeClose(source);
                emptyGlyphHistory.add(glyphIndex);
                continue;
            }

            if (!isUsableBitmap(source)) {
                safeClose(source);
                markGlyphFailure(glyphIndex, "Glyph bitmap loading returned no usable bitmap.", null);
                continue;
            }

            Preconditions.checkNotNull(source);

            if (!canFitGeneratedBitmap(source)) {
                safeClose(source);
                markGlyphFailure(glyphIndex, "Generated SDF would exceed the atlas page size.", null);
                continue;
            }

            // finally the usable bitmap
            final ST_Bitmap sourceBitmap = source;

            CompletableFuture<GlyphBuildResult> future = null;
            try {
                future = CompletableFuture.supplyAsync(() -> buildGlyphSdf(glyphIndex, sourceBitmap), workerPool);
                jobs.put(glyphIndex, future);
            } finally {
                if (future == null) {
                    safeClose(sourceBitmap);
                }
            }
        }

        for (Map.Entry<Integer, CompletableFuture<GlyphBuildResult>> entry : jobs.entrySet()) {
            GlyphBuildResult result = entry.getValue().join();

            if (result.failure != null) {
                markGlyphFailure(result.glyphIndex, "SDF generation failed.", result.failure);
                continue;
            }

            try (SDFBitmap sdfBitmap = Preconditions.checkNotNull(result.bitmap)) {
                makeRoomForInsertion(frameGlyphs);

                AbstractPagedAtlas.SlotHandle<Tex2DArrayGlyphAtlas.LayerPage> slot = glyphAtlas.allocate(sdfBitmap);
                glyphCache.put(result.glyphIndex, new GlyphEntry(slot));
            }
        }
    }

    private int uploadInstances(@NonNull TextCommandList commandList) {
        int maximumBufferSize = Math.multiplyExact(INSTANCE_STRIDE, commandList.size());
        ByteBuffer buffer = MemoryUtil.memAlloc(maximumBufferSize);

        int instanceCount = 0;

        try {
            for (int i = 0; i < commandList.size(); i++) {
                int glyph = commandList.glyphIndex(i);
                GlyphEntry entry = null;
                if (glyph != 0) {
                    // LinkedHashMap#get updates access order
                    entry = glyphCache.get(glyph);
                }

                if (glyph == 0 || entry == null) {
                    if (glyph == 0) {
                        // hint: 32nd bit
                        buffer
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(commandList.x(i))
                                .putFloat(commandList.y(i))
                                .putFloat(commandList.width(i))
                                .putFloat(commandList.height(i))
                                .putFloat(commandList.size(i))
                                .putInt(commandList.color(i))
                                .putInt(0)
                                .putInt(commandList.hint(i) | (1 << 31));
                        instanceCount++;
                    } else if (emptyGlyphHistory.contains(glyph)) {
                        // hint: 31st bit
                        buffer
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(commandList.x(i))
                                .putFloat(commandList.y(i))
                                .putFloat(commandList.width(i))
                                .putFloat(commandList.height(i))
                                .putFloat(commandList.size(i))
                                .putInt(commandList.color(i))
                                .putInt(0)
                                .putInt(commandList.hint(i) | (1 << 30));
                        instanceCount++;
                    } else if (failedGlyphHistory.contains(glyph)) {
                        // hint: 30th bit
                        buffer
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(0)
                                .putFloat(commandList.x(i))
                                .putFloat(commandList.y(i))
                                .putFloat(commandList.width(i))
                                .putFloat(commandList.height(i))
                                .putFloat(commandList.size(i))
                                .putInt(commandList.color(i))
                                .putInt(0)
                                .putInt(commandList.hint(i) | (1 << 29));
                        instanceCount++;
                    }
                } else {
                    AbstractPagedAtlas.SlotHandle<Tex2DArrayGlyphAtlas.LayerPage> handle = entry.slot;
                    Preconditions.checkState(!handle.isReleased(),
                            "Cached glyph=%s points to a released slot.", glyph);

                    int layer = handle.getPage().getLayer();

                    buffer
                            .putFloat(handle.u0(glyphAtlas.getPageWidth(), 0))
                            .putFloat(handle.v0(glyphAtlas.getPageHeight(), 0))
                            .putFloat(handle.u1(glyphAtlas.getPageWidth(), 0))
                            .putFloat(handle.v1(glyphAtlas.getPageHeight(), 0))
                            .putFloat(commandList.x(i))
                            .putFloat(commandList.y(i))
                            .putFloat(commandList.width(i))
                            .putFloat(commandList.height(i))
                            .putFloat(commandList.size(i))
                            .putInt(commandList.color(i))
                            .putInt(layer)
                            .putInt(commandList.hint(i));

                    instanceCount++;
                }
            }

            if (instanceCount == 0) {
                return 0;
            }

            buffer.flip();

            instanceVbo.bind();
            // orphaning
            instanceVbo.alloc(buffer.remaining(), BufferUploadHint.STREAM_DRAW);
            instanceVbo.uploadBySubData(0, buffer);
            instanceVbo.bind(0);

            return instanceCount;
        } finally {
            MemoryUtil.memFree(buffer);
        }
    }

    @NonNull
    private GlyphBuildResult buildGlyphSdf(int glyphIndex, @NonNull ST_Bitmap source) {
        SDFGenerator generator = null;
        SDFBitmap output = null;

        try (ST_Bitmap ignored = source) {
            generator = generatorPool.acquire();
            output = Preconditions.checkNotNull(generator.compute(source));

            return GlyphBuildResult.success(glyphIndex, output);
        } catch (Throwable t) {
            safeClose(output);
            return GlyphBuildResult.failure(glyphIndex, t);
        } finally {
            if (generator != null) {
                generatorPool.release(generator);
            }
        }
    }

    private void draw(int instanceCount) {
        Preconditions.checkArgument(instanceCount > 0);

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        float screenWidth = (float) resolution.getScaledWidth_double();
        float screenHeight = (float) resolution.getScaledHeight_double();

        program.use();
        GL20.glUniform2f(scaledResLoc, screenWidth, screenHeight);
        if (tickLoc >= 0) {
            GL30.glUniform1ui(tickLoc, (int) (System.nanoTime() / 80_000_000L));
        }

        GL13.glActiveTexture(ATLAS_TEXTURE_UNIT);
        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, glyphAtlas.getTexture().textureID());

        vao.bind();
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, 4, instanceCount);
        VAO.bind(0);

        GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        program.use0();
    }

    /**
     * <p>Note: No need to run <code>close</code> on the GL thread, but must not
     * run it concurrently with {@link #consume(TextCommandList)}.</p>
     * <p>Note: The atlas itself is externally owned and is not disposed here.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        ForkJoinPoolUtils.shutdownPool(workerPool, 5);

        for (GlyphEntry entry : glyphCache.values()) {
            entry.slot.release();
        }

        glyphCache.clear();
        failedGlyphHistory.clear();
        emptyGlyphHistory.clear();
    }
}
