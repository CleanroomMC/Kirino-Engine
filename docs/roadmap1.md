# Foundation Stabilization & Rendering Integration Roadmap

This is a roadmap for now, not necessarily a long-term general purpose one.

One major pain point of Kirino for now is that:
- A wide range of infrastructure is required to support higher level feature development,
  but it needs polishing
- Higher level features/abstractions remain WIP so we can't take over vanilla rendering, blocking
  a majority of APIs

> Note:<br>
> Since we only patch and delegate one major vanilla render call and apply the Strangler Pattern,
> everything must be at least ready and tested to enable the delegation.
> Otherwise, debugging would be hellish due to the error-prone nature of Minecraft patches.

**Goals**:
- Stabilize GL abstractions and ECS as independently usable foundations before expanding higher level rendering
- Minecraft renderer replacement remains deferred

> Example:
> ImmediateClientServices, ICS, is a good way of exposing APIs without relying on the engine lifecycle,
> which is not blocked by the delayed vanilla render delegation.

**FAQ**:
- Why not simply build the higher level features and migrate to future infrastructure later?
  - Kirino focuses on architecture
  - Low level infrastructure is intended to be consumed outside the engine as well, like GL, ECS

---

## 1. Current Architectural Challenges

### 1.1. Implicit Architectural Dependencies

Even when planned subsystems are implemented, they expose gaps in existing abstractions. 
Fixes can span multiple layers, making isolated work and parallel contributions difficult.

Direction:
- Prefer coherent abstractions over feature-specific workarounds
- Stabilize subsystem contracts before expanding
- Delay parallel work when major design decisions remain unresolved

### 1.2. Lifecycle Dependency Inversion

The GL shader debug layer needs engine lifecycle coordination, 
but that is tied to the unfinished engine runtime and requires Minecraft integration. 
This can block foundational work on unrelated high-level changes.

Direction:
- Foundational subsystems should work without the high-level rendering lifecycle
- Presumably move foundational APIs to ICS

---

## 2. GL Abstraction Stabilization

**Goal**: Provide independently usable GL infrastructure with broad API coverage

**Status**: (mostly done)
- [x] Polish Buffer, Texture, Framebuffer, VAO
- [x] Shader reflection
- [ ] Resource binding planner, e.g. TextureUnitPlanner
- [ ] Polish shader debug infrastructure
- [ ] Make shader debug usable outside the engine runtime

The binding planner & shader debug layer require a significant amount of work but aren't urgent.
Expect ~90% of Minecraft rendering scenarios to be covered with the current GL abstraction layer.

---

## 3. ECS Stabilization and Runtime Independence

**Goal**: Make ECS independently usable

**Status**: Core implementation established; polishing pending

## 3.1. Runtime Ownership

`CleanECSRuntime` construction depends on the Kirino bootstrap. 
This complicates independent ECS worlds and lifecycles.

Planned work:
- [ ] Separate ECS definitions and registration
- [ ] Support independently constructed ECS instances, supporting multiple ECS instances
- [ ] (For WIP Renderer) Make world creation, execution, and disposal independent of engine runtime
- [ ] (Follow Up) Expose Minecraft world creation callbacks, chunk callbacks, etc.

## 3.2. Entity and Storage Semantics

- [x] Establish SoA-based component storage
- [x] Implement archetype based organization
- [x] Support deferred entity operations and generation-based handles
- [x] Support component queries and archetype migration
- [ ] Review handle generation validity and concurrent access
- [ ] Implement off-heap MemorySegment based archetypes
- [ ] Implement lifecycle and storage regression tests

## 3.3. Job Execution and Synchronization

