package com.cleanroommc.kirino.engine.render.core.pipeline.pass;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.ClaimedScopeHandle;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * It provides the render scope, and it'll get executed before the render call.
 *
 * @see SubpassCallback
 */
@FunctionalInterface
public interface SubpassScopeProvider {

    @NonNull
    ClaimedScopeHandle provide(
            int index,
            int subpassCount,
            @NonNull String subpassName,
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera,
            @Nullable Object payload);
}
