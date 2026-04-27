
`MinecraftScene` is a platform-level world orchestrator, not a renderer.

`FrameFinalizer` also decides the minimum resource allocation, not only finalizes a frame.
The optimal solution is to split it into `FrameFinalizer` and `RenderGraph`, but Minecraft rendering
is so 'discrete' that it would be so hard to actually implement `RenderGraph`.

Our framebuffer resource allocation (see `FrameFinalizer`) requires post-processing data to be
predefined / fixed / ahead-of-time. Eg. pass count<=1 implies 1 intermediate fbo, but pass count>=2 implies
2 intermediate fbo. As a result, I guess we can have a fixed built-in early post-processing _**and**_
a dynamic late post-processing (togglable on startup) which requires 2 fbo anyways.

The architecture boundary between `render.core` and `render.platform` is
whether the classes touch Minecraft concepts like `Chunk`s etc. It's fine to
reference Minecraft classes, and that's why I put `MinecraftCamera` under `render.core`.
