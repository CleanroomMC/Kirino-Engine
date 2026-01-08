package com.cleanroommc.kirino.engine.render.debug.hud;

import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import org.jspecify.annotations.NonNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class InGameDebugHUDManager {

    private static class GLStateBackup {
        private final IntBuffer INT_BUFFER_16 = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        private final FloatBuffer FLOAT_BUFFER_16 = ByteBuffer.allocateDirect(16 << 2).order(ByteOrder.nativeOrder()).asFloatBuffer();

        private int textureID = 0;
        private float r = 0, g = 0, b = 0, a = 0;
        private boolean blend = false;
        private boolean lighting = false;
        private boolean texture2D = false;
        private boolean alphaTest = false;
        private int shadeModel = 0;
        private boolean depthTest = false;
        private boolean cullFace = false;
        private int blendSrcRgb;
        private int blendDstRgb;
        private int blendSrcAlpha;
        private int blendDstAlpha;
        private int alphaFunc;
        private float alphaRef;

        private void storeGlStates() {
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D, INT_BUFFER_16);
            textureID = INT_BUFFER_16.get(0);
            FLOAT_BUFFER_16.clear();
            GL11.glGetFloat(GL11.GL_CURRENT_COLOR, FLOAT_BUFFER_16);
            r = FLOAT_BUFFER_16.get(0);
            g = FLOAT_BUFFER_16.get(1);
            b = FLOAT_BUFFER_16.get(2);
            a = FLOAT_BUFFER_16.get(3);
            blend = GL11.glIsEnabled(GL11.GL_BLEND);
            lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
            texture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL11.GL_SHADE_MODEL, INT_BUFFER_16);
            shadeModel = INT_BUFFER_16.get(0);
            depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB, INT_BUFFER_16);
            blendSrcRgb = INT_BUFFER_16.get(0);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL14.GL_BLEND_DST_RGB, INT_BUFFER_16);
            blendDstRgb = INT_BUFFER_16.get(0);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA, INT_BUFFER_16);
            blendSrcAlpha = INT_BUFFER_16.get(0);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA, INT_BUFFER_16);
            blendDstAlpha = INT_BUFFER_16.get(0);
            INT_BUFFER_16.clear();
            GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC, INT_BUFFER_16);
            alphaFunc = INT_BUFFER_16.get(0);
            FLOAT_BUFFER_16.clear();
            GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF, FLOAT_BUFFER_16);
            alphaRef = FLOAT_BUFFER_16.get(0);
        }

        private void restoreGlStates() {
            GlStateManager.alphaFunc(alphaFunc, alphaRef);
            GlStateManager.tryBlendFuncSeparate(blendSrcRgb, blendDstRgb, blendSrcAlpha, blendDstAlpha);
            if (cullFace) {
                GlStateManager.enableCull();
            } else {
                GlStateManager.disableCull();
            }
            if (depthTest) {
                GlStateManager.enableDepth();
            } else {
                GlStateManager.disableDepth();
            }
            GlStateManager.shadeModel(shadeModel);
            if (alphaTest) {
                GlStateManager.enableAlpha();
            } else {
                GlStateManager.disableAlpha();
            }
            if (texture2D) {
                GlStateManager.enableTexture2D();
            } else {
                GlStateManager.disableTexture2D();
            }
            if (lighting) {
                GlStateManager.enableLighting();
            } else {
                GlStateManager.disableLighting();
            }
            if (blend) {
                GlStateManager.enableBlend();
            } else {
                GlStateManager.disableBlend();
            }
            GlStateManager.color(r, g, b, a);
            GlStateManager.bindTexture(textureID);
        }
    }

    private final GLStateBackup glStateBackup;

    private final List<IImmediateHUD> huds;
    private HUDContext context;

    private boolean enabled = false;
    private int currentIndex = 0;

    public boolean isEnabled() {
        return enabled && !huds.isEmpty();
    }

    private static final float START_X = 1f;
    private static final float START_Y = 1f;

    public InGameDebugHUDManager() {
        glStateBackup = new GLStateBackup();
        huds = new ArrayList<>();
    }

    public void lateInit() {
        context = new HUDContext(Minecraft.getMinecraft().fontRenderer, Tessellator.getInstance());
    }

    public void register(@NonNull IImmediateHUD hud) {
        Preconditions.checkNotNull(hud);

        huds.add(hud);
    }

    /**
     * Handles input and draw the HUDs via the OpenGL fixed-function pipeline if HUDs are active.
     */
    public void updateAndRenderIfNeeded() {
        handleInput();

        if (!enabled || huds.isEmpty()) {
            return;
        }

        glStateBackup.storeGlStates();
        GlStateManager.disableDepth();

        context.setPivotX(START_X);
        context.setPivotY(START_Y);
        IImmediateHUD hud = huds.get(currentIndex);
        hud.draw(context);

        Preconditions.checkState(context.getDepth() == 0,
                "The layout depth of \"%s\" must be zero after a draw update.", hud.getClass().getName());

        glStateBackup.restoreGlStates();
    }

    private boolean lastP = false;
    private boolean lastLeft = false;
    private boolean lastRight = false;

    private void handleInput() {
        if (!ctrlDown()) {
            lastP = lastLeft = lastRight = false;
            return;
        }

        boolean pDown = Keyboard.isKeyDown(Keyboard.KEY_P);
        if (pDown && !lastP) {
            enabled = !enabled;
        }
        lastP = pDown;

        if (!enabled || huds.isEmpty()) {
            lastLeft = lastRight = false;
            return;
        }

        boolean leftDown = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        if (leftDown && !lastLeft) {
            currentIndex = (currentIndex - 1 + huds.size()) % huds.size();
        }
        lastLeft = leftDown;

        boolean rightDown = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        if (rightDown && !lastRight) {
            currentIndex = (currentIndex + 1) % huds.size();
        }
        lastRight = rightDown;
    }

    private boolean ctrlDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
                Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }
}
