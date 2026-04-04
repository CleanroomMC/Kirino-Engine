
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
  - In order to reduce the amount of compute shader's work, 
    meshlet vertex/index output will follow a slot memory layout 
    (`|Slot 0: data ... padding|Slot 1: ...` where every slot has the same length, so in place rewrite will be possible)
- Issue: `Meshlet.blockCount == 0` for some compute invocations while there's no empty meshlet on CPU side
- Issue: shader debug infra causes crash (system freeze) due to an unknown issue
- Need to verify that meshlet vertex/index output is working correctly and following a slot layout
- Need another SSBO to record indices to guide VS vertex pulling

## April 2026

**Debugging**:
- (i) **Actual cause**: UNKNOWN<br>
  `MeshletBufferWriteJob.execute` didn't run (due to the lack of data) for the first write task; therefore `Meshlet.blockCount == 0` for the first compute dispatch.
  However, a `write --> then --> compute` lifecycle is guaranteed. Issue might be stemmed from the ECS job related stuff.
- (ii) **Potential race condition**<br>
  `CleanWorld.update`, which is flushing the ecs commands, may run while `job.executeAsync` is running.
  As a result, archetype data may be modified while a job is reading it.
  Creating snapshots might be too expensive, so I may delay `CleanWorld.update` based on the current job status
  ```java
  if (!system1.isExecuting() && !system2.isExecuting() ...) {
      super.update();
  }
  ```
  But it looks less elegant since systems/jobs inside a `CleanWorld` will require manual managements.
  It still makes sense if we aim to provide a low level ECS.
- **Guess**<br>
  The potential ECS race condition might be the cause of `Meshlet.blockCount == 0` but
  the empty meshlet issue only happens for the first compute dispatch. The consistency makes this thing more suspicious.
  Race conditions might not be consistent like that.
- (iii) **Guess**<br>
  Shader debug was causing system freeze and crash likely because I didn't allocate enough
  storage for buffers? Btw I shouldn't assume that debug data stay together for every frame since `append log` was called async.
- **Fixed (i)!!!**
  - Firstly, there was a common pool starvation issue that slightly pauses my ECS system flow execution.
    Now, I've switched to my dedicated fork join pool. Resolved.
  - For the empty meshlet issue, the culprit was essentially this line from `SystemExeFlowGraph`
    ```java
    /**
    * Async version of {@link #execute()}.
    */
    default CompletableFuture<Void> executeAsync(Executor executor) {
        return CompletableFuture.runAsync(this::execute, executor);
    }
    ```
    So `executing=false` is slightly delayed due to the async execution, creating a submit-start gap.
    When it comes to `MeshletGpuPipelineScheduler`
    ```java
    if (storage.get(meshletGpuRegistry).hasMeshletChanges()) {
        if (storage.get(meshletGpuRegistry).isWriting()) {
            meshletFsm.next(); // COMPUTABLE

            KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_ALREADY_WRITING);
        } else {
            KirinoClientDebug.MeshletGpuTimeline$beginWriting();

            storage.get(meshletGpuRegistry).beginWriting();
            meshletBufferWriteSystem.executeAsync(systemFlowExecutor);
            meshletFsm.next(); // COMPUTABLE

            KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_BEGIN_WRITING);
        }
    } else ...
    ```
    FSM advances to `COMPUTABLE` from `IDLE` immediately but `executing` is still `false` due to the submit-start gap.
    The compute system therefore thinks that the write task has finished, messing up the first compute dispatch.
    Here's the log that verifies the correctness of the fix.
    <details>
    <summary>Click to Expand</summary>

    ```log
    [01:16:27] [Client thread/INFO] [Kirino Core]: dispatch 4
    [01:16:27] [ForkJoinPool-1-worker-3/INFO] [Kirino Core]: callback start meshletBufferWriteSystem
    [01:16:27] [ForkJoinPool-1-worker-3/INFO] [Kirino Core]: callback finish meshletBufferWriteSystem
    [01:16:27] [Client thread/INFO] [Kirino Core]: ---------------------------------
    [01:16:27] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f10 (first index): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
    [01:16:27] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
    [01:16:27] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 512
    [01:16:27] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 768
    [01:16:27] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
    [01:16:27] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): -1
    [01:16:27] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 1
    [01:16:27] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f10 (first index): 1152
    [01:16:27] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
    [01:16:27] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
    [01:16:27] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 128
    [01:16:27] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 192
    [01:16:27] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
    [01:16:27] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): -1
    [01:16:27] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 2
    [01:16:27] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f10 (first index): 2304
    [01:16:27] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
    [01:16:27] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
    [01:16:27] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 384
    [01:16:27] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 576
    [01:16:27] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
    [01:16:27] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): -1
    [01:16:27] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): -1
    [01:16:27] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 3
    [01:16:27] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f10 (first index): 3456
    [01:16:27] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
    [01:16:27] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
    [01:16:27] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 256
    [01:16:27] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 384
    [01:16:27] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
    [01:16:27] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
    [01:16:27] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): 0
    ```

    </details>