- [ ] Define flush boundaries. There are currently no guards (can't flush when any system is still running)
- [ ] (Easy) Implement system execution MultiFlow (see SingleFlow for references)
- [ ] (Hard & Time Consuming) Implement system execution scheduler (see https://github.com/CleanroomMC/Kirino-Engine/pull/8)
  - [ ] Component access chain read/write granularity, like `position.xyz.z | write` doesn't conflict with `position.xyz.y | write`
  - [ ] Find a proper algorithm to schedule the system execution (offline scheduling: build once & no demand on dynamic rebuild)
- [ ] Prevent and warn about potential race conditions inside active jobs? (If possible)
- [ ] Stabilize job scheduling, data injection APIs
- [ ] Validate failure propagation (haven't really thought about the failure propagation)
- [ ] Implement concurrency stress tests & benchmark & optimize

---

## 4. Engine Lifecycle Decoupling

Planned work:
- [ ] Separate infrastructure enablement, Engine lifecycle activation, and Render Delegate activation
- [ ] Keep GL and ECS usable while the Render Delegate is disabled
- [ ] Move useful utilities to ICS

---

## 5. Minecraft Rendering Takeover

**Status**: Deferred

Takeover adds compatibility work involving Minecraft rendering behavior, Forge hooks, 
third-party modifications, external OpenGL state, fallback behavior, and failures outside Kirino.
Other subsystems mentioned above must be implemented and polished before it.

## 5.1. Development-Only Takeover

This is about when we start working on the actual vanilla renderer replacement.

**Entry requirements**:
- [ ] (Section 2,3) GL and ECS foundations meet their usability milestones
- [ ] (Section 4) Engine lifecycle is decoupled
- [ ] ([Engine Overview/Terrain Rendering](engine_overview.md)) SceneType abstraction is done
- [ ] Vanilla rendering path is wired back to the delegated render call with SceneType integrated
- [ ] A test scene is created and works through Kirino's pipeline

## 5.2. Directions

Definitely not finalized.

- Meshlet based rendering and GI for near scene
- [NAADF](https://github.com/cg-tuwien/NAADF) voxel ray tracing for far scene

---

## Future Ideas

- GPU-Driven Terrain Rendering (Partially implemented in [PR7](https://github.com/CleanroomMC/Kirino-Engine/pull/7); Requires 5.1 as a prerequisite)
  - GPU-based meshlet clustering
  - Explore meshlet based lighting techniques (GI)

- Visibility Buffer Rendering (Requires 5.1 as a prerequisite)
  - Replace traditional G-buffer rendering with a visibility buffer
  - Investigate deferred material evaluation

- SDF-Based Ambient Occlusion (Can be developed in parallel; Requires 5.1 for integration)<br>
  `Note: Integration cost is relatively low as it's more about algorithms and procedures`
  - Generate voxel occupancy fields for arbitrary world geometry
  - Investigate SDF-based AO, self-occlusion, and approximate GI

- GPU Model Voxelization (Related to SDF-Based Ambient Occlusion)

- NAADF voxel tracing (Prototype implemented already; Requires 5.1 for further exploration)

- General-Purpose Animation Framework (Can be developed in parallel; Requires 5.1 for integration)<br>
  `Note: Integration cost might be high as we will want everything related to rendering to go through the engine lifecycle`
  - Develop a reusable animation system supporting forward and inverse kinematics (FK/IK)
  - Explore skeletal animation, blending, and alternatives to GeckoLib

- Engine Editor GUI (Can be developed in parallel; Requires SimpleText & SimpleGUI which are mostly done)
  - Polishing SimpleText & SimpleGUI is wanted during the development of Engine Editor GUI
  - Develop a Document/Database oriented editor UI framework that has first class undo support

- Advanced Shader Debugging & Diagnostics (Requires 2/Shader Debug as a prerequisite)
  - Explore buffer inspection, debugging visualization, etc. (Requires Engine Editor GUI)

- Advanced Post-Processing & Temporal Techniques (Can be developed in parallel; Requires lots of things for integration)<br>
  `Note: Post-processing pipeline has been polished once and is ready to be expanded`
  - Investigate framebuffer upscaling/downscaling, motion vectors, and temporal reconstruction
  - Develop reusable image-processing algorithms independently of the main rendering pipeline

- Extensible Asset System (Can be developed in parallel; Slightly requires Engine Editor GUI)
  - Able to serialize "non-serializable" object references like Model, Texture, etc.
  - First class stale value support (less hassle for field renaming)
  - Generate assets from code (e.g. reflection scanning)
  - Engine Editor GUI integration
