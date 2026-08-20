package com.cleanroommc.kirino.ecs.component.schema.meta;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MemberLayout {
    public final ImmutableList<String> fieldNames;

    public MemberLayout(@NonNull List<@NonNull String> fieldNames) {
        Preconditions.checkNotNull(fieldNames);
        for (String fieldName : fieldNames) {
            Preconditions.checkNotNull(fieldName);
        }

        this.fieldNames = ImmutableList.copyOf(fieldNames);
    }

    public MemberLayout(@NonNull String @NonNull ... fieldNames) {
        Preconditions.checkNotNull(fieldNames);
        for (String fieldName : fieldNames) {
            Preconditions.checkNotNull(fieldName);
        }

        this.fieldNames = ImmutableList.copyOf(fieldNames);
    }
}
