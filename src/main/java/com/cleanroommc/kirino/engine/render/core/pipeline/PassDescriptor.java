package com.cleanroommc.kirino.engine.render.core.pipeline;

import com.cleanroommc.kirino.engine.render.core.pipeline.pass.RenderPass;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class PassDescriptor {

    public enum Availability {
        AVAILABLE,
        NOT_IMPLEMENTED,
        DEP_UNSATISFIED
    }

    private final RenderPass pass;
    private final Availability availability;
    private final String unavailableReason;

    /**
     * It registers a pass with availability conditions.
     *
     * @param availability The availability is only about the internal implementation of a pass.
     *                     Like, whether the implementation has implicit dependencies or is unfinished
     */
    public PassDescriptor(
            @NonNull RenderPass pass,
            @NonNull Availability availability,
            @NonNull String unavailableReason) {

        Preconditions.checkNotNull(pass);
        Preconditions.checkNotNull(availability);
        Preconditions.checkNotNull(unavailableReason);

        this.pass = pass;
        this.availability = availability;
        this.unavailableReason = unavailableReason;
    }

    /**
     * It registers an <i><b>available</b></i> pass.
     */
    public PassDescriptor(@NonNull RenderPass pass) {
        Preconditions.checkNotNull(pass);

        this.pass = pass;
        this.availability = Availability.AVAILABLE;
        this.unavailableReason = "";
    }

    @NonNull
    public RenderPass acquire() {
        if (availability == Availability.AVAILABLE) {
            return pass;
        }

        throw new IllegalStateException(String.format(
                "Failed to acquire the RenderPass \"%s\". Reason [%s]: %s",
                pass.passName,
                availability.toString(),
                unavailableReason));
    }
}
