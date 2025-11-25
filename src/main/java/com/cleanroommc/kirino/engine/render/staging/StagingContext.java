package com.cleanroommc.kirino.engine.render.staging;

import com.cleanroommc.kirino.engine.render.staging.handle.*;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;

public class StagingContext {
    protected StagingBufferManager manager;

    public PersistentVAOHandle getPersistentVAO(AttributeLayout attributeLayout, String eboStorageKey, String vboStorageKey, int eboStoragePageIndex, int vboStoragePageIndex) {
        return getPersistentVAO(attributeLayout, eboStorageKey, new String[]{vboStorageKey}, eboStoragePageIndex, new int[]{vboStoragePageIndex});
    }

    public PersistentVAOHandle getPersistentVAO(AttributeLayout attributeLayout, String eboStorageKey, String[] vboStorageKeys, int eboStoragePageIndex, int[] vboStoragePageIndices) {
        return manager.getPersistentVAOHandle(attributeLayout, eboStorageKey, vboStorageKeys, eboStoragePageIndex, vboStoragePageIndices);
    }

    public PersistentVBOHandle getPersistentVBO(String storageKey, int size) {
        return manager.getPersistentVBOHandle(storageKey, size);
    }

    public PersistentEBOHandle getPersistentEBO(String storageKey, int size) {
        return manager.getPersistentEBOHandle(storageKey, size);
    }

    public TemporaryVAOHandle getTemporaryVAO(AttributeLayout attributeLayout, TemporaryEBOHandle eboHandle, TemporaryVBOHandle... vboHandles) {
        return manager.getTemporaryVAOHandle(attributeLayout, eboHandle, vboHandles);
    }

    public TemporaryVBOHandle getTemporaryVBO(int size) {
        return manager.getTemporaryVBOHandle(size);
    }

    public TemporaryEBOHandle getTemporaryEBO(int size) {
        return manager.getTemporaryEBOHandle(size);
    }
}
