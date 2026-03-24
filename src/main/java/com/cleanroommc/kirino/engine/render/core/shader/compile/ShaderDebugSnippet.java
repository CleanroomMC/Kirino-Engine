package com.cleanroommc.kirino.engine.render.core.shader.compile;

import com.google.common.collect.ImmutableList;

public final class ShaderDebugSnippet {
    private ShaderDebugSnippet() {
    }

    public static final String KIRINO_DEBUG_INVALIDITY = "1_kirino_debug_invalidity.glsl";
    public static final String KIRINO_DEBUG_COUNTER = "2_kirino_debug_counter.glsl";
    public static final String KIRINO_DEBUG_VEC3F_RECORD = "2_kirino_debug_vec3f_record.glsl";

    public static final String REMAP_FIELD_COUNTER_BINDING = "counter_binding";
    public static final String REMAP_FIELD_MAX_COUNTER = "max_counter";
    public static final String REMAP_FIELD_VEC3F_RECORD_BINDING = "vec3f_record_binding";
    public static final String REMAP_FIELD_MAX_VEC3F_RECORD = "max_vec3f_record";

    public static final ImmutableList<String> REMAP_KIRINO_DEBUG_COUNTER = ImmutableList.of(
            REMAP_FIELD_COUNTER_BINDING,
            REMAP_FIELD_MAX_COUNTER);

    public static final ImmutableList<String> REMAP_KIRINO_DEBUG_VEC3F_RECORD = ImmutableList.of(
            REMAP_FIELD_VEC3F_RECORD_BINDING,
            REMAP_FIELD_MAX_VEC3F_RECORD);
}
