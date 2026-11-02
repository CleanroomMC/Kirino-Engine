package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record AtomicCounterBuffer(
        int index,
        int binding,
        long dataSize,
        @NonNull ImmutableList<AtomicCounter> counters) {

    public AtomicCounterBuffer {
        Preconditions.checkNotNull(counters);
    }

    @NonNull
    public Optional<AtomicCounter> counter(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return counters
                .stream()
                .filter(counter -> counter.name().equals(name) || counter.name().equals(name + "[0]"))
                .findFirst();
    }
}
