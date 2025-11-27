package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.schemata.pool.ThreadSafeGenPool;
import org.jspecify.annotations.NonNull;

public final class LowLevelDC implements IDrawCommand {

    public enum DrawType {
        ELEMENTS,                    // directly drawable
        ELEMENTS_INSTANCED,          // directly drawable
        MULTI_ELEMENTS_INDIRECT,     // directly drawable
        MULTI_ELEMENTS_INDIRECT_UNIT // indirectly drawable (components of MULTI_ELEMENTS_INDIRECT)
    }

    private final static ThreadSafeGenPool<LowLevelDC> POOL = new ThreadSafeGenPool<>(KirinoCore.KIRINO_CONFIG_HUB.lowLevelDrawCommandPoolSize) {
        @NonNull
        @Override
        public LowLevelDC newObject(@NonNull Handle<LowLevelDC> handle) {
            return new LowLevelDC(handle);
        }
    };

    public static void nextGen() {
        POOL.nextGen();
    }

    private final ThreadSafeGenPool.Handle<LowLevelDC> handle;

    private LowLevelDC(ThreadSafeGenPool.Handle<LowLevelDC> handle) {
        this.handle = handle;
    }

    public static LowLevelDC get() {
        return POOL.lend();
    }

    public void recycle() {
        if (handle.isInPool()) {
            reset();
            handle.recycle();
        }
    }

    public DrawType type = null;

    public int vao = -1;
    public int idb = -1;

    public int mode = -1;
    public int indicesCount = -1;
    public int elementType = -1;
    public int eboOffset = -1;

    public int instanceCount = -1;

    public int idbOffset = -1;
    public int idbStride = -1;

    public int idIndicesCount = -1;
    public int idInstanceCount = -1;
    public int idEboFirstIndex = -1;
    public int idBaseVertex = -1;
    public int idBaseInstance = -1;

    private void reset() {
        type = null;
        vao = -1;
        idb = -1;
        mode = -1;
        indicesCount = -1;
        elementType = -1;
        eboOffset = -1;
        instanceCount = -1;
        idbOffset = -1;
        idbStride = -1;
        idIndicesCount = -1;
        idInstanceCount = -1;
        idEboFirstIndex = -1;
        idBaseVertex = -1;
        idBaseInstance = -1;
    }

    // ========== filler methods ==========

    public LowLevelDC fillElement(int vao, int mode, int indicesCount, int elementType, int eboOffset) {
        this.type = DrawType.ELEMENTS;
        this.vao = vao;
        this.mode = mode;
        this.indicesCount = indicesCount;
        this.elementType = elementType;
        this.eboOffset = eboOffset;
        return this;
    }

    public LowLevelDC fillElementInstanced(int vao, int mode, int indicesCount, int elementType, int eboOffset, int instanceCount) {
        this.type = DrawType.ELEMENTS_INSTANCED;
        this.vao = vao;
        this.mode = mode;
        this.indicesCount = indicesCount;
        this.elementType = elementType;
        this.eboOffset = eboOffset;
        this.instanceCount = instanceCount;
        return this;
    }

    public LowLevelDC fillMultiElementIndirect(int vao, int idb, int mode, int elementType, int idbOffset, int idbStride, int instanceCount) {
        this.type = DrawType.MULTI_ELEMENTS_INDIRECT;
        this.vao = vao;
        this.idb = idb;
        this.mode = mode;
        this.elementType = elementType;
        this.idbOffset = idbOffset;
        this.idbStride = idbStride;
        this.instanceCount = instanceCount;
        return this;
    }

    public LowLevelDC fillMultiElementIndirectUnit(int vao, int mode, int elementType, int idIndicesCount, int idInstanceCount, int idEboFirstIndex, int idBaseVertex, int idBaseInstance) {
        this.type = DrawType.MULTI_ELEMENTS_INDIRECT_UNIT;
        this.vao = vao;
        this.mode = mode;
        this.elementType = elementType;
        this.idIndicesCount = idIndicesCount;
        this.idInstanceCount = idInstanceCount;
        this.idEboFirstIndex = idEboFirstIndex;
        this.idBaseVertex = idBaseVertex;
        this.idBaseInstance = idBaseInstance;
        return this;
    }
}
