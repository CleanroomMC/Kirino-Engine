package com.cleanroommc.kirino.engine.render.core.resource.receipt;

import org.jspecify.annotations.NonNull;

public interface ResourceReceipt<T extends IResourceReceipt<T>> {
    @NonNull
    T getReceipt();
}
