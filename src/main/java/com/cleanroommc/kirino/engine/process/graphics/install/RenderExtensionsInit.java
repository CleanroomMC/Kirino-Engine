package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingEntry;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;

import java.lang.invoke.MethodHandle;

/**
 * @see com.cleanroommc.kirino.engine.render.core.RenderExtensions
 */
public final class RenderExtensionsInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();
        PostProcessingManager ppManager = context.ext().postProcessingManager;

        if (context.rs().enablePostProcessing) {
            MethodHolder.openRegister(ppManager);
            for (PostProcessingEntry entry : context.ext().postProcessingEntries) {
                // this shader program slot is dynamically generated during RuntimeShaderBundleInit
                ResourceSlot<ShaderProgram> slot = ppManager.addSubpass(entry);
                ShaderProgram shaderProgram = storage.get(context.graphicsb().shaderRegistry).newShaderProgram(entry.shaders);
                storage.put(slot, shaderProgram);
            }
            MethodHolder.closeRegister(ppManager);
        }

        if (context.rs().enablePostProcessing) {
            Preconditions.checkState(!context.ext().postProcessingEntries.isEmpty(),
                    "Post-processing is enabled. Post-processing manager must have at least one subpass at runtime to work as expected.");

            ppManager.lateInit(
                    storage.get(context.graphicsb().frameFinalizer).acquireFramebufferStore(),
                    context.rs().postProcessingSchedule);
        } else {
            Preconditions.checkState(context.ext().postProcessingEntries.isEmpty(),
                    "Post-processing is disabled. Post-processing manager must have exactly zero subpass at runtime to work as expected.");
        }
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getMethod(PostProcessingManager.class, "openRegister", void.class),
                    ReflectionUtils.getMethod(PostProcessingManager.class, "closeRegister", void.class));

            Preconditions.checkNotNull(DELEGATE.openRegister);
            Preconditions.checkNotNull(DELEGATE.closeRegister);
        }

        static void openRegister(PostProcessingManager owner) {
            try {
                DELEGATE.openRegister.invokeExact(owner);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        static void closeRegister(PostProcessingManager owner) {
            try {
                DELEGATE.closeRegister.invokeExact(owner);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record Delegate(
                MethodHandle openRegister,
                MethodHandle closeRegister) {
        }
    }
}
