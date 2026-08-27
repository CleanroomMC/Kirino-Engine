package com.cleanroommc.kirino;

import org.jspecify.annotations.NonNull;

/**
 * Simply an alias of {@link ImmediateClientServices}.
 */
public final class ICS {

    private ICS() {
    }

    /**
     * <p>Note: Must only use it on the GL thread.</p>
     */
    @NonNull
    public static ImmediateClientServices instance() {
        return ImmediateClientServices.instance();
    }
}
