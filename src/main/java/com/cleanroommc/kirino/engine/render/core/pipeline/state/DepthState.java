package com.cleanroommc.kirino.engine.render.core.pipeline.state;

public record DepthState(
        boolean depthTest,
        boolean depthWrite,
        int depthFunc) {
}
