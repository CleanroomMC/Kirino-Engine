package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A {@link ScalarType} is not by definition a scalar but more like built-in primitive types.
 * The opposite concept is a struct ({@link StructDef}).
 */
public enum ScalarType {
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOL,
    VEC2("x", "y"),
    VEC3("x", "y", "z"),
    VEC4("x", "y", "z", "w"),
    MAT3("m00", "m01", "m02",
                "m10", "m11", "m12",
                "m20", "m21", "m22"),
    MAT4("m00", "m01", "m02", "m03",
                "m10", "m11", "m12", "m13",
                "m20", "m21", "m22", "m23",
                "m30", "m31", "m32", "m33");

    public final String[] fieldNames;
    private final Integer[] ordinals;

    ScalarType(String... fields) {
        if (fields.length == 0) {
            this.fieldNames = null;
            this.ordinals = null;
            return;
        }

        String[] tmpFieldNames = fields.clone();
        Integer[] tmpOrdinals = new Integer[fields.length];

        for (int i = 0; i < tmpOrdinals.length; i++) {
            tmpOrdinals[i] = i;
        }

        // This will be useful later
        Arrays.sort(tmpOrdinals, Comparator.comparing(lhs -> tmpFieldNames[lhs]));
        Arrays.sort(tmpFieldNames);

        this.fieldNames = tmpFieldNames;
        this.ordinals = tmpOrdinals;
    }

    public int ordinalOffsetOfField(@NonNull String field) {
        Preconditions.checkNotNull(field);

        if (ordinals == null) {
            return 1;
        }

        return ordinals[Arrays.binarySearch(this.fieldNames, field)]; // For larger matrices it will be an improvement, hopefully.
    }
}
