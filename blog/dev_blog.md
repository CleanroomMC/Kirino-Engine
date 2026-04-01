
## Feb 2026
**Want To Implement:**
- Terrain meshlet double buffering
- Meshlet write → compute → fully programmable vertex pulling _(i)_
- Expand the _(i)_ pipeline to opaque/transparent/cutout
- Block udpate notify
- On demand compute; in place rewrite

**Follow-up Tasks:**
- GL test framework
- Full texture abstraction including DSA and 1D 2D 3D 2DArray etc.
- Shader debug framework including macro injection etc.

**Future To-Dos:**
- Migrate to visibility buffer for terrain rendering
- SDF AO

**Done:**
- Implement the terrain meshlet double buffering pipeline
  - 2 buffers for meshlet data read (read by compute) and write (write from cpu)
  - 2 buffers for compute data read (read by draw calls) and write (write from gpu)
- Fix terrain meshlet double buffering lifecycle issue
- Add terrain meshlet double buffering lifecycle debug hud
  ![](gallery/2026-02-21.png)
- Remove interface `I` prefix
- Implement GL junit test extensions

## March 2026
**Done:**
- Refactor & implement texture abstraction: `GLTexture` + `Accessor`
- Replace SSBO counter by uimage1D
- Basic shader debug infra

**Want To Investigate:**
- Crash due to compute shader debug
- Why `Meshlet.blockCount == 0` for some compute shader calls
- Whether GPU side meshlet vertex/index data follow the slot layout

**3.31 Snapshot:**
- Currently working on the "On demand compute; in place rewrite"
  - In order reduce the amount of compute shader's work, 
    meshlet vertex/index output will follow a slot memory layout 
    (`|Slot 0: data ... padding|Slot 1: ...` where every slot has the same length, so in place rewrite will be possible)
- Issue: `Meshlet.blockCount == 0` for some compute invocations while there's no empty meshlet on CPU side
- Issue: shader debug infra causes crash (system freeze) due to an unknown issue
- Need to verify that meshlet vertex/index output is working correctly and following a slot layout
- Need another SSBO to record indices to guide VS vertex pulling
