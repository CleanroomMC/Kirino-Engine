
# Kirino Engine <img src="logo.png" alt="logo" width="160" align="right" style="margin-left: 16px; vertical-align: middle;"/>

Kirino-Engine is a CPU-GPU dual pipeline research-oriented rendering engine that combines ECS paradigm and modern rendering techniques.

Its primary goal is to overhaul most of Minecraft’s rendering in a future-proof and elegant manner, 
provide a set of clean rendering APIs to mod developers, 
**_and_** serve as a platform for exploring the feasibility of modern, GPU-driven rendering techniques within Minecraft’s constraints.

> The project is highly WIP - contributions are welcome to help accelerate development!

## Q&A

<details>
<summary>Click to Expand</summary>

- Why is Kirino-Engine different compared to OptiFine / Sodium / Iris?
  - Because Kirino-Engine reimagines the entire rendering pipeline without patching things in an unmaintainable way
  - Kirino-Engine is fundamentally GPU-driven and ECS-driven, unlike traditional optimizers or shader mods
  - Kirino-Engine pays a huge attention to its architecture
  - Kirino-Engine isn't merely an optimizer or shader mod — it's a research-oriented rendering engine

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
  - Ecosystem: easily extensible rendering pipeline for community shaders

- What can I expect as a mod developer?
  - Clean rendering APIs that hide OpenGL completely
  - Versatile rendering APIs that focus on the concept of render commands
  - Will be able to create emissive blocks, PBR, fogs, decals, any post-processing effects
  - Future-proof architecture

</details>

## What This Project Provides?

### 1) Engine-agnostic ECS framework
- Data-oriented SoA archetypes as the core data model, 
  designed for predictable memory layout and efficient iteration
- Strict separation of entity identity, component storage, and system execution
- Designed to integrate with other data-oriented tech stacks like
  job systems, execution graphs

### 2) Low-level OpenGL abstraction layer
The project provides a low-level, semantic abstraction layer over modern OpenGL.
Rather than acting as a black-box wrapper, this layer aims to preserve the
meaning of OpenGL operations, **_and_** explain the implicit and subtle assumptions clearly. (WIP; partially usable)

Note: this part is partially coupled with the engine implementation

This abstraction focuses on:
- Explicit GPU resource management, ctor allocation & explicit disposability & auto disposability
- Shader and program registry
- Clear separation between resource description and usage, a buffer-view pattern to be exact
- OpenGL debug instrumentation

The goal of this layer is not to hide OpenGL, **but to clarify it**.

### 3) Rendering engine implementation

Building on the ECS framework and OpenGL abstraction, the project includes a
concrete rendering engine implementation. (WIP)

The engine is designed around a hybrid CPU–GPU pipeline, where high-level
orchestration remains on the CPU while some jobs like culling, data processing are delegated to the GPU.

Note: Minecraft is mostly used as a data source and constraint, rather
than as a strict compatibility target.

Key characteristics include:
- A pass-based rendering architecture with explicit `RenderPass` and `Subpass`
  composition
- Immutable pipeline state descriptions to reduce state ambiguity and CPU-GPU stall (when fetching states)
- Support for advanced rendering techniques like meshlet-based virtual geometry, GPU-driven draw submission
- Leave rooms to experimental features like multi-resolution rendering, super-sampling

## Roadmap & Todos
[View Project Board](https://github.com/orgs/CleanroomMC/projects/13) to track development progress, features and ideas.

## How It Works?
A user-friendly version of engine overview is work-in-progress.

## Contributing
If you would like to contribute, check out our [Contributing Page](https://github.com/CleanroomMC/Kirino-Engine?tab=contributing-ov-file) and [Engine Overview Page](https://github.com/CleanroomMC/Kirino-Engine/blob/main/ENGINE_OVERVIEW.md)!

## Credits

Kirino-Engine is made possible thanks to the efforts of all contributors!

- [tttsaurus](https://github.com/tttsaurus) - Core maintainer, architecture design, and overall project coordination
- [Eerie](https://github.com/Kuba663) - Feature development and algorithmic contributions
- [ChaosStrikez](https://github.com/jchung01) - Code refactoring, call-site improvements, and algorithm fixes

## License

This project is licensed under [Mod Permissions License](https://github.com/CleanroomMC/Kirino-Engine?tab=License-1-ov-file) published by Jbredwards.
