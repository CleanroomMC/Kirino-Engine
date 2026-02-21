package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class DiffingContainer {

    private final MinecraftCamera camera;

    DiffingContainer(MinecraftCamera camera) {
        this.camera = camera;
    }

    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldForegroundRenderDis = 0;
    private final FloatBuffer oldViewRot = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer oldProjection = BufferUtils.createFloatBuffer(16);

    public int getOldForegroundRenderDis() {
        return oldForegroundRenderDis;
    }

    public boolean updateCameraPos() {
        Vector3f camPos = camera.getWorldOffset();
        if (camPos.x != oldCamX || camPos.y != oldCamY || camPos.z != oldCamZ) {
            if (Math.sqrt(
                    (camPos.x - oldCamX) * (camPos.x - oldCamX) +
                            (camPos.y - oldCamY) * (camPos.y - oldCamY) +
                            (camPos.z - oldCamZ) * (camPos.z - oldCamZ)) >= KirinoCommonCore.KIRINO_CONFIG_HUB.getChunkUpdateDisplacement()) {
                oldCamX = camPos.x;
                oldCamY = camPos.y;
                oldCamZ = camPos.z;
                return true;
            }
        }
        return false;
    }

    public boolean updateForegroundRenderDis() {
        int renderDis = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
        renderDis = Math.max(renderDis, KirinoCommonCore.KIRINO_CONFIG_HUB.getForegroundRenderDistance());
        if (oldForegroundRenderDis != renderDis) {
            oldForegroundRenderDis = renderDis;
            return true;
        }
        return false;
    }

    public boolean updateCameraViewProj() {
        FloatBuffer viewRot = camera.getViewRotationBuffer();
        FloatBuffer projection = camera.getProjectionBuffer();
        if (!oldViewRot.equals(viewRot) || !oldProjection.equals(projection)) {
            oldViewRot.position(0).put(viewRot).flip();
            oldProjection.position(0).put(projection).flip();
            return true;
        }
        return false;
    }
}
