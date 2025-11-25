package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.buffer.BufferStorage;
import com.cleanroommc.kirino.engine.render.staging.handle.*;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <code>StagingBufferManager</code> is where we upload data to GPU. {@link #runStaging(IStagingCallback)} to be exact.
 * <code>StagingBufferManager</code> guarantees that it uploads to memory slices where GPU is no longer reading by introducing a window period (instead of a ring buffer).
 * To illustrate, clients are only allowed to access <code>StagingBufferManager</code> and upload data during {@link #runStaging(IStagingCallback)}.
 */
public class StagingBufferManager {
    private final Map<AttributeLayout, Map<Long, VAO>> persistentVaos = new HashMap<>();
    private final Map<String, BufferStorage<VBOView>> persistentVbos = new HashMap<>();
    private final Map<String, BufferStorage<EBOView>> persistentEbos = new HashMap<>();

    private final List<VAO> temporaryVaos = new ArrayList<>();
    private final List<VBOView> temporaryVbos = new ArrayList<>();
    private final List<EBOView> temporaryEbos = new ArrayList<>();

    private long handleGeneration = 0;

    public long getHandleGeneration() {
        return handleGeneration;
    }

    //<editor-fold desc="staging">
    protected boolean active = false;

    private void beginStaging() {
        // avoid disposing buffers being used
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        handleGeneration++;
        for (VAO vao : temporaryVaos) {
            GLResourceManager.disposeEarly(vao);
        }
        temporaryVaos.clear();
        for (VBOView vboView : temporaryVbos) {
            GLResourceManager.disposeEarly(vboView.buffer);
        }
        temporaryVbos.clear();
        for (EBOView eboView : temporaryEbos) {
            GLResourceManager.disposeEarly(eboView.buffer);
        }
        temporaryEbos.clear();
        active = true;
    }

    private void endStaging() {
        active = false;
    }

    private final StagingContext stagingContext = new StagingContext();

    public void runStaging(IStagingCallback callback) {
        beginStaging();
        stagingContext.manager = this;
        callback.run(stagingContext);
        endStaging();
    }
    //</editor-fold>

    public void genPersistentBuffers(String key) {
        Preconditions.checkArgument(!persistentVbos.containsKey(key), "The \"key\" already exists.");

        BufferStorage<VBOView> vboStorage = new BufferStorage<>(() -> new VBOView(new GLBuffer()));
        BufferStorage<EBOView> eboStorage = new BufferStorage<>(() -> new EBOView(new GLBuffer()));

        persistentVbos.put(key, vboStorage);
        persistentEbos.put(key, eboStorage);
    }

    protected PersistentVAOHandle getPersistentVAOHandle(AttributeLayout attributeLayout, String eboStorageKey, String[] vboStorageKeys, int eboStoragePageIndex, int[] vboStoragePageIndices) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(vboStorageKeys.length == vboStoragePageIndices.length, "Argument \"vboStorageKeys.length\" must match \"vboStoragePageIndices.length\".");
        Preconditions.checkArgument(persistentEbos.containsKey(eboStorageKey), "Argument \"eboStorageKey\" doesn't have a corresponding storage.");
        for (String vboStorageKey : vboStorageKeys) {
            Preconditions.checkArgument(persistentVbos.containsKey(vboStorageKey), "One of \"vboStorageKeys\" doesn't have a corresponding storage.");
        }

        BufferStorage<EBOView> eboStorage = persistentEbos.get(eboStorageKey);
        Preconditions.checkNotNull(eboStorage);
        Preconditions.checkPositionIndex(eboStoragePageIndex, eboStorage.getPageCount());

        List<BufferStorage<VBOView>> vboStorages = new ArrayList<>();
        for (String vboStorageKey : vboStorageKeys) {
            BufferStorage<VBOView> vboStorage = persistentVbos.get(vboStorageKey);
            Preconditions.checkNotNull(vboStorage);
            vboStorages.add(vboStorage);
        }

        for (int i = 0; i < vboStoragePageIndices.length; i++)
            Preconditions.checkPositionIndex(vboStoragePageIndices[i], vboStorages.get(i).getPageCount());
        }

        // todo

        return null;
    }

    /**
     * <p>This method guarantees no GL buffer binding changes. No potential <code>buffer.bind(0)</code> usage here.</p>
     */
    protected PersistentVBOHandle getPersistentVBOHandle(String key, int size) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");

        BufferStorage<VBOView> storage = persistentVbos.get(key);
        Preconditions.checkNotNull(storage);

        Preconditions.checkArgument(size >= 0, "Cannot have a negative \"size\".");
        Preconditions.checkArgument(size <= storage.getPageSize(),
                "Argument \"size\"=%s must be smaller than or equal to the page size=%s.", size, storage.getPageSize());

        BufferStorage.SlotHandle<VBOView> handle = storage.allocate(size);

        return new PersistentVBOHandle(this, handleGeneration, handle.getOffset(), handle.getSize(), handle);
    }

    /**
     * <p>This method guarantees no GL buffer binding changes. No potential <code>buffer.bind(0)</code> usage here.</p>
     */
    protected PersistentEBOHandle getPersistentEBOHandle(String key, int size) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");

        BufferStorage<EBOView> storage = persistentEbos.get(key);
        Preconditions.checkNotNull(storage);

        Preconditions.checkArgument(size >= 0, "Cannot have a negative \"size\".");
        Preconditions.checkArgument(size <= storage.getPageSize(),
                "Argument \"size\"=%s must be smaller than or equal to the page size=%s.", size, storage.getPageSize());

        BufferStorage.SlotHandle<EBOView> handle = storage.allocate(size);

        return new PersistentEBOHandle(this, handleGeneration, handle.getOffset(), handle.getSize(), handle);
    }

    /**
     * <p>This method (directly OR potentially) changes GL buffer binding.</p>
     * However, it'll only be called during the window period (no GL state restrictions on this period) so <code>buffer.bind(0)</code> is fine.
     * <hr>
     * There're hidden <code>bind(0)</code>s inside {@link TemporaryVAOHandle} and {@link VAO}.
     */
    protected TemporaryVAOHandle getTemporaryVAOHandle(AttributeLayout attributeLayout, TemporaryEBOHandle eboHandle, TemporaryVBOHandle... vboHandles) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(eboHandle.generation == handleGeneration, "The temporary EBO handle is expired.");
        for (TemporaryVBOHandle vboHandle : vboHandles) {
            Preconditions.checkArgument(vboHandle.generation == handleGeneration, "The temporary VBO handle is expired.");
        }

        TemporaryVAOHandle vaoHandle = new TemporaryVAOHandle(this, handleGeneration, attributeLayout, eboHandle, vboHandles);
        temporaryVaos.add(MethodHolder.getVAO(vaoHandle));
        return vaoHandle;
    }

    /**
     * <p>This method (directly OR potentially) changes GL buffer binding.</p>
     * However, it'll only be called during the window period (no GL state restrictions on this period) so <code>buffer.bind(0)</code> is fine.
     * <hr>
     * There're hidden <code>bind(0)</code>s inside {@link TemporaryVBOHandle}.
     */
    protected TemporaryVBOHandle getTemporaryVBOHandle(int size) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative \"size\".");

        VBOView vboView = new VBOView(new GLBuffer());
        vboView.turnOffValidation();
        vboView.bind();
        vboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        vboView.bind(0);
        temporaryVbos.add(vboView);
        return new TemporaryVBOHandle(this, handleGeneration, size, vboView);
    }

    /**
     * <p>This method (directly OR potentially) changes GL buffer binding.</p>
     * However, it'll only be called during the window period (no GL state restrictions on this period) so <code>buffer.bind(0)</code> is fine.
     * <hr>
     * There're hidden <code>bind(0)</code>s inside {@link TemporaryEBOHandle}.
     */
    protected TemporaryEBOHandle getTemporaryEBOHandle(int size) {
        Preconditions.checkState(active, "Must not access StagingBufferManager when the manager is inactive.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative \"size\".");

        EBOView eboView = new EBOView(new GLBuffer());
        eboView.turnOffValidation();
        eboView.bind();
        eboView.alloc(size, BufferUploadHint.STATIC_DRAW);
        eboView.bind(0);
        temporaryEbos.add(eboView);
        return new TemporaryEBOHandle(this, handleGeneration, size, eboView);
    }

    private static class MethodHolder {
        static final MethodHandle VAO_GETTER;

        static {
            VAO_GETTER = ReflectionUtils.getFieldGetter(TemporaryVAOHandle.class, "vao", VAO.class);

            Preconditions.checkNotNull(VAO_GETTER);
        }

        /**
         * @see TemporaryVAOHandle#vao
         */
        static VAO getVAO(TemporaryVAOHandle instance) {
            try {
                return (VAO) VAO_GETTER.invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