**Follow-up Tasks:**
- Must provide multiple ECS runtimes since ECS flush timing is per `CleanWorld`
- Meshlets that belong to chunks outside radius=8 must be disposed immediately since
  all max buffer sizes are defined using `WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS = 16384`
- Refactor `RenderExtension` & post-processing registration

Whether `GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT` is needed for coherent mapped buffers?
- No
  https://community.khronos.org/t/usage-of-gl-client-mapped-buffer-barrier-bit/75355
  > If GL_MAP_COHERENT_BIT is set and the server does a write, the app must call FenceSync with GL_SYNC_GPU_COMMANDS_COMPLETE (or glFinish). Then the CPU will see the writes after the sync is complete.

Slot-allocation layout / in-place-rewrite safe layout verified:
- <details>
  <summary>Click to Expand</summary>
  
  ```log
  [22:29:20] [Client thread/INFO] [Kirino Core]: dispatch 3
  [22:29:21] [Client thread/INFO] [Kirino Core]: ---------------------------------
  [22:29:21] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f10 (first index): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
  [22:29:21] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
  [22:29:21] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 384
  [22:29:21] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 576
  [22:29:21] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
  [22:29:21] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 1
  [22:29:21] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f10 (first index): 1152
  [22:29:21] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
  [22:29:21] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
  [22:29:21] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 128
  [22:29:21] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 192
  [22:29:21] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
  [22:29:21] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): -1
  [22:29:21] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 2
  [22:29:21] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f10 (first index): 2304
  [22:29:21] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
  [22:29:21] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
  [22:29:21] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 256
  [22:29:21] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 384
  [22:29:21] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
  [22:29:21] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): -1
  [22:29:21] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): -1
  [22:29:21] [Client thread/INFO] [Kirino Core]: ---------------------------------
  [22:29:21] [Client thread/INFO] [Kirino Core]: dispatch 67
  [22:29:21] [Client thread/INFO] [Kirino Core]: ---------------------------------
  [22:29:21] [Client thread/INFO] [Kirino Core]: f00 (dirty index): 3
  [22:29:21] [Client thread/INFO] [Kirino Core]: f01 (old index count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f02 (old vertex count): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f10 (first index): 3456
  [22:29:21] [Client thread/INFO] [Kirino Core]: f11 (index count): 192
  [22:29:21] [Client thread/INFO] [Kirino Core]: f12 (vertex count): 128
  [22:29:21] [Client thread/INFO] [Kirino Core]: f20 (couters 0: vertex): 1516
  [22:29:21] [Client thread/INFO] [Kirino Core]: f21 (couters 1: index): 2274
  [22:29:21] [Client thread/INFO] [Kirino Core]: f22 (meshlet block count): 32
  [22:29:21] [Client thread/INFO] [Kirino Core]: f30 (chunk pos x): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f31 (chunk pos y): 0
  [22:29:21] [Client thread/INFO] [Kirino Core]: f32 (chun pos z): -1
  ```
  
  </details>
