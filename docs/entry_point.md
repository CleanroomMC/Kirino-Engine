# Initialization Entry Point

> Relevant classes:
> <br>· `com.cleanroommc.kirino.KirinoCommonCore`
> <br>· `com.cleanroommc.kirino.KirinoClientCore`
> <br>· `com.cleanroommc.kirino.KirinoServerCore`

- `KirinoClientCore.init()` will be executed at the end of `FMLClientHandler.beginMinecraftLoading()`, which occurs at the end of the `preInit` phase.
- `KirinoClientCore.postInit()` will be executed at end of `FMLClientHandler.finishMinecraftLoading()`, which occurs right after Forge's splash process.

During `KirinoClientCore.init()`, the following steps will be performed:
- It calls `KirinoCommonCore.init()` to initialize side-agnostic stuff
- It initializes client side only logic

During `KirinoClientCore.postInit()`
- OpenGL resources like framebuffers will be allocated

For `KirinoServerCore`, it behaves similarly as it calls `KirinoCommonCore.init()` too but
there's no server side only logic at this point.

> Note:
> - We pass `KirinoCommonCore.KIRINO_EVENT_BUS` to the constructors of `CleanECSRuntime` & `KirinoEngine`, so all relevant events will be posted to `KirinoCommonCore.KIRINO_EVENT_BUS`
> - Please use `KirinoCommonCore.KIRINO_EVENT_BUS` instead of `MinecraftForge.EVENT_BUS` for your event listeners

Here are some events you want to listen from the engine setup:
- `StructScanningEvent`
- `ComponentScanningEvent`
- `JobRegistrationEvent`
- `ShaderRegistrationEvent`
- `ModuleInstallerRegistrationEvent`
- ...

# Engine Mode

For client side logic, the engine can run in two different modes depending on the config and environment.

Two modes:
- `Headless`
- `Graphics`

If it was `Headless` mode, no GL related resources will be initialized; vanilla render 
update will not be taken over; however, the engine will not pause but run in headless mode instead

If it was `Graphics` mode, all functionalities will be activated but they are still WIP.

We introduced `Headless` mode to decouple the engine from rendering, so the ECS runtime and
other GL agnostic modules can still run when the rendering part is disbaled or GL requirement isn't met.

# Immediate Services

In order to interact with the engine, you must utilize event listeners to register your classes.
However, `ImmediateServices.instance()` provides immediate access to multiple services without following the lifecycle.

# Rendering Entry Point

> Relevant classes:
> <br>· `com.cleanroommc.kirino.KirinoClientCore`

> Relevant methods:
> <br>· `com.cleanroommc.kirino.KirinoClientCore#EntityRenderer$renderWorld`
> <br>· `com.cleanroommc.kirino.KirinoClientCore#RenderGlobal$notifyBlockUpdate`
> <br>· `com.cleanroommc.kirino.KirinoClientCore#RenderGlobal$notifyLightUpdate`

`EntityRenderer$renderWorld` is a direct replacement of Minecraft's implementation `EntityRenderer#renderWorld`.
Specifically, `anaglyph` logic is removed and all other functions remain the same,
while `anaglyph` can be easily added back via post-processing.

`RenderGlobal$notifyBlockUpdate` and `RenderGlobal$notifyLightUpdate` are hooks injected 
to Minecraft's `RenderGlobal` to listen rendering related updates.

# Util Classes

> Relevant classes:
> <br>· `com.cleanroommc.kirino.utils.*`

Similar to immediate services, feel free to utilize our util classes. Examples:
- `MinecraftResourceUtils.readText(ResourceLocation, NewLineType)`
- `ReflectionUtils` to unreflect fields and methods

