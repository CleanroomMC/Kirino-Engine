package com.cleanroommc.kirino.engine.render.core.pipeline.pass;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.debug.KHRDebug;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class RenderPass {
    private final Map<String, Subpass> subpassMap = new HashMap<>();
    private final Map<String, List<SubpassDecorator>> subpassDecoratorMap = new HashMap<>();
    private final List<String> subpassOrder = new ArrayList<>();
    private final DrawQueue drawQueue = new DrawQueue();

    private final ResourceSlot<GraphicResourceManager> graphicResourceManager;
    private final ResourceSlot<IndirectDrawBufferGenerator> idbGenerator;

    public final String passName;

    public int size() {
        return subpassMap.size();
    }

    public RenderPass(
            String passName,
            ResourceSlot<GraphicResourceManager> graphicResourceManager,
            ResourceSlot<IndirectDrawBufferGenerator> idbGenerator) {
        this.passName = passName;
        this.graphicResourceManager = graphicResourceManager;
        this.idbGenerator = idbGenerator;
    }

    public boolean hasSubpass(String subpassName) {
        return subpassMap.containsKey(subpassName);
    }

    public void addSubpass(String subpassName, Subpass subpass) {
        if (subpassMap.containsKey(subpassName)) {
            return;
        }
        subpassMap.put(subpassName, subpass);
        subpassOrder.add(subpassName);
    }

    public void removeSubpass(String subpassName) {
        subpassMap.remove(subpassName);
        subpassOrder.remove(subpassName);
    }

    public void attachSubpassDecorator(String subpassName, SubpassDecorator decorator) {
        List<SubpassDecorator> list = subpassDecoratorMap.computeIfAbsent(subpassName, k -> new ArrayList<>());
        list.add(decorator);
    }

    public void render(@NonNull ResourceStorage storage, @Nullable Camera camera) {
        render(storage, camera, null, null);
    }

    public void render(@NonNull ResourceStorage storage, @Nullable Camera camera, @Nullable BiConsumer<String, Integer> subpassCallback, @Nullable Object @Nullable [] payloads) {
        if (payloads != null) {
            Preconditions.checkArgument(payloads.length == size(),
                    "Payloads length (%s) must equal to the size (%s) of this render pass.", payloads.length, size());
        }

        int index = 0;
        for (String subpassName : subpassOrder) {
            KHRDebug.pushGroup(passName + " - " + subpassName);

            drawQueue.clear();
            Subpass subpass = subpassMap.get(subpassName);
            subpass.collectCommands(storage, drawQueue);
            List<SubpassDecorator> list = subpassDecoratorMap.get(subpassName);
            if (list != null) {
                for (SubpassDecorator decorator : list) {
                    subpass.decorateCommands(storage, drawQueue, decorator);
                }
            }

            subpass.render(
                    storage,
                    drawQueue,
                    camera,
                    storage.get(graphicResourceManager),
                    storage.get(idbGenerator),
                    payloads == null ? null : payloads[index]);

            if (subpassCallback != null) {
                subpassCallback.accept(subpassName, index);
            }

            KHRDebug.popGroup();
            index++;
        }
    }
}
