package com.cleanroommc.kirino.engine.render.debug.data;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugDataServiceLocator {
    private final Map<Class<?>, DebugDataHandle<?>> handles = new ConcurrentHashMap<>();

    private DebugDataServiceLocator() {
    }

    /**
     * Register your service at any time. No event-driven entry point or strict phase restrictions.
     * However, it's preferably to register services during Forge's <code>preInit</code>/<code>init</code>/<code>postInit</code> stage.
     */
    public <T extends IDebugDataService> void register(@NonNull Class<T> type, @NonNull T service) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(service);

        handles.put(type, new DebugDataHandle<>(service));
    }

    /**
     * This method will throw an exception if the service can't be found.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <T extends IDebugDataService> DebugDataHandle<T> get(@NonNull Class<T> type) {
        Preconditions.checkNotNull(type);

        DebugDataHandle<T> handle = (DebugDataHandle<T>) handles.get(type);

        Preconditions.checkNotNull(handle,
                "Service \"%s\" isn't registered.", type.getName());

        return handle;
    }
}
