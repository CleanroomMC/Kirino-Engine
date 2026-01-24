# Initialization Entry Point

> Relevant classes:
> <br>· `com.cleanroommc.kirino.KirinoClientCore`

- `KirinoCore.init()` will be executed at the end of `FMLClientHandler.beginMinecraftLoading()`, which occurs at the end of the `preInit` phase.
- `KirinoCore.postInit()` will be executed at end of `FMLClientHandler.finishMinecraftLoading()`, which occurs right after Forge's splash process.

During `KirinoCore.init()`, the following steps will be performed:
- Set up `KHR_debug`
- Register default event listeners to `KirinoCore.KIRINO_EVENT_BUS`
- Initialize the ECS Runtime
- Initialize the Kirino Engine

During `KirinoCore.postInit()`
- OpenGL resources like framebuffers will be allocated

> Note:
> - We pass `KirinoCore.KIRINO_EVENT_BUS` to the constructors of `CleanECSRuntime` & `KirinoEngine`, so all relevant events will be posted to `KirinoCore.KIRINO_EVENT_BUS`
> - Please use `KirinoCore.KIRINO_EVENT_BUS` instead of `MinecraftForge.EVENT_BUS` for your event listeners

Here's a list of events you can listen from ECS Runtime setup:
- `StructScanningEvent`
- `ComponentScanningEvent`
- `JobRegistrationEvent`

Here's a list of events you can listen from Kirino Engine setup:
- `ShaderRegistrationEvent`
- `PostProcessingRegistrationEvent`

# Rendering Entry Point

> Relevant classes:
> <br>· `com.cleanroommc.kirino.KirinoClientCore`

> Relevant methods:
> <br>· `com.cleanroommc.kirino.KirinoClientCore#EntityRenderer$renderWorld`
> <br>· `com.cleanroommc.kirino.KirinoClientCore#RenderGlobal$notifyBlockUpdate`
> <br>· `com.cleanroommc.kirino.KirinoClientCore#RenderGlobal$notifyLightUpdate`

`EntityRenderer$renderWorld` is a direct replacement of Minecraft's implementation `EntityRenderer#renderWorld`.
Specifically, `anaglyph` logic is removed and all other functions remain the same,
while `anaglyph` can be easily added back via post-processing by the way.

`RenderGlobal$notifyBlockUpdate` and `RenderGlobal$notifyLightUpdate` are hooks injected 
to Minecraft's `RenderGlobal` to listen rendering related updates.
