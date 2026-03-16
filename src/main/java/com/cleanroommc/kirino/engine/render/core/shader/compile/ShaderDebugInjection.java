package com.cleanroommc.kirino.engine.render.core.shader.compile;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class ShaderDebugInjection {

    public static final int COMPUTE_FRAME_DEBUG_VEC3F = 1;
    public static final int COMPUTE_INVOCATION_LIMIT = 1 << 1;
    public static final int COMPUTE_SPECIFIED_INVOCATION = 1 << 2;
    public static final int COMPUTE_STAGE_DEBUG = 1 << 3;
    public static final int COMPUTE_IMAGE_DEBUG = 1 << 4;
    public static final int VERTEX_FRAME_DEBUG_VEC3F = 1 << 5;
    public static final int VERTEX_STAGE_DEBUG = 1 << 6;

    private static final int ALL_FLAGS =
            COMPUTE_FRAME_DEBUG_VEC3F
            | COMPUTE_INVOCATION_LIMIT
            | COMPUTE_SPECIFIED_INVOCATION
            | COMPUTE_STAGE_DEBUG
            | COMPUTE_IMAGE_DEBUG
            | VERTEX_FRAME_DEBUG_VEC3F
            | VERTEX_STAGE_DEBUG;

    public enum Type {
        COMPUTE_FRAME_DEBUG_VEC3F,
        COMPUTE_INVOCATION_LIMIT,
        COMPUTE_SPECIFIED_INVOCATION,
        COMPUTE_STAGE_DEBUG,
        COMPUTE_IMAGE_DEBUG,
        VERTEX_FRAME_DEBUG_VEC3F,
        VERTEX_STAGE_DEBUG
    }

    public static List<Type> parse(int flags) {
        Preconditions.checkArgument((flags & ~ALL_FLAGS) == 0,
                "Unknown ShaderDebugInjection flags 0x%s with invalid bits 0x%s.",
                Integer.toHexString(flags),
                Integer.toHexString(flags & ~ALL_FLAGS));

        int size = Integer.bitCount(flags);
        List<Type> result = new ArrayList<>(size);
        if (size == 0) {
            return result;
        }

        if ((flags & COMPUTE_FRAME_DEBUG_VEC3F) != 0) {
            result.add(Type.COMPUTE_FRAME_DEBUG_VEC3F);
        }

        if ((flags & COMPUTE_INVOCATION_LIMIT) != 0) {
            result.add(Type.COMPUTE_INVOCATION_LIMIT);
        }

        if ((flags & COMPUTE_SPECIFIED_INVOCATION) != 0) {
            result.add(Type.COMPUTE_SPECIFIED_INVOCATION);
        }

        if ((flags & COMPUTE_STAGE_DEBUG) != 0) {
            result.add(Type.COMPUTE_STAGE_DEBUG);
        }

        if ((flags & COMPUTE_IMAGE_DEBUG) != 0) {
            result.add(Type.COMPUTE_IMAGE_DEBUG);
        }

        if ((flags & VERTEX_FRAME_DEBUG_VEC3F) != 0) {
            result.add(Type.VERTEX_FRAME_DEBUG_VEC3F);
        }

        if ((flags & VERTEX_STAGE_DEBUG) != 0) {
            result.add(Type.VERTEX_STAGE_DEBUG);
        }

        return result;
    }
}
