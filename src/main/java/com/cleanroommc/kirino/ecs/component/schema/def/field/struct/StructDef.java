package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.google.common.base.Preconditions;
import com.cleanroommc.kirino.utils.collections.ImmutableObjectArrayList;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The opposite concept is a scalar ({@link ScalarType}).
 */
public final class StructDef {
    public final ImmutableObjectArrayList<FieldDef> fields;

    public StructDef(@NonNull List<@NonNull FieldDef> fields) {
        Preconditions.checkNotNull(fields);
        for (FieldDef field : fields) {
            Preconditions.checkNotNull(field);
        }

        this.fields = ImmutableObjectArrayList.copyOf(fields, FieldDef[]::new);
    }

    public StructDef(@NonNull FieldDef @NonNull ... fields) {
        Preconditions.checkNotNull(fields);
        for (FieldDef field : fields) {
            Preconditions.checkNotNull(field);
        }

        this.fields = ImmutableObjectArrayList.copyOf(fields);
    }

    public String toString(@NonNull StructRegistry structRegistry) {
        Preconditions.checkNotNull(structRegistry);

        StringBuilder builder = new StringBuilder();
        builder.append("StructDef{ ");
        FieldDef[] definitions = fields.elements();
        for (int i = 0; i < definitions.length; i++) {
            FieldDef field = definitions[i];
            if (i == definitions.length - 1) {
                builder.append(field.toString(structRegistry));
            } else {
                builder.append(field.toString(structRegistry)).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
