package com.cleanroommc.kirino.engine.render.core.resource.receipt;

import org.jspecify.annotations.NonNull;

public interface ResourceReceipt<T extends ResourceReceipt<T>> {
    @NonNull
    T getReceipt();
}
