package com.cleanroommc.kirino.engine.render.core.debug.data;

public interface DebugDataService {
    /**
     * A service must be active to therefore get a non-null instance from {@link DebugDataHandle#fetch()}.
     * As a result, debug related computations can be disabled while the service is inactive.
     */
    boolean isActive();
}
