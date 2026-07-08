
# Kirino Engine (WIP) <img src="logo.png" alt="logo" width="160" align="right" style="margin-left: 16px; vertical-align: middle;"/>

![Platform](https://img.shields.io/badge/Platform-Cleanroom%20Minecraft%201.12.2-brightgreen.svg)
![Status](https://img.shields.io/badge/Status-WIP-yellow.svg)
[![License](https://img.shields.io/badge/License-Custom%20(Mod%20Permissions)-orange.svg)](LICENSE)

Minecraft rendering becomes difficult as implicit state and mixins couple rendering behavior.
Our primary goal is 
- To provide an explicit structure ([engine_overview](docs/engine_overview.md))
- Overhaul most of Minecraft’s rendering in a future-proof manner ([engine_overview → Terrain Rendering](docs/engine_overview.md))
- And provide a set of advanced rendering APIs to mod developers 

## FAQs

<details>
<summary>Click to Expand</summary>

- Is it like a normal render mod?
  Like:
  ```java
  void update()
  {
      glEnable();
      bind();
      obj.render();
      glDisable();
  }
  ```
  - No, and we don't mistake convention for engineering.

<br>

- Why is it different compared to OptiFine / Sodium / Iris?
  - Because Kirino-Engine wants to bring actual architectural changes, and pays less attention to
    _optimization_ and _shaders_

<br>

- Will it be compatible with existing major render mods?
  - No, because Kirino-Engine replaces the whole rendering pipeline
  - _**But**_, it isn't coupled with rendering in order to remove the hard GL version requirement. 
    A toggle, which defeats the point, is therefore provided to disable the rendering part of the engine

<br>

- What's the point of not being compatible with other render mods?
  - To stop perpetuating legacy solutions and API-caller mindsets wrapped in modern graphics APIs
  
<br>

- What can I _expect_ as a player/mod dev when it's out?
  - We provide what we want (read the next section). You might coincidentally like some of it.

<br>

- Hmm, it looks... too promising?
  - Feel free to wait until there's something stable.
    Unfortunately, the goal isn't to sound revolutionary.
    It's disappointing if it seems that way

<br>

- How do I download it?
  - You don't. It'll be shipped with Cleanroom with rolling updates

</details>

## Description

In terms of the **fundamental changes** we'd like to introduce:

- Introduce a set of engine infrastructure on top of Minecraft _(most prioritized)_
  - Enable less mixin work through explicit lifecycles and entrypoint 
    ([entrypoint](docs/entry_point.md))
  - Provide reusable services such as ECS, GL abstraction layer 
    ([ecs_usage_overview](docs/gl/gl_usage_overview.md), [gl_usage_overview](docs/gl/gl_usage_overview.md))
  - Provide the engine editor GUI to inspect everything
  
- Replace direct GL draw calls by abstracted render commands and defer the rendering process _(most prioritized)_
  - No more concerns about the manual GL state management inside TESR/Entity/Item classes
  - No more unbatched and unoptimized draw calls
  - Deferred command execution allows _post_ render modification without mixins
  
- Modernize Minecraft rendering in general
  - Introduce immutable pipeline to hide GL state mutability
  - Introduce RenderPass/Subpass composition
  - Introduce explicit resource lifetime to prevent hidden synchronization
  
- Implement a terrain rendering rewrite with meshlets
  - Normal-based meshlet clustering creates foundation for better lighting, finer culling,
    GPU acceleration, and future visibility buffer techniques 
    ([engine_overview → Terrain Rendering](docs/engine_overview.md))

Looking further ahead, we'd like to pay more attention to lifecycles, phases, and systems, 
rather than features, compatibility with legacy solutions, or similar concerns.

> The project is highly WIP - contributions are welcome to help accelerate development!<br>

> Want a deeper look right away? Jump to "**How Everything Works?**"

## Non-Goals
- Not a drop-in performance patch
- Not a strict compatibility target
- Not an object-centric rendering engine
- Not a finalized or frozen architecture
- Not a high-fidelity path tracing solution

## Dev Env / Build
- Fork this repo (branch: `main`) / OR skip this step
- Clone [Cleanroom](https://github.com/CleanroomMC/Cleanroom) (branch: `fix/lwjgl`) locally
- _Everything below will be happened under your Cleanroom dev env_
- Go to `.gitmodules`
  ```
  [submodule "projects/kirino"]
    path = projects/kirino
    url = https://github.com/CleanroomMC/Kirino-Engine.git
  ```
- Set the `url` to your fork (if you forked Kirino-Engine)
- ```bash
  git submodule init
  git submodule update
  ```
- Import `build.gradle` and then `./gradlew setup`

**Dev Tips**
- `./gradlew cleanroomClient` to run the project
- `./gradlew build` to build the project
- `./gradlew genPatches` to generate patches if you modified Minecraft source code
  (btw you'll have to push to Cleanroom repo if you intended modifying Minecraft source;
  it'd be the best you contact us first before doing so)
- `Cleanroom/projects/cleanroom/src/main/java/` is where you modify Minecraft source code
- `Cleanroom/projects/kirino/src/main/java/` is where you modify your Kirino-Engine fork

> Note:
> Especially if you're using IDEA, you can literally develop Kirino-Engine _inside_ Cleanroom

## Contributing
If you would like to contribute, please take a look at our [Contributing Page](CONTRIBUTING.md)
and the `docs/` to understand the project’s assumptions and architectural direction.

To keep discussions productive, please read the guidelines below.

<details>
<summary>Click to Expand</summary>

### Clarification
Before proposing changes or opening discussions, please:

- Treat/Assume the current design choices as intentional and valid start points
  (that is, reason within the given assumption space)
- Base your reasoning on the assumptions implied by the existing docs
- Focus on internal consistency, trade-offs, and constructive outcomes

### What a Good Proposal Looks Like

**Good example**:
1. Assume the staging buffer and resource management designs are valid and
   their stated (or implied) design principles are satisfied
2. Demonstrate how the staging buffer design creates tension within the resource management principles
3. Conclude that the staging buffer design may need some fix

This kind of proposal helps identify real issues while respecting the project’s intent.

### What to Avoid

**Less helpful examples:**

- “Why ECS? Why GPU-driven? Why XXX?”
- “ECS isn’t useful; OOP might be faster based on my past tuning experience”

These questions re-open foundational decisions **_without engaging with the project’s assumption space_**.

If you believe core assumptions lead to issues,
please demonstrate how it contradicts itself in this specific context.

### Main Idea

Please don’t feel discouraged if your reasoning isn’t formal.
This project values thoughtful discussion. If an idea is
proposed in good faith, incomplete and informal reasoning is still welcome.

</details>

## How Everything Works?
See the `docs/` directory for a high-level overview of Kirino-Engine, 
where you can understand our implicit assumptions, build the mental model gradually, and find code usage explanations.

## Roadmap & Todos
[View Project Board](https://github.com/orgs/CleanroomMC/projects/13) / [View Dev Blog](blog/dev_blog.md) to track development progress, features and ideas.

## Credits

Kirino-Engine is made possible thanks to the efforts of all contributors!

- [tttsaurus](https://github.com/tttsaurus) - Core maintainer, architecture design, and overall project coordination
- [Eerie](https://github.com/Kuba663) - Feature development and algorithmic contributions
- [ChaosStrikez](https://github.com/jchung01) - Code refactoring, call-site improvements, and algorithm fixes

## License

This project is licensed under [Mod Permissions License](https://github.com/CleanroomMC/Kirino-Engine?tab=License-1-ov-file) published by Jbredwards.
