package com.cleanroommc.kirino.gl.texture.accessor;

import com.google.common.base.Preconditions;

public record StorageOptions(boolean mutable, int levels) {

    public static final StorageOptions MUTABLE_BASE_ONLY = new StorageOptions(true, 1);
    public static final StorageOptions IMMUTABLE_BASE_ONLY = new StorageOptions(false, 1);

    public StorageOptions {
        Preconditions.checkArgument(levels >= 1,
                "Argument \"levels\"=%s must be at least 1.", levels);
    }
}
