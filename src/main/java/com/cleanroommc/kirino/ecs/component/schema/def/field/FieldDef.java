package com.cleanroommc.kirino.ecs.component.schema.def.field;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class FieldDef {
    public final FieldKind fieldKind;
    public final ScalarType scalarType;
    public final String structTypeName;

    public FieldDef(@NonNull ScalarType scalarType) {
        Preconditions.checkNotNull(scalarType);

        fieldKind = FieldKind.SCALAR;
        this.scalarType = scalarType;
        structTypeName = null;
    }

    public FieldDef(@NonNull String structTypeName) {
        Preconditions.checkNotNull(structTypeName);

        fieldKind = FieldKind.STRUCT;
        scalarType = null;
        this.structTypeName = structTypeName;
    }

    public String toString(@NonNull StructRegistry structRegistry) {
        Preconditions.checkNotNull(structRegistry);

        if (fieldKind == FieldKind.SCALAR) {
            return "FieldDef{ scalarType=" + scalarType + " }";
        } else if (fieldKind == FieldKind.STRUCT) {
            StructDef structDef = structRegistry.getStructDef(structTypeName);
            if (structDef == null) {
                throw new IllegalStateException("Struct type " + structTypeName + " doesn't exist.");
            }
            return "FieldDef{ structType=(" + structTypeName + ")" + structDef.toString(structRegistry) + " }";
        }
        return "";
    }
}
