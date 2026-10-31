package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class BuiltinTypes {

    static final ImmutableMap<String, GlslType> TYPES = create();

    private BuiltinTypes() {
    }

    private static ImmutableMap<String, GlslType> create() {
        Map<String, GlslType> types = new LinkedHashMap<>();
        types.put("void", VoidType.INSTANCE);
        addNumeric(types, ScalarType.BOOL, "bvec", null);
        addNumeric(types, ScalarType.INT, "ivec", null);
        addNumeric(types, ScalarType.UINT, "uvec", null);
        addNumeric(types, ScalarType.FLOAT, "vec", "mat");
        addNumeric(types, ScalarType.DOUBLE, "dvec", "dmat");
        types.put("atomic_uint", AtomicCounterType.INSTANCE);

        addTexture(types, "1D", TextureDimension.D1, false, false, true);
        addTexture(types, "2D", TextureDimension.D2, false, false, true);
        addTexture(types, "3D", TextureDimension.D3, false, false, false);
        addTexture(types, "Cube", TextureDimension.CUBE, false, false, true);
        addTexture(types, "2DRect", TextureDimension.RECTANGLE, false, false, true);
        addTexture(types, "1DArray", TextureDimension.D1, true, false, true);
        addTexture(types, "2DArray", TextureDimension.D2, true, false, true);
        addTexture(types, "CubeArray", TextureDimension.CUBE, true, false, true);
        addTexture(types, "Buffer", TextureDimension.BUFFER, false, false, false);
        addTexture(types, "2DMS", TextureDimension.D2, false, true, false);
        addTexture(types, "2DMSArray", TextureDimension.D2, true, true, false);
        return ImmutableMap.copyOf(types);
    }

    private static void addNumeric(
            @NonNull Map<String, GlslType> types,
            @NonNull ScalarType scalar,
            @NonNull String vectorPrefix,
            @Nullable String matrixPrefix) {

        types.put(scalar.name().toLowerCase(Locale.ROOT), scalar);
        for (int size = 2; size <= 4; size++) {
            types.put(vectorPrefix + size, new VectorType(scalar, size));
        }

        if (matrixPrefix != null) {
            for (int columns = 2; columns <= 4; columns++) {
                for (int rows = 2; rows <= 4; rows++) {
                    MatrixType matrix = new MatrixType(scalar, columns, rows);
                    types.put(matrixPrefix + columns + "x" + rows, matrix);
                    if (columns == rows) {
                        types.put(matrixPrefix + columns, matrix);
                    }
                }
            }
        }
    }

    private static void addTexture(
            @NonNull Map<String, GlslType> types,
            @NonNull String suffix,
            @NonNull TextureDimension dimension,
            boolean arrayed,
            boolean multisample,
            boolean shadow) {

        addTexture(types, "", suffix, ScalarType.FLOAT, dimension, arrayed, multisample);
        addTexture(types, "i", suffix, ScalarType.INT, dimension, arrayed, multisample);
        addTexture(types, "u", suffix, ScalarType.UINT, dimension, arrayed, multisample);
        if (shadow) {
            types.put("sampler" + suffix + "Shadow", new SamplerType(ScalarType.FLOAT, dimension, arrayed, multisample, true));
        }
    }

    private static void addTexture(
            @NonNull Map<String, GlslType> types,
            @NonNull String prefix,
            @NonNull String suffix,
            @NonNull ScalarType sampledType,
            @NonNull TextureDimension dimension,
            boolean arrayed,
            boolean multisample) {

        types.put(prefix + "sampler" + suffix, new SamplerType(sampledType, dimension, arrayed, multisample, false));
        types.put(prefix + "image" + suffix, new ImageType(sampledType, dimension, arrayed, multisample));
    }
}
