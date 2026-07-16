package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.RuntimeShaderBundleInit
 */
public final class RuntimeShaderBundle {

    private final ResourceLayout resourceLayout;

    // key: id
    // value: collision count
    private final Map<String, Integer> collision = new HashMap<>();

    private String shaders2id(String[] shaders) {
        return String.valueOf(String.join(",", shaders).hashCode());
    }

    @NonNull
    private ResourceSlot<ShaderProgram> newShaderProgram(@NonNull String name, @NonNull String @NonNull [] shaders) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(shaders);
        for (String shader : shaders) {
            Preconditions.checkNotNull(shader);
        }

        String id = shaders2id(shaders);
        String finalId = name.toLowerCase(Locale.ROOT).replace(' ', '_') + "_" + id;

        int index = collision.compute(finalId, (k, v) -> v == null ? 0 : v + 1);

        return resourceLayout.slot(ShaderProgram.class, finalId + "_" + index);
    }

    public RuntimeShaderBundle(@NonNull ResourceLayout resourceLayout) {
        Preconditions.checkNotNull(resourceLayout);

        this.resourceLayout = resourceLayout;
    }
}
