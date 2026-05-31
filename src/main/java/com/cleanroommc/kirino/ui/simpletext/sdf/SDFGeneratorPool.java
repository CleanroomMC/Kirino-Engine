package com.cleanroommc.kirino.ui.simpletext.sdf;

import com.cleanroommc.kirino.utils.ForkJoinPoolUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public final class SDFGeneratorPool {

    private final BlockingQueue<SDFGenerator> generators;

    public SDFGeneratorPool(int count, @NonNull Supplier<SDFGenerator> factory) {
        Preconditions.checkArgument(count > 0,
                "Argument \"count\" must be greater than 0.");
        Preconditions.checkNotNull(factory);

        generators = new ArrayBlockingQueue<>(count);

        for (int i = 0; i < count; i++) {
            generators.add(Preconditions.checkNotNull(factory.get()));
        }
    }

    @NonNull
    public SDFGenerator acquire() {
        return ForkJoinPoolUtils.managedTake(generators);
    }

    public void release(@NonNull SDFGenerator generator) {
        Preconditions.checkNotNull(generator);
        Preconditions.checkState(generators.offer(generator), "SDFGeneratorPool overflow.");
    }
}