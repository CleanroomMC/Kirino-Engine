package com.cleanroommc.kirino.engine.render.usage.scene.callback;

import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MeshletDestroyCallbackTest {
    @Test
    void destroy_onlyReadsTheMeshletIdAndDisposesItWithoutReconstructingTheComponent() throws Throwable {
        ResourceStorage storage = MethodHolder.storage();
        ResourceLayout layout = MethodHolder.layout();
        var slot = layout.slot(MeshletGpuRegistry.class);
        AtomicInteger disposed = new AtomicInteger(-1);
        storage.put(slot, new MeshletGpuRegistry() {
            @Override public void disposeMeshletID(int id) { disposed.set(id); }
        });
        EntityDestroyContext context = (EntityDestroyContext) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{EntityDestroyContext.class}, (proxy, method, arguments) -> {
                assertEquals("getInt", method.getName());
                assertSame(MeshletComponent.class, arguments[0]);
                assertArrayEquals(new String[]{"meshletId"}, (String[]) arguments[1]);
                return 713;
            });
        new MeshletDestroyCallback(storage, slot).beforeDestroy(context);
        assertEquals(713, disposed.get());
    }

    private static final class MethodHolder {
        static final MethodHandle NEW_STORAGE = Preconditions.checkNotNull(ReflectionUtils.getConstructor(ResourceStorage.class));
        static final MethodHandle NEW_LAYOUT = Preconditions.checkNotNull(ReflectionUtils.getConstructor(ResourceLayout.class));

        static ResourceStorage storage() throws Throwable { return (ResourceStorage) NEW_STORAGE.invokeExact(); }
        static ResourceLayout layout() throws Throwable { return (ResourceLayout) NEW_LAYOUT.invokeExact(); }
    }
}
