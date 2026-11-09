package com.cleanroommc.kirino.ecs;

import com.google.common.base.Preconditions;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import org.jspecify.annotations.NonNull;

import java.util.List;

final class ComponentDesc {
    public final String name;
    public final ObjectArrayView<FieldDef> fields;
    public final ObjectArrayView<String> fieldTypeNames;

    ComponentDesc(String name, List<FieldDef> fields, String... fieldTypeNames) {
        this.name = name;
        this.fields = ObjectArrayView.copyOf(fields, FieldDef[]::new);
        this.fieldTypeNames = ObjectArrayView.copyOf(fieldTypeNames);
    }

    ComponentDesc(String name, List<FieldDef> fields, List<String> fieldTypeNames) {
        this.name = name;
        this.fields = ObjectArrayView.copyOf(fields, FieldDef[]::new);
        this.fieldTypeNames = ObjectArrayView.copyOf(fieldTypeNames, String[]::new);
    }

    public String toString(@NonNull StructRegistry structRegistry) {
        Preconditions.checkNotNull(structRegistry);

        StringBuilder builder = new StringBuilder();
        builder.append("ComponentDesc{ name=").append(name).append(", fields=");
        FieldDef[] definitions = fields.elements();
        for (int i = 0; i < definitions.length; i++) {
            FieldDef field = definitions[i];
            if (i == definitions.length - 1){
                builder.append(field.toString(structRegistry));
            } else {
                builder.append(field.toString(structRegistry)).append(", ");
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
