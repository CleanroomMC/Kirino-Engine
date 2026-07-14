# Engine Overview

The details about the actual implementations won't be discussed here.
You can expect to read about general ideas and the mental model from this section.

Most of the confusions related to Kirino Engine will be explained in this section.

## The Strangler Pattern

We don't have a bunch of mixins to modify Minecraft rendering behavior.
Instead, only one Minecraft rendering call is delegated via source patching.

Before:
```java
...
renderWorld();
...
```

After:
```java
...
if (engineEnabled)
{
    ourVersionOfRenderWorld();
}
else
{
    renderWorld();
}
...
```

A full rewrite is therefore required. We need to replicate the whole Minecraft rendering one-to-one.
For now, most of the Minecraft rendering is replaced with a `MethodHandle` call that accesses vanilla implementations
intead of providing our own implementations.

Regarding the rewrite, the goal is to split every part of `renderWorld` into decoupled modules, like
`skybox`, `terrain`, `post-processing`. That is, we will replace vanilla implementations gradually.

## Terrain Rendering

Regarding the `terrain` module, a special approach is taken.

Firstly, we want to abstract away the concept of `scene`. You can think of an enum `SceneType`:
- `MinecraftTerrainWithVanillaImpl`
- `MinecraftTerrainWithOurImpl`
- `TestScene`
- Or more

With `MinecraftTerrainWithVanillaImpl`, the terrain is rendered with vanilla implementations.
With `MinecraftTerrainWithOurImpl`, the terrain is rendered with our implementations.
With `TestScene`, the concept of a voxel terrain no longer exists. We do whatever we want.

When it comes to our version of the terrain renderer, it's going to be done in a modern and experimental way, 
presumably not similar to the solutions you might think of.

To illustrate, every chunk is going to be split into 32 meshlets based on the local curvature, 
and the Meshlet becomes the new smallest unit of rendering rather than a Chunk, which allows finer culling, 
easier lighting evaluations etc.

Technically, we are not going to rely on VAO, VBO, or GL30-ish stuff but embrace GPU-driven rendering with
a fully programmable vertex pulling pipeline.

The goal is not to create yet another renderer with batching optimizations and whatever accelarations.
We'd like to reimagine the process of organizing terrain rendering.
We pay less attention to implementations that look modern but essentially follow an API-caller mindset.

Like, replacing Minecraft textures & buffers with the DSA ones (via mixins) may accelerate things up, but we don't
chase FPS boost in this way.

## ECS

The ECS runtime will be provided by the engine if users register their services through our lifecycle and entrypoint.

And, our ECS is written in Java, not an external dependency, nothing too special here.

> Note:
> The WIP terrain renderer mentioned above is fully based on our ECS.

## GL Abstraction Layer

We've introduced a `GLResourceManager` and interface `Disposable` for the GL resource wrappers.
With our `ShutdownManager`, `GLResourceManager` will register proper hooks and GL resources will be disposed correctly.

GL resource wrapper classes have the GL allocation located in their Ctor.
`View` classes take in a GL resource wrapper and provide utilities for the users to interact with the GPU.

We don't abstract GL resources in a total blackbox way. For example, we avoid 
abstractions like `GL_PersistentMappedBuffer`, `GL_DSABuffer`.
Instead, we respect the combinatorical potentials of GL hints/constants.

> Note:
> The GL abstraction layer is exposed to advanced users, but it's still a low-level part of engine.
> Nothing other than GL resource disposal will be handled by this layer.

## Utils

We provide a set of utilities to accelerate development. Check [utils](utils) for details.
All of them can be accessed without following the engine lifecycle.

## Engine Mode

The engine requires GL46 (we might try utilizing extensions to lower the requirement a bit).
You might be interested what do we do when a GL46 context can't be created.

For client side logic, the engine can run in two different modes depending on the config and environment.

Two modes are:
- `Headless`
- `Graphics`

If it was the `Headless` mode, no GL related resources will be initialized; vanilla render
update will not be taken over; however, the engine will not pause but run in headless mode instead.

If it was the `Graphics` mode, all functionalities will be activated - i.e. vanilla render
update will be taken over.

We introduced `Headless` mode to decouple the engine from rendering, so the ECS runtime and
other GL agnostic modules can still run when the rendering part is disbaled or GL requirement isn't met.

## Virtualized Engine Initialization

To properly support the both `Headless` and `Graphics` mode. We've virtualized the engine initialization process.

It means that:
- All `shader`, `renderer`, `renderpass` instances can be safely initialized without a GL context requirement.
- All `shader`, `renderer`, `renderpass` instances exist on both `Headless` and `Graphics` mode.
- All `shader`, `renderer`, `renderpass` instances can be safely depended on by other higher level abstractions.
- The engine is capable to describe the existence of the classes that used to 100% crash the engine.

To illustrate, everything is defined using `ResourceSlot<T>` during the initialization, and a `storage.get(slot)`
call is required to resolve the resources during the runtime. `ResourceStorage` will fail fast if the requested resource
can't be resolved.

The whole initialization can be treated like two-pass.
During the first pass, resource definitions will be initialized.
During the second pass, actual resources will be selectively allocated depending on the engine mode.

Moreover, you don't have to introduce `if-else` checks to write different logic for `Headless` and `Graphics` mode.
We've introduced the concept of `WorldRunner<Mode>` to avoid `if-else` checks.

When the rendering is enabled _AND_ the GL context satisfies the requirement:
- ```java
  headlessWorld.run();
  graphicsWorld.run();
  ```
Otherwise:
- ```java
  headlessWorld.run();
  ```

