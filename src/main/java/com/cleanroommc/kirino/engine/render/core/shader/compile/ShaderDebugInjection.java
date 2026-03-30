package com.cleanroommc.kirino.engine.render.core.shader.compile;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public final class ShaderDebugInjection {
    private ShaderDebugInjection() {
    }

    public static final int VEC3F_DEBUG = 1;
    public static final int INVOCATION_LIMIT = 1 << 1;
    public static final int SPECIFIED_INVOCATION = 1 << 2;
    public static final int STAGE_DEBUG = 1 << 3;
    public static final int IMAGE_DEBUG = 1 << 4;

    private static final int ALL_FLAGS =
            VEC3F_DEBUG
            | INVOCATION_LIMIT
            | SPECIFIED_INVOCATION
            | STAGE_DEBUG
            | IMAGE_DEBUG;

    public enum Type {
        VEC3F_DEBUG,
        INVOCATION_LIMIT,
        SPECIFIED_INVOCATION,
        STAGE_DEBUG,
        IMAGE_DEBUG,
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

        if ((flags & VEC3F_DEBUG) != 0) {
            result.add(Type.VEC3F_DEBUG);
        }

        if ((flags & INVOCATION_LIMIT) != 0) {
            result.add(Type.INVOCATION_LIMIT);
        }

        if ((flags & SPECIFIED_INVOCATION) != 0) {
            result.add(Type.SPECIFIED_INVOCATION);
        }

        if ((flags & STAGE_DEBUG) != 0) {
            result.add(Type.STAGE_DEBUG);
        }

        if ((flags & IMAGE_DEBUG) != 0) {
            result.add(Type.IMAGE_DEBUG);
        }

        return result;
    }

    public static Map<String, String> resolveDebugRemap(Set<String> debugRemapFields) {
        Map<String, String> result = new HashMap<>();

        for (String field : debugRemapFields) {
            switch (field) {
                case ShaderDebugSnippet.REMAP_FIELD_COUNTER_BINDING -> result.put(field, String.valueOf(KirinoClientCore.GL_DEVICE_INFO.getMaxSsboBlocksCompute() - 1));
                case ShaderDebugSnippet.REMAP_FIELD_MAX_COUNTER -> result.put(field, String.valueOf(1024));
                case ShaderDebugSnippet.REMAP_FIELD_VEC3F_RECORD_BINDING -> result.put(field, String.valueOf(KirinoClientCore.GL_DEVICE_INFO.getMaxSsboBlocksCompute() - 2));
                case ShaderDebugSnippet.REMAP_FIELD_MAX_VEC3F_RECORD -> result.put(field, String.valueOf(4096));
            }
        }

        return result;
    }

    /**
     * Inject the content after comments and the <code>#version</code> line if they existed.
     */
    public static String inject(String shaderSource, String content) {
        int insertPos = findVersionInsertPos(shaderSource);
        return shaderSource.substring(0, insertPos) + content + shaderSource.substring(insertPos);
    }

    /**
     * Inject the debug infrastructure.
     *
     * @see #inject(String, String)
     */
    public static String injectDebugInfra(String shaderSource, List<Type> debugTypes, Set<String> outDebugRemapFields) {
        outDebugRemapFields.clear();

        StringBuilder infra = new StringBuilder();
        infra.append("\n// ===== Kirino Debug Infra Begin =====\n");

        Set<String> glslSet = new HashSet<>();
        glslSet.add(ShaderDebugSnippet.KIRINO_DEBUG_INVALIDITY);

        for (Type type : debugTypes) {
            switch (type) {
                case VEC3F_DEBUG -> {
                    glslSet.add(ShaderDebugSnippet.KIRINO_DEBUG_COUNTER);
                    glslSet.add(ShaderDebugSnippet.KIRINO_DEBUG_VEC3F_RECORD);
                    outDebugRemapFields.addAll(ShaderDebugSnippet.REMAP_KIRINO_DEBUG_COUNTER);
                    outDebugRemapFields.addAll(ShaderDebugSnippet.REMAP_KIRINO_DEBUG_VEC3F_RECORD);
                }
                // todo: other cases
            }
        }

        for (String glsl : new ArrayList<>(glslSet).stream().sorted().toArray(String[]::new)) {
            infra.append("\n").append(MinecraftResourceUtils.readText(
                    new ResourceLocation("forge:shaders/debug/lowlevel/" + glsl),
                    MinecraftResourceUtils.NewLineType.BACK_SLASH_N)).append("\n");
        }

        infra.append("// ===== Kirino Debug Infra End =====\n\n");

        return inject(shaderSource, infra.toString());
    }

    private static int findVersionInsertPos(String source) {
        int i = 0;
        int len = source.length();

        // bom header
        if (len >= 1 && source.charAt(0) == '\uFEFF') {
            i = 1;
        }

        while (i < len) {
            while (i < len && Character.isWhitespace(source.charAt(i))) {
                i++;
            }

            if (i >= len) {
                break;
            }

            if (i + 1 < len && source.charAt(i) == '/' && source.charAt(i + 1) == '/') {
                i += 2;
                while (i < len && source.charAt(i) != '\n') {
                    i++;
                }
                continue;
            }

            if (i + 1 < len && source.charAt(i) == '/' && source.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < len && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
                    i++;
                }
                i += 2;
                continue;
            }

            if (source.startsWith("#version", i)) {
                int lineEnd = i;
                while (lineEnd < len && source.charAt(lineEnd) != '\n') {
                    lineEnd++;
                }
                return (lineEnd < len) ? lineEnd + 1 : len;
            }

            break;
        }

        return 0;
    }
}
