package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.process.analysis.install.AnalyticalWorldInstaller;
import com.cleanroommc.kirino.engine.process.graphics.install.GraphicsWorldInstaller;
import com.cleanroommc.kirino.engine.process.graphics.view.GraphicsWorldViewImpl;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.usage.McIntegrationBundle;
import com.cleanroommc.kirino.engine.render.usage.McSceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.WorldRunner;
import com.cleanroommc.kirino.engine.world.event.ModuleInstallerRegistrationEvent;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.process.analysis.view.AnalyticalWorldViewImpl;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;

public class KirinoEngine {

    @SuppressWarnings("FieldCanBeLocal")
    private final GraphicsRuntimeBundle graphicsRuntimeBundle;

    @SuppressWarnings("FieldCanBeLocal")
    private final BuiltinShaderBundle builtinShaderBundle;

    @SuppressWarnings("FieldCanBeLocal")
    private final RuntimeShaderBundle runtimeShaderBundle;

    @SuppressWarnings("FieldCanBeLocal")
    private final McSceneViewState mcSceneViewState;

    @SuppressWarnings("FieldCanBeLocal")
    private final McIntegrationBundle mcIntegrationBundle;

    @SuppressWarnings("FieldCanBeLocal")
    private final ShaderIntrospection shaderIntrospection;

    @SuppressWarnings("FieldCanBeLocal")
    private final RenderStructure renderStructure;

    @SuppressWarnings("FieldCanBeLocal")
    private final RenderExtensions renderExtensions;

    private final ResourceStorage storage;

    /**
     * Storage will be null before it's sealed.
     * You can only expect non-null storage at runtime.
     *
     * <p>You're not supposed to get the storage in general! This method
     * is meant to be accessed by engine kernel classes.</p>
     */
    @Nullable
    public ResourceStorage getStorage() {
        if (storage.isStorageSealed()) {
            return storage;
        } else {
            return null;
        }
    }

    private final WorldRunner<Graphics> graphicsWorld;
    private final WorldRunner<Headless> headlessWorld;

    /**
     * Side-effect free. Resource allocation is deferred via {@link ResourceSlot} or say virtualized.
     * You can treat it like a compile-time safe late binding. This constructor doesn't even care
     * if the engine is in <code>Graphics</code> mode or <code>Headless</code> mode.
     * To be more specific, everything can be safely initialized and depended on without the GL context.
     */
    @SuppressWarnings("unchecked")
    private KirinoEngine(
            @NonNull EventBus eventBus,
            @NonNull Logger logger,
            @NonNull CleanECSRuntime ecsRuntime,
            @NonNull EngineInitParams params) {

        Preconditions.checkNotNull(eventBus);
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(ecsRuntime);
        Preconditions.checkNotNull(params);

        ResourceLayout resourceLayout = MethodHolder.constructResourceLayout();
        storage = MethodHolder.constructResourceStorage();

        graphicsRuntimeBundle = new GraphicsRuntimeBundle(resourceLayout);

        builtinShaderBundle = new BuiltinShaderBundle(resourceLayout);

        runtimeShaderBundle = new RuntimeShaderBundle(resourceLayout);

        mcIntegrationBundle = new McIntegrationBundle(resourceLayout);

        mcSceneViewState = new McSceneViewState(
                storage,
                resourceLayout,
                ecsRuntime,
                graphicsRuntimeBundle,
                mcIntegrationBundle);

        shaderIntrospection = new ShaderIntrospection(
                new GLSLRegistry(),
                new DefaultShaderAnalyzer());

        renderStructure = new RenderStructure(
                params.enableHDR(),
                params.enablePostProcessing(),
                params.enableKhrDebug(),
                params.enableShaderDebug(),
                params.postProcessingSchedule(),
                graphicsRuntimeBundle,
                builtinShaderBundle);

        renderExtensions = new RenderExtensions(
                graphicsRuntimeBundle,
                builtinShaderBundle);

        ModuleInstallerRegistrationEvent event = new ModuleInstallerRegistrationEvent();
        eventBus.post(event);
        List<ModuleInstaller<Headless>> headlessInstallers = MethodHolder.getHeadlessInstallers(event);
        List<ModuleInstaller<Graphics>> graphicsInstallers = MethodHolder.getGraphicsInstallers(event);

        headlessInstallers.addFirst(new AnalyticalWorldInstaller());
        graphicsInstallers.addFirst(new GraphicsWorldInstaller());

        for (ModuleInstaller<Headless> installer : headlessInstallers) {
            logger.info("Registered headless module installer \"{}\".", installer.getClass().getName());
        }
        for (ModuleInstaller<Graphics> installer : graphicsInstallers) {
            logger.info("Registered graphics module installer \"{}\".", installer.getClass().getName());
        }

        graphicsWorld = WorldRunner.of(
                new GraphicsWorldViewImpl(
                        ecsRuntime,
                        renderStructure,
                        renderExtensions,
                        eventBus,
                        logger,
                        storage,
                        builtinShaderBundle,
                        runtimeShaderBundle,
                        graphicsRuntimeBundle,
                        mcIntegrationBundle,
                        mcSceneViewState,
                        shaderIntrospection),
                resourceLayout,
                graphicsInstallers.toArray(ModuleInstaller[]::new));

        headlessWorld = WorldRunner.of(
                new AnalyticalWorldViewImpl(
                        ecsRuntime,
                        renderStructure,
                        renderExtensions,
                        eventBus,
                        logger,
                        shaderIntrospection),
                resourceLayout,
                headlessInstallers.toArray(ModuleInstaller[]::new));
    }

