package com.cleanroommc.kirino.ecs;

import com.google.common.base.Preconditions;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The opposite concept is a scalar ({@link ScalarType}).
 */
final class StructDef {
    public final ObjectArrayView<FieldDef> fields;

    public StructDef(@NonNull List<@NonNull FieldDef> fields) {
        Preconditions.checkNotNull(fields);
        for (FieldDef field : fields) {
            Preconditions.checkNotNull(field);
        }

        this.fields = ObjectArrayView.copyOf(fields, FieldDef[]::new);
    }

    public StructDef(@NonNull FieldDef @NonNull ... fields) {
        Preconditions.checkNotNull(fields);
        for (FieldDef field : fields) {
            Preconditions.checkNotNull(field);
        }

        this.fields = ObjectArrayView.copyOf(fields);
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
