package com.cleanroommc.kirino.engine.render.usage.scene.scheduler;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataServiceLocator;
import com.cleanroommc.kirino.engine.render.usage.debug.data.MeshletGpuTimeline;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Throwables;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public final class RendererTestSupport {
    private static final MethodHandle NEW_STORAGE = ReflectionUtils.getConstructor(ResourceStorage.class);
    private static final MethodHandle NEW_LAYOUT = ReflectionUtils.getConstructor(ResourceLayout.class);
    private static final MethodHandle NEW_DEBUG = ReflectionUtils.getConstructor(DebugDataServiceLocator.class);
    private static final MethodHandle GET_TIMELINE = ReflectionUtils.getFieldGetter(KirinoClientDebug.class,
        "meshletGpuTimeline", DebugDataHandle.class);
    private static final MethodHandle SET_TIMELINE = ReflectionUtils.getFieldSetter(KirinoClientDebug.class,
        "meshletGpuTimeline", DebugDataHandle.class);

    public static void initializeConfig(Path home) throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            homeField.set(null, home.toFile());
            Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunch;
        }
    }

    static ResourceStorage storage() throws Throwable { return (ResourceStorage) NEW_STORAGE.invokeExact(); }
    static ResourceLayout layout() throws Throwable { return (ResourceLayout) NEW_LAYOUT.invokeExact(); }

    public static DebugDataHandle<?> installInactiveTimeline() throws Throwable {
        DebugDataHandle<?> previous = (DebugDataHandle<?>) GET_TIMELINE.invokeExact();
        DebugDataServiceLocator debug = (DebugDataServiceLocator) NEW_DEBUG.invokeExact();
        debug.register(MeshletGpuTimeline.class, new MeshletGpuTimeline() {
            @Override public boolean isActive() { return false; }
        });
        setTimeline(debug.get(MeshletGpuTimeline.class));
        return previous;
    }

    public static void setTimeline(DebugDataHandle<?> handle) throws Throwable {
        SET_TIMELINE.invokeExact((DebugDataHandle) handle);
    }

    static void rethrow(Throwable failure) {
        Throwables.throwIfUnchecked(failure);
        throw new CompletionException(failure);
    }

    static final class Flow implements SystemExeFlowGraph {
        final List<CompletableFuture<Void>> runs = new ArrayList<>();
        int synchronousRuns;
        Throwable launchFailure;
        Throwable synchronousFailure;
        Runnable synchronousAction = () -> {};

        @Override
        public void execute() {
            synchronousRuns++;
            synchronousAction.run();
            if (synchronousFailure != null) {
                rethrow(synchronousFailure);
            }
        }

        @Override
        public CompletableFuture<Void> executeAsync(Executor executor) {
            if (launchFailure != null) {
                rethrow(launchFailure);
            }
            CompletableFuture<Void> run = new CompletableFuture<>();
            runs.add(run);
            return run;
        }

        @Override public boolean isExecuting() { return false; }
        CompletableFuture<Void> current() { return runs.getLast(); }
    }
}
