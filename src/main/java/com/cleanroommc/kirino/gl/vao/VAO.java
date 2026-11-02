package com.cleanroommc.kirino.gl.vao;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VAO extends GLDisposable {
    public final int vaoID;
    public final boolean dsa;

    private final AttributeLayout attributeLayout;
    private final EBOView eboView;
    private final List<VBOView> vboViews = new ArrayList<>();

    public static void bind(int vaoID) {
        GL30.glBindVertexArray(vaoID);
    }

    public void bind() {
        bind(vaoID);
    }

    private static int createVAO(boolean dsa) {
        if (dsa) {
            return GL45.glCreateVertexArrays();
        } else {
            return GL30.glGenVertexArrays();
        }
    }

    /**
     * <p>Note: This is the target-bound path.</p>
     *
     * <p>OpenGL <code>bind(0)</code> might be called on several targets depending on the nullability of the arguments,
     * and <code>bind(0)</code> will be called on <code>vao</code>.</p>
     *
     * <p><b>Suggestion</b>: Only initialize VAO via the target-bound path during the initial
     * setup or early preparation stage of each frame.</p>
     */
    public VAO(
            @NonNull AttributeLayout attributeLayout,
            @Nullable EBOView eboView,
            @NonNull VBOView @Nullable ... vboViews) {

        this(false, attributeLayout, eboView, vboViews);
    }

    /**
     * Creates and initializes a VAO using either legacy target-bound or DSA operations.
     */
    public VAO(
            boolean dsa,
            @NonNull AttributeLayout attributeLayout,
            @Nullable EBOView eboView,
            @NonNull VBOView @Nullable ... vboViews) {

        Preconditions.checkNotNull(attributeLayout);
        if (vboViews != null) {
            Preconditions.checkArgument(vboViews.length != 0, "Argument \"vboViews\" must not be empty if non-null.");
            for (VBOView vbo : vboViews) {
                Preconditions.checkNotNull(vbo);
            }
        }

        vaoID = createVAO(dsa);
        this.dsa = dsa;

        this.attributeLayout = attributeLayout;
        this.eboView = eboView;
        if (vboViews != null) {
            this.vboViews.addAll(Arrays.asList(vboViews));
        }

        if (dsa) {
            if (eboView != null) {
                GL45.glVertexArrayElementBuffer(vaoID, eboView.bufferID);
            }
            if (vboViews != null) {
                attributeLayout.upload(vaoID, vboViews);
            }
        } else {
            bind();

            if (eboView != null) {
                eboView.bind(); // ebo will be remembered
            }
            if (vboViews != null) {
                attributeLayout.upload(vboViews);
            }

            bind(0);

            if (eboView != null) {
                EBOView.bindRaw(0);
            }
            if (vboViews != null) {
                VBOView.bindRaw(0);
            }
        }

        GLResourceManager.addDisposable(this);
    }

    @Override
    public int disposePriority() {
        return 100; // earlier than vbo and ebo
    }

    @Override
    protected void dispose() {
        GL30.glDeleteVertexArrays(vaoID);
    }
}
