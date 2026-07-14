package com.cleanroommc.kirino.engine.process.analysis.install;

import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.debug.hud.event.DebugHUDRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import com.cleanroommc.kirino.engine.render.core.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @see com.cleanroommc.kirino.engine.render.core.RenderExtensions
 */
public final class RenderExtensionsInit {

    static void init(AnalyticalWorldView context) {
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        context.bus().post(shaderRegistrationEvent);
        context.ext().rawShaders.clear();
        context.ext().rawShaders.putAll(MethodHolder.getRawShaders(shaderRegistrationEvent));

        if (context.rs().enablePostProcessing) {
            PostProcessingRegistrationEvent postProcessingRegistrationEvent = new PostProcessingRegistrationEvent();
            context.bus().post(postProcessingRegistrationEvent);
            context.ext().postProcessingEntries.clear();
            context.ext().postProcessingEntries.addAll(MethodHolder.getPostProcessingEntries(postProcessingRegistrationEvent));
        }

        DebugHUDRegistrationEvent debugHudRegistrationEvent = new DebugHUDRegistrationEvent();
        context.bus().post(debugHudRegistrationEvent);
        context.ext().debugHuds.clear();
        context.ext().debugHuds.addAll(MethodHolder.getDebugHuds(debugHudRegistrationEvent));
    }

    private static final class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldGetter(ShaderRegistrationEvent.class, "rawShaders", Map.class),
                    ReflectionUtils.getFieldGetter(PostProcessingRegistrationEvent.class, "postProcessingEntries", List.class),
                    ReflectionUtils.getFieldGetter(DebugHUDRegistrationEvent.class, "debugHuds", List.class));

            Preconditions.checkNotNull(DELEGATE.shaderResourceLocationsGetter);
            Preconditions.checkNotNull(DELEGATE.postProcessingEntriesGetter);
            Preconditions.checkNotNull(DELEGATE.debugHudsGetter);
        }

        @SuppressWarnings("unchecked")
        static Map<ResourceLocation, Optional<ShaderCompileOptions>> getRawShaders(ShaderRegistrationEvent shaderRegistrationEvent) {
            Map<ResourceLocation, Optional<ShaderCompileOptions>> rawShaders;
            try {
                rawShaders = (Map<ResourceLocation, Optional<ShaderCompileOptions>>) DELEGATE.shaderResourceLocationsGetter.invokeExact(shaderRegistrationEvent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return rawShaders;
        }

        @SuppressWarnings("unchecked")
        static List<Triple<String, String[], TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass>>> getPostProcessingEntries(PostProcessingRegistrationEvent postProcessingRegistrationEvent) {
            List<Triple<String, String[], TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass>>> postProcessingEntries;
            try {
                postProcessingEntries = (List<Triple<String, String[], TriFunction<ResourceSlot<Renderer>, PipelineStateObject, ResourceSlot<VAO>, AbstractPostProcessingPass>>>) DELEGATE.postProcessingEntriesGetter.invokeExact(postProcessingRegistrationEvent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return postProcessingEntries;
        }

        @SuppressWarnings("unchecked")
        static List<ImmediateHUD> getDebugHuds(DebugHUDRegistrationEvent debugHudRegistrationEvent) {
            List<ImmediateHUD> debugHuds;
            try {
                debugHuds = (List<ImmediateHUD>) DELEGATE.debugHudsGetter.invokeExact(debugHudRegistrationEvent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return debugHuds;
        }

        record Delegate(
                MethodHandle shaderResourceLocationsGetter,
                MethodHandle postProcessingEntriesGetter,
                MethodHandle debugHudsGetter) {
        }
    }
}
