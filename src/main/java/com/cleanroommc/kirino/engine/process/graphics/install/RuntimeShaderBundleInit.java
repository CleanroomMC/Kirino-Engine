package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.render.core.RuntimeShaderBundle;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingEntry;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;

import java.lang.invoke.MethodHandle;

/**
 * @see com.cleanroommc.kirino.engine.render.core.RuntimeShaderBundle
 */
public final class RuntimeShaderBundleInit {

    static void init(GraphicsWorldView context) {
        for (PostProcessingEntry entry : context.ext().postProcessingEntries) {
            ResourceSlot<ShaderProgram> shaderProgram = MethodHolder.newShaderProgram(
                    context.shaderrb(),
                    entry.subpassName,
                    entry.shaders);
            MethodHolder.setShaderProgram(entry, shaderProgram);
        }
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getMethod(RuntimeShaderBundle.class, "newShaderProgram", ResourceSlot.class, String.class, String[].class),
                    ReflectionUtils.getMethod(PostProcessingEntry.class, "setShaderProgram", void.class, ResourceSlot.class));

            Preconditions.checkNotNull(DELEGATE.newShaderProgram);
            Preconditions.checkNotNull(DELEGATE.setShaderProgram);
        }

        @SuppressWarnings("unchecked")
        static ResourceSlot<ShaderProgram> newShaderProgram(RuntimeShaderBundle owner, String name, String[] shaders) {
            try {
                return (ResourceSlot<ShaderProgram>) DELEGATE.newShaderProgram.invokeExact(owner, name, shaders);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        static void setShaderProgram(PostProcessingEntry owner, ResourceSlot<ShaderProgram> shaderProgram) {
            try {
                DELEGATE.setShaderProgram.invokeExact(owner, shaderProgram);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record Delegate(
                MethodHandle newShaderProgram,
                MethodHandle setShaderProgram) {
        }
    }
}
