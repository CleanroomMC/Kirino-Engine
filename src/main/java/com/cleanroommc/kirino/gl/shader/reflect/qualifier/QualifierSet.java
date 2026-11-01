package com.cleanroommc.kirino.gl.shader.reflect.qualifier;

import com.cleanroommc.kirino.gl.shader.reflect.type.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record QualifierSet(
        @Nullable Precision precision,
        @Nullable Interpolation interpolation,
        @Nullable Sampling sampling,
        boolean patch,
        boolean invariant,
        boolean precise,
        @NonNull ImmutableSet<MemoryQualifier> memory,
        @NonNull ImmutableList<String> extensions) {

    public static final QualifierSet EMPTY = new QualifierSet(
            null,
            null,
            null,
            false,
            false,
            false,
            ImmutableSet.of(),
            ImmutableList.of());

    public QualifierSet {
        Preconditions.checkNotNull(memory);
        Preconditions.checkNotNull(extensions);
    }

    @NonNull
    public QualifierSet overlay(@NonNull QualifierSet other) {
        Preconditions.checkNotNull(other);

        return new QualifierSet(
                other.precision != null ? other.precision : precision,
                other.interpolation != null ? other.interpolation : interpolation,
                other.sampling != null ? other.sampling : sampling,
                patch || other.patch,
                invariant || other.invariant,
                precise || other.precise,
                ImmutableSet.<MemoryQualifier>builder().addAll(memory).addAll(other.memory).build(),
                ImmutableList.<String>builder().addAll(extensions).addAll(other.extensions).build());
    }

    @NonNull
    public static QualifierSet resolve(
            @NonNull Iterable<String> qualifiers,
            @NonNull GlslType type,
            @NonNull Map<String, Precision> defaults) {

        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(defaults);

        while (type instanceof ArrayType array) {
            type = array.elementType();
        }

        GlslType base = type instanceof VectorType vector
                ? vector.componentType()
                : type instanceof MatrixType matrix ? matrix.componentType() : type;

        Precision precision = null;
        if (base == ScalarType.FLOAT || base == ScalarType.INT || base == ScalarType.UINT) {
            precision = defaults.getOrDefault(base == ScalarType.FLOAT ? "float" : "int", Precision.HIGHP);
        } else if (base instanceof SamplerType || base instanceof ImageType || base instanceof AtomicCounterType) {
            precision = defaults.getOrDefault(TypeRegistry.builtinName(base).orElseThrow(), Precision.HIGHP);
        }

        return new QualifierSet(
                precision,
                null,
                null,
                false,
                false,
                false,
                ImmutableSet.of(),
                ImmutableList.of()).overlay(from(qualifiers));
    }

    @NonNull
    public static QualifierSet from(@NonNull Iterable<String> qualifiers) {
        Preconditions.checkNotNull(qualifiers);

        Precision precision = null;
        Interpolation interpolation = null;
        Sampling sampling = null;
        boolean patch = false;
        boolean invariant = false;
        boolean precise = false;
        ImmutableSet.Builder<MemoryQualifier> memory = ImmutableSet.builder();
        ImmutableList.Builder<String> extensions = ImmutableList.builder();
        for (String qualifier : qualifiers) {
            Preconditions.checkNotNull(qualifier);

            switch (qualifier) {
                case "highp" -> precision = Precision.HIGHP;
                case "mediump" -> precision = Precision.MEDIUMP;
                case "lowp" -> precision = Precision.LOWP;
                case "smooth" -> interpolation = Interpolation.SMOOTH;
                case "flat" -> interpolation = Interpolation.FLAT;
                case "noperspective" -> interpolation = Interpolation.NOPERSPECTIVE;
                case "centroid" -> sampling = Sampling.CENTROID;
                case "sample" -> sampling = Sampling.SAMPLE;
                case "patch" -> patch = true;
                case "invariant" -> invariant = true;
                case "precise" -> precise = true;
                case "coherent" -> memory.add(MemoryQualifier.COHERENT);
                case "volatile" -> memory.add(MemoryQualifier.VOLATILE, MemoryQualifier.COHERENT);
                case "restrict" -> memory.add(MemoryQualifier.RESTRICT);
                case "readonly" -> memory.add(MemoryQualifier.READONLY);
                case "writeonly" -> memory.add(MemoryQualifier.WRITEONLY);
                case "const",
                     "in",
                     "out",
                     "inout",
                     "uniform",
                     "buffer",
                     "shared",
                     "attribute",
                     "varying",
                     "subroutine" -> {
                }
                default -> extensions.add(qualifier);
            }
        }

        QualifierSet result = new QualifierSet(
                precision,
                interpolation,
                sampling,
                patch,
                invariant,
                precise,
                memory.build(),
                extensions.build());

        return result.equals(EMPTY) ? EMPTY : result;
    }
}
