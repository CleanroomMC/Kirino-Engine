package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer.MeshletBufferSlotAllocator;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer.MeshletInputDoubleBuffer;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer.VertexOutputDoubleBuffer;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MeshletGpuRegistry {
    protected final MeshletInputDoubleBuffer meshletInputBuffer;
    protected final VertexOutputDoubleBuffer vertexOutputBuffer;
    protected final MeshletBufferSlotAllocator meshletBufferSlotAllocator;

    private int meshletIdCounter = 0;
    private final List<Integer> freeMeshletIds = new ArrayList<>();
    private final List<Integer> pendingMeshletIdRemoval = new ArrayList<>();
    // begin writing is the synchronization point
    private final List<Integer> meshletIdAddedSinceLastBegin = new ArrayList<>();
    private final List<Integer> meshletIdRemovedSinceLastBegin = new ArrayList<>();

    private boolean writing = false;
    private boolean finishedWritingOnce = false;

    public MeshletGpuRegistry() {
        meshletInputBuffer = new MeshletInputDoubleBuffer();
        vertexOutputBuffer = new VertexOutputDoubleBuffer();
        meshletBufferSlotAllocator = new MeshletBufferSlotAllocator(meshletInputBuffer);
    }

    public void lateInit() {
        meshletInputBuffer.lateInit();
        vertexOutputBuffer.lateInit();
    }

    //<editor-fold desc="id allocation & disposal">
    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The allocated id must be disposed later rather than leaked</li>
     * </ul>
     *
     * Thread-safety is guaranteed. Can be run anywhere.
     * Feel free to allocate ids during writing; that is allowed.
     */
    public void allocateMeshletID(MeshletComponent meshletComponent) {
        meshletComponent.meshletId = allocateMeshletID();
    }

    private synchronized int allocateMeshletID() {
        int meshletId;
        if (freeMeshletIds.isEmpty()) {
            meshletId = meshletIdCounter++;
        } else {
            meshletId = freeMeshletIds.removeLast();
        }

        meshletIdAddedSinceLastBegin.add(meshletId);

        return meshletId;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The id to be disposed must be valid (i.e. is allocated from this registry)</li>
     *     <li>Must not dispose twice</li>
     * </ul>
     *
     * Thread-safety is guaranteed. Can be run anywhere.
     * Feel free to dispose ids during writing; that is allowed.
     */
    public synchronized void disposeMeshletID(int meshletId) {
        meshletIdRemovedSinceLastBegin.add(meshletId);
        pendingMeshletIdRemoval.add(meshletId);
    }
    //</editor-fold>

    public synchronized boolean isWriting() {
        return writing;
    }

    public synchronized boolean isFinishedWritingOnce() {
        return finishedWritingOnce;
    }

    public synchronized boolean hasMeshletChanges() {
        return !meshletIdAddedSinceLastBegin.isEmpty() || !meshletIdRemovedSinceLastBegin.isEmpty();
    }

    /**
     * Thread-safety is guaranteed. Must be run first or after {@link #finishWriting}.
     * <p>Semantic Note: it consumes all ID changes until now since last <code>beginWriting</code> and prepares for the writing task.</p>
     * <br>
     * Should be called before an independent writing task.
     */
    public synchronized void beginWriting() {
        Preconditions.checkState(!writing, "Must not be writing already.");

        // handle side effects, modify id -> slot mapping to be exact
        meshletBufferSlotAllocator.syncMeshletIdAddition(meshletIdAddedSinceLastBegin);
        meshletBufferSlotAllocator.syncMeshletIdRemoval(meshletIdRemovedSinceLastBegin);
        meshletIdAddedSinceLastBegin.clear();
        meshletIdRemovedSinceLastBegin.clear();

        meshletBufferSlotAllocator.growBufferIfNeeded();

        writing = true;
    }

    /**
     * Thread-safety is guaranteed. Must be run after {@link #beginWriting()}.
     * <p>Semantic Note: it frees all pending ID disposals during the writing phase and prepares the output to be consumed.</p>
     * <br>
     * Should be called after an independent writing task.
     */
    public synchronized void finishWriting() {
        Preconditions.checkState(writing, "Must be writing already.");

        freeMeshletIds.addAll(pendingMeshletIdRemoval);
        pendingMeshletIdRemoval.clear();

        meshletInputBuffer.swap();

        writing = false;
        if (!finishedWritingOnce) {
            finishedWritingOnce = true;
        }
    }

    /**
     * The consume target is up to date since last {@link #finishWriting()}.
     * Must only retrieve the ssbo after {@link #finishWriting()} and before next {@link #finishWriting()}.
     *
     * <p>Be aware of the current phase when calling this method.</p>
     *
     * @return The the ssbo to be consumed by compute shaders
     */
    public synchronized SSBOView getConsumeTarget() {
        Preconditions.checkState(finishedWritingOnce, "Must finished writing once.");

        return meshletInputBuffer.getConsumeTarget();
    }

    /**
     * The meshlet count is up to date since last begin.
     *
     * <p>Be aware of the current phase when calling this method.</p>
     *
     * @return The meshlet count
     */
    public synchronized int getMeshletCount() {
        return meshletBufferSlotAllocator.getMeshletCount();
    }
}
