package com.cleanroommc.kirino.engine.analysis.install;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.analysis.view.AnalyticalWorldViewImpl;
import com.cleanroommc.kirino.engine.render.core.debug.hud.IImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.debug.hud.event.DebugHUDRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class AnalyticalWorldInstaller implements ModuleInstaller<Headless> {

    private boolean init = false;

    private void initRenderExtensions(AnalyticalWorldViewImpl context) {
        ShaderRegistrationEvent shaderRegistrationEvent = new ShaderRegistrationEvent();
        context.bus().post(shaderRegistrationEvent);
        context.ext().shaderRLs.clear();
        context.ext().shaderRLs.addAll(MethodHolder.getShaderResourceLocations(shaderRegistrationEvent));

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

    private void prepare(WorldContext<Headless> context) {
        if (init) {
            return;
        }
        if (!(context instanceof AnalyticalWorldViewImpl analyticalWorldView)) {
            throw new RuntimeException("WorldContext is not an instance of AnalyticalWorldViewImpl.");
        }

        initRenderExtensions(analyticalWorldView);

        init = true;
    }

    @Override
    public void install(@NonNull WorldContext<Headless> context) {
        context.on(FramePhase.PREPARE, this::prepare);
    }

    private static class MethodHolder {
        static final MethodHolder.Delegate DELEGATE;

        static {
            DELEGATE = new MethodHolder.Delegate(
                    ReflectionUtils.getFieldGetter(ShaderRegistrationEvent.class, "shaderResourceLocations", List.class),
                    ReflectionUtils.getFieldGetter(PostProcessingRegistrationEvent.class, "postProcessingEntries", List.class),
                    ReflectionUtils.getFieldGetter(DebugHUDRegistrationEvent.class, "debugHuds", List.class));

            Preconditions.checkNotNull(DELEGATE.shaderResourceLocationsGetter);
            Preconditions.checkNotNull(DELEGATE.postProcessingEntriesGetter);
            Preconditions.checkNotNull(DELEGATE.debugHudsGetter);
        }

        @SuppressWarnings("unchecked")
        static List<ResourceLocation> getShaderResourceLocations(ShaderRegistrationEvent shaderRegistrationEvent) {
            List<ResourceLocation> shaderResourceLocations;
            try {
                shaderResourceLocations = (List<ResourceLocation>) DELEGATE.shaderResourceLocationsGetter.invokeExact(shaderRegistrationEvent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return shaderResourceLocations;
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
        static List<IImmediateHUD> getDebugHuds(DebugHUDRegistrationEvent debugHudRegistrationEvent) {
            List<IImmediateHUD> debugHuds;
            try {
                debugHuds = (List<IImmediateHUD>) DELEGATE.debugHudsGetter.invokeExact(debugHudRegistrationEvent);
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
