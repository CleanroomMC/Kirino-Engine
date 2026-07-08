# Engine Overview

The details about the actual implementations won't be discussed here.
You can expect to read about general ideas and the mental model from this section.

## Mixin?

We don't have mixins to inject or modify Minecraft rendering behavior.
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
We'd like to reimage the process of organizing terrain rendering.
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

GL resource wrapper classes have the GL allocation located in their Ctor.
`View` classes take in a GL resource wrapper and provide utilities for the users to interact with the GPU.

We don't abstract GL resources in a total blackbox way, like `GL_PersistentMappedBuffer`, `GL_DSABuffer`.
Instead, we respect the combinatorical potentials of GL hints/constants.

## Utils

We provide a set of utilities to accelerate development. Check [utils](utils) for details.