    private boolean modeChosen = false;
    private boolean headlessMode = false;

    private final FramePhaseFSM framePhaseFsm = new FramePhaseFSM();

    private boolean firstPrepareFinished = false;
    private boolean afterFirstPrepare = false;

    /**
     * This will be toggled on forever during the second {@link #run(FramePhase)} or {@link #runHeadlessly(FramePhase)}.
     * This information is important because the first {@link FramePhase#PREPARE} run is executed during
     * the engine initialization instead of world ticking.
     */
    public boolean isAfterFirstPrepare() {
        return afterFirstPrepare;
    }

    //<editor-fold desc="run & runHeadlessly">
    /**
     * The {@link FramePhase} execution order is explicitly guaranteed by the FSM.
     * An error will be thrown on violations.
     *
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public void run(@NonNull FramePhase phase) {
        Preconditions.checkState(!modeChosen || !headlessMode,
                "The engine was running headlessly and it's not allowed to switch mode during runtime.");

        if (!modeChosen) {
            modeChosen = true;
            headlessMode = false;
        }

        Preconditions.checkState(firstPrepareFinished || phase == FramePhase.PREPARE,
                "First phase to be run must be \"PREPARE\".");

        if (firstPrepareFinished && !afterFirstPrepare) {
            afterFirstPrepare = true;
        }

        if (!firstPrepareFinished) {
            firstPrepareFinished = true;
        }

        Preconditions.checkState(framePhaseFsm.getState() == phase,
                "Expect to run \"%s\" but got \"%s\".", framePhaseFsm.getState(), phase);

        framePhaseFsm.next();

        headlessWorld.run(phase, !afterFirstPrepare);
        graphicsWorld.run(phase, !afterFirstPrepare);

        if (phase == FramePhase.PREPARE && !storage.isStorageSealed()) {
            MethodHolder.sealResourceStorage(storage);
        }
    }

    /**
     * The {@link FramePhase} execution order is <i>not</i> guaranteed by the FSM
     * since <code>runHeadlessly</code> is injected to a heavily mixin'd method instead of
     * the methods maintained by us.
     * No error will be thrown on violations.
     *
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public void runHeadlessly(@NonNull FramePhase phase) {
        Preconditions.checkState(!modeChosen || headlessMode,
                "The engine wasn't running headlessly and it's not allowed to switch mode during runtime.");

        if (!modeChosen) {
            modeChosen = true;
            headlessMode = true;
        }

        Preconditions.checkState(firstPrepareFinished || phase == FramePhase.PREPARE,
                "First phase to be run must be \"PREPARE\".");

        if (firstPrepareFinished && !afterFirstPrepare) {
            afterFirstPrepare = true;
        }

        if (!firstPrepareFinished) {
            firstPrepareFinished = true;
        }

        headlessWorld.run(phase, !afterFirstPrepare);

        if (phase == FramePhase.PREPARE && !storage.isStorageSealed()) {
            MethodHolder.sealResourceStorage(storage);
        }
    }
    //</editor-fold>

    //<editor-fold desc="method holder">
    private static final class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getConstructor(ResourceLayout.class),
                    ReflectionUtils.getConstructor(ResourceStorage.class),
                    ReflectionUtils.getMethod(ResourceStorage.class, "seal", void.class),
                    ReflectionUtils.getFieldGetter(ModuleInstallerRegistrationEvent.class, "headlessInstallers", List.class),
                    ReflectionUtils.getFieldGetter(ModuleInstallerRegistrationEvent.class, "graphicsInstallers", List.class));

            Preconditions.checkNotNull(DELEGATE.resourceLayoutCtor);
            Preconditions.checkNotNull(DELEGATE.resourceStorageCtor);
            Preconditions.checkNotNull(DELEGATE.resourceStorageSeal);
            Preconditions.checkNotNull(DELEGATE.headlessInstallersGetter);
            Preconditions.checkNotNull(DELEGATE.graphicsInstallersGetter);
        }

        static ResourceLayout constructResourceLayout() {
            ResourceLayout result;
            try {
                result = (ResourceLayout) DELEGATE.resourceLayoutCtor.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static ResourceStorage constructResourceStorage() {
            ResourceStorage result;
            try {
                result = (ResourceStorage) DELEGATE.resourceStorageCtor.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static void sealResourceStorage(ResourceStorage storage) {
            try {
                DELEGATE.resourceStorageSeal.invokeExact(storage);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        static List<ModuleInstaller<Headless>> getHeadlessInstallers(ModuleInstallerRegistrationEvent event) {
            List<ModuleInstaller<Headless>> result;
            try {
                result = (List<ModuleInstaller<Headless>>) DELEGATE.headlessInstallersGetter.invokeExact(event);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        static List<ModuleInstaller<Graphics>> getGraphicsInstallers(ModuleInstallerRegistrationEvent event) {
            List<ModuleInstaller<Graphics>> result;
            try {
                result = (List<ModuleInstaller<Graphics>>) DELEGATE.graphicsInstallersGetter.invokeExact(event);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
                MethodHandle resourceLayoutCtor,
                MethodHandle resourceStorageCtor,
                MethodHandle resourceStorageSeal,
                MethodHandle headlessInstallersGetter,
                MethodHandle graphicsInstallersGetter) {
        }
    }
    //</editor-fold>
}
