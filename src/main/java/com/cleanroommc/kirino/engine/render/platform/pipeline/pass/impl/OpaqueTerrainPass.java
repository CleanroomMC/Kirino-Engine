package com.cleanroommc.kirino.engine.render.platform.pipeline.pass.impl;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletRenderPayload;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

public class OpaqueTerrainPass extends Subpass {
    /**
     * @param renderer A global renderer
     * @param pso      A pipeline state object (pipeline parameters)
     */
    public OpaqueTerrainPass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso) {
        super(renderer, pso);
    }

    @Override
    protected void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable Camera camera, @Nullable Object payload) {
        int worldOffset = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "worldOffset");
        int viewRot = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "viewRot");
        int projection = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "projection");

        Preconditions.checkNotNull(camera);

        Vector3f vec3 = camera.getWorldOffset();
        GL20.glUniform3f(worldOffset, vec3.x, vec3.y, vec3.z);
        GL20C.glUniformMatrix4fv(viewRot, false, camera.getViewRotationBuffer());
        GL20C.glUniformMatrix4fv(projection, false, camera.getProjectionBuffer());

        int tex = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "tex");

        // test
        int[] res = new int[1];
        GL11C.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, res);
        int texUnit = res[0];

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,
                Minecraft.getMinecraft()
                        .getTextureManager()
                        .getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
                        .getGlTextureId());
        GL20.glUniform1i(tex, 3);
        GL13.glActiveTexture(texUnit);
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @Override
    public @NonNull PassHint passHint() {
        return PassHint.OPAQUE;
    }

    @Override
    protected void execute(@NonNull ResourceStorage storage, @NonNull DrawQueue drawQueue, @Nullable Object payload) {
        MeshletRenderPayload meshletRenderPayload = (MeshletRenderPayload) payload;
        storage.get(renderer).dummyDraw(GL11.GL_TRIANGLES, 0, (int) (meshletRenderPayload.uintIndexCount() & 0xFFFF_FFFFL));
    }

    @Override
    public void collectCommands(@NonNull ResourceStorage storage, @NonNull DrawQueue drawQueue) {
    }
}
