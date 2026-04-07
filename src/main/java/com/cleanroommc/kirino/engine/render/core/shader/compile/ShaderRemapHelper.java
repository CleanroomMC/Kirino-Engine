package com.cleanroommc.kirino.engine.render.core.shader.compile;

import java.util.Map;

public final class ShaderRemapHelper {
    private ShaderRemapHelper() {
    }

    public static String remap(String shaderSource, Map<String, String> remap) {
        int len = shaderSource.length();
        StringBuilder out = new StringBuilder(len + 64);

        for (int i = 0; i < len; i++) {
            char c = shaderSource.charAt(i);

            if (c == '{') {
                int start = i + 1;
                int end = start;

                while (end < len && shaderSource.charAt(end) != '}') {
                    end++;
                }

                if (end < len) {
                    String key = shaderSource.substring(start, end);
                    String value = remap.get(key);

                    if (value != null) {
                        out.append(value);
                    } else {
                        out.append('{').append(key).append('}');
                    }

                    i = end;
                    continue;
                }
            }

            out.append(c);
        }

        return out.toString();
    }
}
