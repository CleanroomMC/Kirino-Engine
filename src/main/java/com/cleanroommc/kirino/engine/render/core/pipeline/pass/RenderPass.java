package com.cleanroommc.kirino.engine.render.core.pipeline.pass;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.ClaimedScopeHandle;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.debug.KHRDebug;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RenderPass {

    private final Map<String, Subpass> subpassMap = new HashMap<>();
    private final Map<String, List<SubpassDecorator>> subpassDecoratorMap = new HashMap<>();
    private final List<String> subpassOrder = new ArrayList<>();
    private final DrawQueue drawQueue = new DrawQueue();

    public final String passName;
    private final ResourceSlot<GraphicResourceManager> graphicResourceManager;
    private final ResourceSlot<IndirectDrawBufferGenerator> idbGenerator;

    public int size() {
        return subpassMap.size();
    }

    public RenderPass(
            @NonNull String passName,
            @NonNull ResourceSlot<GraphicResourceManager> graphicResourceManager,
            @NonNull ResourceSlot<IndirectDrawBufferGenerator> idbGenerator) {

        Preconditions.checkNotNull(passName);
        Preconditions.checkNotNull(graphicResourceManager);
        Preconditions.checkNotNull(idbGenerator);

        this.passName = passName;
        this.graphicResourceManager = graphicResourceManager;
        this.idbGenerator = idbGenerator;
    }

    public boolean hasSubpass(@NonNull String subpassName) {
        Preconditions.checkNotNull(subpassName);

        return subpassMap.containsKey(subpassName);
    }

    /**
     * It silently fails when receiving a duplicate <code>subpassName</code>.
     */
    public void addSubpass(@NonNull String subpassName, @NonNull Subpass subpass) {
        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(subpass);

        if (subpassMap.containsKey(subpassName)) {
            return;
        }

        subpassMap.put(subpassName, subpass);
        subpassOrder.add(subpassName);
    }

    @Nullable
    public Subpass removeSubpass(@NonNull String subpassName) {
        Preconditions.checkNotNull(subpassName);

        Subpass subpass = subpassMap.remove(subpassName);
        subpassOrder.remove(subpassName);

        return subpass;
    }

    public void clearSubpasses() {
        subpassMap.clear();
        subpassOrder.clear();
    }

    public void attachSubpassDecorator(@NonNull String subpassName, @NonNull SubpassDecorator decorator) {
        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(decorator);

        List<SubpassDecorator> list = subpassDecoratorMap.computeIfAbsent(subpassName, k -> new ArrayList<>());
        list.add(decorator);
    }

    /**
     * @param camera The optional camera instance
     */
    public void render(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera) {

        render(storage, glKnowledge, camera, null, null, null);
    }

    /**
     * @param camera The optional camera instance
     * @param subpassCallback The optional callback. It'll be executed right after each subpass
     * @param subpassScopeProvider The optional scope provider
     * @param payloads The optional payload for each subpass. The length must match the number of subpasses
     */
    public void render(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera,
            @Nullable SubpassCallback subpassCallback,
            @Nullable SubpassScopeProvider subpassScopeProvider,
            @Nullable Object @Nullable [] payloads) {

        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(glKnowledge);

        if (payloads != null) {
            Preconditions.checkArgument(payloads.length == size(),
                    "Payloads length (%s) must equal to the size (%s) of this render pass.", payloads.length, size());
        }

        int index = 0;
        for (String subpassName : subpassOrder) {

            KHRDebug.pushGroup(passName + " - " + subpassName);
            try {
                drawQueue.clear();
                Subpass subpass = subpassMap.get(subpassName);
                subpass.collectCommands(storage, drawQueue);
                List<SubpassDecorator> list = subpassDecoratorMap.get(subpassName);
                if (list != null) {
                    for (SubpassDecorator decorator : list) {
                        subpass.decorateCommands(storage, drawQueue, decorator);
                    }
                }

                Object payload = payloads == null ? null : payloads[index];

                try (ClaimedScopeHandle ignored = subpassScopeProvider != null ?
                        subpassScopeProvider.provide(
                                index,
                                size(),
                                subpassName,
                                storage,
                                glKnowledge,
                                camera,
                                payload) : null) {

                    subpass.render(
                            storage,
                            glKnowledge,
                            drawQueue,
                            camera,
                            storage.get(graphicResourceManager),
                            storage.get(idbGenerator),
                            payload);
                }

                if (subpassCallback != null) {
                    subpassCallback.run(
                            index,
                            size(),
                            subpassName,
                            storage,
                            glKnowledge,
                            camera,
                            payload);
                }
            } finally {
                KHRDebug.popGroup();
            }

            index++;
        }
    }
}
