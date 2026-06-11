
## Terrain Rewrite Related
**Major Issues/Bugs**:
- Removing meshlets doesn't update `ranges`/`counters` in `MeshletComputeSystem`, causing stale/ghost
  meshlets to be drawn
- World rebuild doesn't remove meshlets, doesn't guarantee that current async tasks finished,
  doesn't reset `MeshletGpuRegistry`, doesn't reset `MeshletComputeSystem`
- Entities rendering responds to camera rotation slower (lagged). Unknown causes
- Changing render dist dynamically causes lag. Unknown causes

**Todos For Terrain Pipeline/Next Milestone**:
- Expand the pipeline to opaque/transparent/cutout
- Block update notify; reset old state; update properly
- Handle non-fullblock rendering; meshlet pipeline only handles the fullblock rendering
- Handle tile entity renderers; integrate render command pipeline

**Future**:
- SDF AO
- GI
- Low priority: chunks outside render dist=8; Another dedicated LOD solution needed

## General/Misc

- Verify `ChunkMeshletGenJob.fillBlockInfo` coordinate space. It currently passes 16^3 section-local
  block coordinates?
- ECS runtime ownership issue. Multiple ECS runtimes is needed because due to `flush`
- Stabilize/Finish shader debug infra
- Refactor `RenderExtension` and post-processing registration so render extension setup is less ad hoc
- Implement full texture abstraction
- Fix & improve the shader debug framework
- Improve GL BufferView
- A more feature rich implementation of the SimpleText text consumer
- SimpleGUI: a ByteBuffer command stream driven GUI rendering helper layer (no layout etc.)
- Engine Editor: `Document` oriented, no actual record classes for runtime data (generic database instead), easy undo,
  command driven, session oriented (no widget tree for docking)
- Migrate most of `ByteBuffer.allocateDirect` to LWJGL explicit `malloc` & `memFree`. Utilize `ShutdownManager` too.
