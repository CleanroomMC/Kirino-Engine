package com.cleanroommc.kirino.ecs;

import java.util.concurrent.CompletableFuture;

final class ObservedCompletion extends CompletableFuture<Void> {
    private final CompletableFuture<Void> actual;

    ObservedCompletion(CompletableFuture<Void> actual) {
        this.actual = actual;
        actual.whenComplete((value, failure) -> {
            if (failure == null) {
                complete(null);
            } else {
                completeExceptionally(failure);
            }
        });
    }

    static CompletableFuture<Void> actual(CompletableFuture<Void> future) {
        return future instanceof ObservedCompletion observed ? observed.actual : future;
    }
}
