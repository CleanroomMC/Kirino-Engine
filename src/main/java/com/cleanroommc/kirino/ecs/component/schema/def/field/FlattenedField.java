package com.cleanroommc.kirino.ecs.component.schema.def.field;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.FlattenedScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.SchemaLayout;
import com.google.common.collect.ImmutableList;

import java.util.List;

public final class FlattenedField {
    public final ImmutableList<FlattenedScalarType> scalarTypes;

    private static List<FlattenedScalarType> makeFlattenedField(FieldDef fieldDef, StructRegistry structRegistry) {
        if (fieldDef.fieldKind == FieldKind.SCALAR) {
            return FlattenedScalarType.flatten(fieldDef.scalarType);
        }
        return structRegistry.getLayout(fieldDef.structTypeName).columns()
            .stream().map(SchemaLayout.Column::type).toList();
    }

    public int getUnitCount() {
        return scalarTypes.size();
    }

    FlattenedField(FieldDef fieldDef, StructRegistry structRegistry) {
        scalarTypes = ImmutableList.copyOf(makeFlattenedField(fieldDef, structRegistry));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FlattenedField{ ");
        for (int i = 0; i < scalarTypes.size(); i++) {
            FlattenedScalarType scalarType = scalarTypes.get(i);
            if (i == scalarTypes.size() - 1) {
                builder.append(scalarType.toString());
            } else {
                builder.append(scalarType.toString()).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