The `headlessWorld` is intended to be run regardless, and the users are encouraged to put pure analytical logic
inside it, like ECS (of course the headless world provides the ECS runtime as well).

Most interestingly, shader registration and render pass definitions are also visible to the headless world.
Most of the rendering things are rendering-agnostic in our engine.

## GL State Concerns

GL state management is like the last piece of the puzzle regarding infrastructure and groundwork.

GL state management strategies are hard to distinguish without explicit clarifications since they are 
fundamentally just a bunch of `glEnable`/`glDisable` calls, and even devs feel lost while facing the whole 
globally mutable state machine.

Regarding the state management strategy:
- We avoid using the change/restore pattern (with `glGet` calls) for the main render update call
- We avoid using a state tracker to track states for the main render update call
- We avoid blindlessly spamming `glEnable`/`glDisable` calls for the main render update call

> Clarification:
> We only care about the GL state management inside `ourVersionOfRenderWorld()`. 
> It's easier to take over the state management when the executions are centralized.

### `glKnowledge` & Immutable Time Slice Model

We've introduced a temporal model for GL states.
You can treat it like the users are allowed to divide a frame into multiple slices.
```java
|--- Slice 1 ---|--- Slice 2 ---|--- Slice 3 ---|--- ...
```

For each slice, the GL states are all treated immutable. If you want to change anything, you'll have to commit another slice.
However, GL states intrinsically can't be immutable. That's why our strategy is more like enforcing a _contract_ instead of straight
up "Vulkan-like immutable pipeline".

For each slice, users can get access to the knowledge runtime called `glKnowledge`, and they are allowed to 
```java
void commit(the known GL state changes) // commit the next slice
void require(key, expected value) // check if something conflicts your knowledge about gl states
void requireKnown(key) // check if something is known
void reportMutation(key) // report if you are sure that some gl state knowledge won't be reliable anymore
```

The main idea is to divide the timeline and every piece of it is supposed to be immutable.

As you can see, our modeling isn't related to the _GL truth_ but the GL states to the best of our knowledge.
The uncertainty itself is also the first-class object here, and the systems are allowed to "risk it" when 
GL knowledge violations happen.

### Clarifications On "GL State Leaks"

GL state leakage describes a situation where a certain state is supposed to be the expected value but not.
The reason behind is that no one knows what's the expected GL state at this moment, especially with nested rendering classes.

GL state tracker doesn't resolve the issue that every render class is unaware of the current GL state requirements.
Instead, it allows external render code to mess up every state during a certain period and then "roll back."

However, even with the ability to roll back a certain time period. The scope of that period, 
also known as granularity, defines GL state leakage.

For example, the following render code might result in artifact.
```java
void render()
{
    renderTexture();
    renderText();   
}
```
If the first `renderTexture` call binds its own texture and the second `renderText` call doesn't,
then `renderText` isn't going to display glyphs correctly aka GL state leakage. In the meanwhile,
the GL state tracker tracks state changes and will roll back everything after the `render` call. 
Obvisouly, it doesn't help.

### Uses Of `glKnowledge`

The purpose of `glKnowledge` is to help identifying the current GL state requirements.
It's harder to leak states when everyone knows what they are doing, and we don't like rolling back states and keeping
every part of the system blind.

- `commit()` can be seen as a way to declare what I want for this time slice
  - For each `RenderPass`, an immutable `PipelineStateObject` is required to apply, which sets GL states and
    calls `commit()` to update the known GL knowledge
  - Subsystems are therefore aware of "what are the states to set back?"
- `require` doesn't tell you what are the state requirements
  - It helps you to examine your belief about the current time slice
  - You can't see what have been committed. They are contract-driven / defined in docs

The whole knowledge model only makes the bad GL assumptions break explicitly, and it acts as a way to enforce
our contract of rendering which intrinsically avoids GL state leakage.

### Bonus Points

The `glKnowledge` and immutable time slice model allows you to divide a frame into infinitely many slices,
thus enforcing infinitely many contracts.

For the predefined engine phases, like `RENDER_OPAQUE`, `RENDER_TRANSPARENT`, they will commit their knowledge to
enforce their contracts. Similarly, you can make your own contracts for your own subsystems. Of course, you're free
to do so without the existence of `glKnowledge`, but an explicit knowledge runtime makes you feel "safer" and helps bad
code to fail fast.

### Compatibility Details

For the boundaries between the engine phase and Minecraft phases,
a bunch of well selected `glEnable`/`glDisable` calls are hardcoded to restore the states to a baseline so
vanilla implementations continue to work. This is not part of the contract but totally compatibility work.

## Frame Phases

Regarding `ourVersionOfRenderWorld()`, it looks like
```java
void ourVersionOfRenderWorld()
{
    PREPARE();
    
    ...

    PRE_UPDATE();
    
    ...

    UPDATE();
    
    ...

    RENDER_OPAQUE();
    
    ...
    
    RENDER_TRANSPARENT();
    
    ...
    
    POST_UPDATE();
    
    ...
            
    RENDER_OVERLAY();
}
```

Where the GL temporal contract is applied to each phase, and exiting a phase automatically invalidates
the current knowledge about GL. Vanilla implementations are treated like the outside world.

Users are allowed to register their callbacks to each phase and interact with the runtimes including `glKnowledge`.

```java
glKnowledge.require(...);
glKnowledge.requireKnown(...);
```
Interacting with the `glKnowledge` is a perfect way to explicitly assert your assumptions. 
As a result, your callbacks are allowed to work in a more transparent environment.

Anyway, the engine frame phase callback acts as a low-level entrypoint to interact with the engine.
Other lifecycle-agnostic and data-driven entrypoint will also be provided.
