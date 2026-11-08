package com.cleanroommc.kirino.ecs.component.schema.meta;

import com.google.common.base.Preconditions;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MemberLayout {
    public final ObjectArrayView<String> fieldNames;

    public MemberLayout(@NonNull List<@NonNull String> fieldNames) {
        Preconditions.checkNotNull(fieldNames);
        for (String fieldName : fieldNames) {
            Preconditions.checkNotNull(fieldName);
        }

        this.fieldNames = ObjectArrayView.copyOf(fieldNames, String[]::new);
    }

    public MemberLayout(@NonNull String @NonNull ... fieldNames) {
        Preconditions.checkNotNull(fieldNames);
        for (String fieldName : fieldNames) {
            Preconditions.checkNotNull(fieldName);
        }

        this.fieldNames = ObjectArrayView.copyOf(fieldNames);
    }
}
