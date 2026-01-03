# Assumptions & Design

Kirino-Engine is built on a set of explicit assumptions.

## Assumption 1: Minecraft as a Data Source, Not a Compatibility Target

This can be stated explicitly and even evexaggerated:
- We **_do not_** aim to provide the **_best_** performance and compatibility
- We **_do not_** aim to prove the superiority over Sodium/Iris,
  since they don't share the same assumption space
- We **_serve as a platform_** for exploring the feasibility of modern, GPU-driven rendering techniques within Minecraft’s constraints
- As a consequence, users can still expect the engine to experiment with
  cutting-edge rendering techniques where feasible, rather than focusing on incremental _marginal optimizations_

## Assumption 2: Rendering Architecture Should Be Explicit

What do we avoid: implicit feature growth that gradually hides responsibilities.

**Bad example**:
```java
// # first version
update();
render();

// # updated version
// - add multi-threading to update() with if-else coordination
// - add fancy camera functionalities
updateAndMore();
renderAndMore();

// # updated version
// - add anaglyph logic
updateAndMore();
if (anaglyph)
{
    renderAndMore(0);
    renderAndMore(1);
}
else
{
    renderAndMore(2);
}

// # updated version
// - add OpenCL acceleration
updateAndMoreButWithSomeOpenCL();
if (anaglyph)
{
    renderAndMoreOpenCLPatch(0);
    renderAndMoreOpenCLPatch(1);
}
else
{
    renderAndMoreOpenCLPatch(2);
}
```

In contrast, an explicit architecture represents features as data and structure,
allowing behavior to be composed.

## Assumption 3: CPU Orchestration & GPU Execution

What do we avoid:
- CPU control increasingly mirrors GPU logic
- GPU computation is treated as an optimization patch
- Responsibilities between CPU and GPU become blurred

**Bad example**:
```java
// # first version
for (Object obj : objects)
{
    if (obj.visible(camera))
    {
        draw(obj);
    }
}

// # updated version
// - add frustum culling
// - add LOD selection
for (Object obj : objects)
{
    if (!obj.inFrustum(camera))
        continue;

    int lod = obj.selectLOD(camera);
    draw(obj, lod);
}
```

In contrast, when GPU execution is treated as the primary execution model,
CPU control naturally becomes more declarative and readable.

## Assumption 4: Architecture Matters More Than Micro-optimizations

What do we avoid:
- Control becomes tightly coupled to low-level details
- Fragile performance gains

**Bad example**:
```java
// # first version
for (int i = 0; i < meshes.size(); i++)
{
    draw(meshes[i]);
}

// # updated version
// - precompute constants
// - manual loop unrolling
for (int i = 0; i < meshes.size(); i += 4)
{
    drawFast(meshes[i]);
    drawFast(meshes[i + 1]);
    drawFast(meshes[i + 2]);
    drawFast(meshes[i + 3]);
}
```

Micro-optimizations harden assumptions.
In constrast, architecture keeps them flexible.
