package com.cleanroommc.kirino.engine.render.core.resource;

public enum UploadStrategy {
    /**
     * High performance strategy
     */
    PERSISTENT,
    /**
     * Performance heavy strategy. Only use it for debug purposes
     */
    TEMPORARY
}
