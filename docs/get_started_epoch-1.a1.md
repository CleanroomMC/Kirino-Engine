# Get Started With `epoch-1.a1`

Kirino Engine `epoch-1.a1` is the first alpha version. It doesn't utilize a SemVer 
version number for now because architecture isn't stable and fast iteration is wanted.

As a mod dev, from `epoch-1.a1`, you can expect:
- Constrained and explicit engine execution model
- General purpose engine entry point to install custom logic
- Runtime ECS access
- Util classes regarding Finite State Machine, Reflections, etc.

What to _not_ expect:
- Full OpenGL abstraction
- Rendering pipelines

> **Note**:<br>
> The purpose of `epoch-1.a1` is to test the robustness and correctness of the ECS runtime.
> Try making mods that fit the ECS context with it would help a lot.

Kirino Engine explicitly separates the execution into two `world view`s, `graphics` and `headless`, where
- Graphics world view handles OpenGL related allocation and executions
- Headless world view handles analytical logic and ECS executions

Specifically, `graphics` context gets access to 
- `ResourceStorage` for graphics related resources
- A bunch of `ResourceSlot` that can be passed to `ResourceStorage` to acquire instances

while `headless` doesn't.

> **Note**:<br>
> Kirino Engine `epoch-1.a1` only runs headlessly. All graphics logic (WIP) is disabled.

## Events

Engine:
- `ModuleInstallerRegistrationEvent`

ECS:
- `StructScanningEvent`
- `ComponentScanningEvent`
- `JobRegistrationEvent`

You should register all engine and ECS event listeners to `KirinoCore.KIRINO_EVENT_BUS` (_**not**_ Forge's event bus!) during `preInit` phase.

> **Note**:<br>
> `ModuleInstallerRegistrationEvent` runs after ECS events.

### Engine Events

During `ModuleInstallerRegistrationEvent`, you have to pass your `ModuleInstaller` implementation which
determines how your logic is injected to the engine lifecycle.

```java
public interface ModuleInstaller<W extends WorldKind> 
{
    void install(@NonNull WorldContext<W> context, @NonNull ResourceLayout layout);
}
```

For `epoch-1.a1`, you should only implement a `ModuleInstaller<Headless>` and 
call `context.on(FramePhase, FramePhaseTiming, Consumer)` inside the `install` method.

You are also allowed to create resource identifiers (`ResourceSlot`) using `ResourceLayout` but
you'll need `ResourceStorage` to instantiate the resources, which requires a `graphics` world (`epoch-1.a1` _only_ runs headlessly).

Note that:
```java
public enum FramePhase {
    PREPARE,
    PRE_UPDATE,
    UPDATE,
    RENDER_OPAQUE,
    RENDER_TRANSPARENT,
    POST_UPDATE,
    RENDER_OVERLAY
}
```
These phases will be run repeatedly every frame, which implies that `PREPARE` will be executed
infinitely many times. 
However, you should still initialize your stuff during `PREPARE` and utilize a boolean flag.

> **Note**:<br>
> `PREPARE`=`init` is a convention, but `ResourceStorage` will also be sealed after the 
> first `PREPARE` phase (no further modification is allowed).

> **Note**:<br>
> `headless` logic always run _before_ `graphics`.
> For example, headless `RENDER_OPAQUE` is a phase right before the actual opaque rendering.
> Of course, you must not draw anything during headless `RENDER_OPAQUE`.

For the `Consumer<WorldContext<Headless>>` you pass to `context.on`,
you are supposed to call `castHeadless(context)` to get the `AnalyticalWorldView` instance for 
the full access permissions. However, you won't need it for `epoch-1.a1`.

### ECS Events

```java
@SubscribeEvent
public static void onStructScan(StructScanningEvent event) {
    event.register("your.package.name");
}
```

For scanning events, register package name so all classes under that directory will be scanned (recursively).

Regarding ECS-Job usage, see the example below:
```java
public class ChunkMeshletGenJob implements ParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public PrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public PrimitiveArray chunkPosZArray;

    @Override
    public void execute(int index) {
        int x = chunkPosXArray.getInt(index);
        int z = chunkPosZArray.getInt(index);
        KirinoCore.LOGGER.info("debug chunk xz: " + x + ", " + z);
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}
```
Two types of annotation, `JobExternalDataQuery` and `JobDataQuery`, are allowed.

> **Note**:<br>
> You must register your job before using it!

You'll need a `JobScheduler` and `EntityManager` to execute a job, which can be access from `context.ecs()`.

_**However**_, you should extend `CleanSystem` and execute jobs there via
```java
public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
```

And you are supposed to call `YourCleanSystem.update(...)` inside your `CleanWorld` instance, where
`JobScheduler` and `EntityManager` instances must be passed to construct a `CleanWorld`. 
That's how dependency flows and `context.ecs()` is the origin of everything.

```
World Context
  └─ ECS Runtime
      ├─ EntityManager
      ├─ JobScheduler
      └─ CleanWorld
           └─ CleanSystems
```

> **Note**:<br>
> You should create `SingleFlow<TSys extends CleanSystem>` inside your `CleanWorld` to help
> systems to run asynchronously with callbacks.

## Summary
In order to utilize `epoch-1.a1` to make a mod, you are supposed to put logic and computations inside
the headless world, _**and**_ put rendering elsewhere like `RenderWorldLast` but not the graphics world since it's disabled for now.

This alpha test exists to validate the versatility of the engine entry point 
and the robustness of the ECS runtime under real-world usage.
