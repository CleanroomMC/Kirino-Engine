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

        switch (fieldKind) {
            case SCALAR -> {
                return "FieldDef{ scalarType=" + scalarType + " }";
            }
            case STRUCT -> {
                StructDef structDef = structRegistry.getStructDef(structTypeName);
                if (structDef == null) {
                    throw new IllegalStateException("Struct type " + structTypeName + " doesn't exist.");
                }
                return "FieldDef{ structType=(" + structTypeName + ")" + structDef.toString(structRegistry) + " }";
            }
            case null, default -> {
                return "";
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 31 + fieldKind.hashCode();
        hash = 31 * hash + (scalarType == null ? 0 : scalarType.hashCode());
        return 31 * hash + (structTypeName == null ? 0 : structTypeName.hashCode());
    }
}
