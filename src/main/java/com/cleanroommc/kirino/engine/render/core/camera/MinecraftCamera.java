package com.cleanroommc.kirino.engine.render.core.camera;

import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.invoke.MethodHandle;
import java.nio.FloatBuffer;

public class MinecraftCamera implements ICamera {
    public double getPartialTicks() {
        Minecraft minecraft = Minecraft.getMinecraft();
        return minecraft.isGamePaused() ? MethodHolder.getPartialTicksPaused(minecraft) : minecraft.getRenderPartialTicks();
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        return new Matrix4f(MethodHolder.getProjectionBuffer());
    }

    @Override
    public FloatBuffer getProjectionBuffer() {
        return MethodHolder.getProjectionBuffer();
    }

    @Override
    public Matrix4f getViewRotationMatrix() {
        return new Matrix4f(MethodHolder.getViewRotationBuffer());
    }

    @Override
    public FloatBuffer getViewRotationBuffer() {
        return MethodHolder.getViewRotationBuffer();
    }

    @Override
    public Vector3f getWorldOffset() {
        Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
        if (camera == null) {
            camera = MethodHolder.getPlayer(Minecraft.getMinecraft());
        }
        double partialTicks = getPartialTicks();
        double camX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double camY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double camZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        return new Vector3f((float)camX, (float)camY, (float)camZ);
    }

    private static class MethodHolder {
        static final CameraInfoDelegate DELEGATE;

        static {
            DELEGATE = new CameraInfoDelegate(
                    ReflectionUtils.getFieldGetter(ActiveRenderInfo.class, "PROJECTION", "field_178813_c", FloatBuffer.class),
                    ReflectionUtils.getFieldGetter(ActiveRenderInfo.class, "MODELVIEW", "field_178812_b", FloatBuffer.class),
                    ReflectionUtils.getFieldGetter(Minecraft.class, "renderPartialTicksPaused", "field_193996_ah", float.class),
                    ReflectionUtils.getFieldGetter(Minecraft.class, "player", "field_71439_g", EntityPlayerSP.class));

            Preconditions.checkNotNull(DELEGATE.projectionBuffer());
            Preconditions.checkNotNull(DELEGATE.viewRotationBuffer());
            Preconditions.checkNotNull(DELEGATE.partialTicksPaused());
            Preconditions.checkNotNull(DELEGATE.player());
        }

        /**
         * @see ActiveRenderInfo#PROJECTION
         */
        static FloatBuffer getProjectionBuffer() {
            try {
                return (FloatBuffer) DELEGATE.projectionBuffer().invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see ActiveRenderInfo#MODELVIEW
         */
        static FloatBuffer getViewRotationBuffer() {
            try {
                return (FloatBuffer) DELEGATE.viewRotationBuffer().invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see Minecraft#renderPartialTicksPaused
         */
        static float getPartialTicksPaused(Minecraft instance) {
            try {
                return (float) DELEGATE.partialTicksPaused().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see Minecraft#player
         */
        static Entity getPlayer(Minecraft instance) {
            try {
                return (EntityPlayerSP) DELEGATE.player().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record CameraInfoDelegate(
                MethodHandle projectionBuffer,
                MethodHandle viewRotationBuffer,
                MethodHandle partialTicksPaused,
                MethodHandle player) {}
    }
}
