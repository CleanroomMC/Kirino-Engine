package com.cleanroommc.kirino.gl.vao;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VAO extends GLDisposable {
    public final int vaoID;

    private final AttributeLayout attributeLayout;
    private final EBOView eboView;
    private final List<VBOView> vboViews = new ArrayList<>();

    public static void bind(int vaoID) {
        GL30.glBindVertexArray(vaoID);
    }

    public void bind() {
        bind(vaoID);
    }

    /**
     * OpenGL <code>bind(0)</code> might be called on several targets depending on the nullity of the arguments,
     * and <code>bind(0)</code> will be called on <code>vao</code>.
     *
     * <p>Only initialize VAO during the initial setup or early preparation stage of each frame.</p>
     *
     * @param attributeLayout Must pass an attribute layout, even an empty one
     * @param eboView The nullable ebo (<code>eboView</code> and <code>vboViews</code> must both be null if one is null)
     * @param vboViews The nullable vbos (<code>eboView</code> and <code>vboViews</code> must both be null if one is null; <code>vboViews</code> can't be empty if non-null)
     */
    public VAO(@NonNull AttributeLayout attributeLayout, @Nullable EBOView eboView, @NonNull VBOView @Nullable ... vboViews) {
        Preconditions.checkNotNull(attributeLayout);
        if (vboViews != null) {
            Preconditions.checkArgument(vboViews.length != 0, "Argument \"vboViews\" must not be empty if non-null.");
            for (VBOView vbo : vboViews) {
                Preconditions.checkNotNull(vbo);
            }
        }
        if (eboView == null || vboViews == null) {
            Preconditions.checkArgument(eboView == null, "Argument \"eboView\" must be null when \"vboViews\" is null.");
            Preconditions.checkArgument(vboViews == null, "Argument \"vboViews\" must be null when \"eboView\" is null.");
        }

        vaoID = GL30.glGenVertexArrays();

        this.attributeLayout = attributeLayout;
        this.eboView = eboView;
        if (vboViews != null) {
            this.vboViews.addAll(Arrays.asList(vboViews));
        }

        bind();

        if (eboView != null) {
            eboView.bind(); // ebo will be remembered
        }
        if (vboViews != null) {
            attributeLayout.upload(vboViews);
        }

        bind(0);

        if (eboView != null) {
            eboView.bind(0);
        }
        if (vboViews != null) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        GLResourceManager.addDisposable(this);
    }

    @Override
    public int disposePriority() {
        return 100; // earlier than vbo and ebo
    }

    @Override
    public void dispose() {
        GL30.glDeleteVertexArrays(vaoID);
    }
}
