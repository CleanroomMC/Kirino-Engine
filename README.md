
# Kirino Engine <img src="logo.png" alt="logo" width="160" align="right" style="margin-left: 16px; vertical-align: middle;"/>

Kirino-Engine is a CPU-GPU dual pipeline rendering engine that combines ECS paradigm and modern rendering techniques.

Its primary goal is to overhaul most of the Minecraft's rendering in a future-proof and elegant manner **_and_** provide a set of clean rendering APIs to mod devs.

> The project is highly WIP - contributions are welcome to help accelerate development!

## Q&A

<details>
<summary>Click to Expand</summary>

- Why is Kirino-Engine different compared to OptiFine / Sodium / Iris?
  - Because Kirino-Engine reimagines the entire rendering pipeline without patching things in an unmaintainable way
  - Kirino-Engine is fundamentally GPU-driven and ECS-driven, unlike traditional optimizers or shader mods
  - Kirino-Engine pays a huge attention to its architecture
  - Kirino-Engine isn't merely an optimizer or shader mod — it's an rendering engine

- What's the most exciting features in dev?
  - GPU-Driven Meshlet Rendering & Virtual Geometry
  - Modern Rendering Paradigms (like Multi-Draw Indirect, Pipeline State Objects, etc.)
  - Semi-Static Global Illumination Driven by Surface Cards & Temporal Accumulation
  - ECS, Parallel Job, and Execution Graph (a Multithread-Friendly Architecture)
  - Async GPU Resource System
  - Multi-resolution & HDR & Post-processing Pipeline

- What can I expect as a player?
  - Performance wise: smoother performance and FPS improvements; Higher CPU & GPU utilization
  - Shader wise: modern lighting techniques and better global illumination
  - Configurability: optional HDR, optional resolution up-scaling or down-scaling, optional post-processing, etc.
  - Ecosystem: easily extendable rendering pipeline for community shaders

- What can I expect as a mod developer?
  - Clean rendering APIs that hide OpenGL completely
  - Versatile rendering APIs that focus on the concept of render commands
  - Will be able to create emissive blocks, PBR, fogs, decals, any post-processing effects
  - Future-proof architecture

</details>

## Roadmap & Todos
[View Project Board](https://github.com/orgs/CleanroomMC/projects/13) to track development progress, features and ideas.

## How It Works?
A player-friendly version of engine overview is work-in-progress.

## Contributing
If you would like to contribute, check out our [Contributing Page](https://github.com/CleanroomMC/Kirino-Engine?tab=contributing-ov-file) and [Engine Overview Page](https://github.com/CleanroomMC/Kirino-Engine/blob/main/ENGINE_OVERVIEW.md)!

## MVP Goals

It's a bit outdated, [Project Board](https://github.com/orgs/CleanroomMC/projects/13) takes precedence.

<details>
<summary>GL Abstraction</summary>

**Goal**: a semantic abstraction layer that preserves the meaning of GL operations instead of a black-box GL wrapper 

- GL Resource Abstraction 🚧
  - Resource manager 🚧
- Shader Abstraction 🚧
  - Only support `vert` + `frag` for now, but design with `tess`, `compute`, etc. in mind
  - Global shader registry
    - Compile and store shaders ✅
    - Shader source hashing
  - Uniform
    - Parse uniforms from shader source
    - Uniform location and type memorization
    - UBO support
  - ShaderProgram
    - Uniform input type widening
- Buffer Abstraction 🚧
  - Generic buffer object + View ✅
  - VAO (VBO + EBO) ✅
  - Vertex attribute layout ✅
  - UBO, SSBO
  - PBO pack & unpack
  - TBO
  - Upload hint + access hint ✅
  - Persistent buffer ✅
  - Framebuffer ✅
    - Attachment ✅
    - RenderBuffer ✅
- Texture Abstraction 🚧
  - Sampler
  - Generic texture object + View ✅
  - Texture 🚧
    - Texture2D (for common uses) ✅
    - Texture2DMultisample (for multisampling fbo) 🚧
    - Texture2DArray (for texture atlas)
    - ...
- Debug Abstraction ✅
  - KHR_debug ✅
- Material Abstraction
  - MaterialTemplate to describe layout and shaders
  - MaterialInstance to hold actual parameters

</details>

<details>
<summary>ECS</summary>

- Overall ECS structure ✅
  - CleanWorld, CleanEntity, CleanComponent, CleanSystem ✅
- Entity ✅
  - Entity manager (utilizes archetype) ✅
- Component ✅
  - Component schema ✅
  - Class scan via ClassGraph ✅
- Storage ✅
  - Archetype ✅
- Runtime 🚧
  - `SystemExeGraph` to coordinate different systems
    - Execution priority
    - Async execution & barrier
- Job ✅
  - Job is a unit of work that can be split and executed in parallel ✅

</details>

<details>
<summary>Engine</summary>

- CPU & GPU hybrid dual pipeline 🚧
- DrawCommand decorating mechanism 🚧
- RenderPass / Subpass architecture ✅
- Built-in Multi-resolution & Super-sampling 🚧
- Immutable Pipeline State Object ✅
- Meshlets 🚧
- Scriptable pipeline
- ...

</details>

## Credits

Kirino Engine is made possible thanks to the efforts of all contributors!

- [tttsaurus](https://github.com/tttsaurus) - Core maintainer, architecture design, and overall project coordination
- [Eerie](https://github.com/Kuba663) - Feature development and algorithmic contributions
- [ChaosStrikez](https://github.com/jchung01) - Code refactoring, call-site improvements, and algorithm fixes

## License

This project is licensed under [Mod Permissions License](https://github.com/CleanroomMC/Kirino-Engine?tab=License-1-ov-file) published by Jbredwards.
