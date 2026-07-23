
# Timeline

> Relevant classes:
> <br>· `com.cleanroommc.kirino.engine.process.graphics.view.GraphicsWorldViewImpl`
> <br>· `com.cleanroommc.kirino.KirinoClientCore`

General timeline:

```java
void renderUpdate()
{
    PREPARE();
    
    ... // outside world (Minecraft logic)

    PRE_UPDATE();
    
    ... // outside world (Minecraft logic)

    UPDATE();
    
    ... // outside world (Minecraft logic)

    RENDER_OPAQUE();
    
    ... // outside world (Minecraft logic)
    
    RENDER_TRANSPARENT();
    
    ... // outside world (Minecraft logic)
    
    POST_UPDATE();
    
    ... // outside world (Minecraft logic)
            
    RENDER_OVERLAY();
}
```

<br>

> Note:<br>
> The phrase `GL Requirements` doesn't mean that you can't interact with the raw GL, but
> - You must set the states back if you want a transient mutation
> - You must not call `glKnowledge.commit()` to mutate the claimed keys (aka `requirements`)

> Note:<br>
> Everything about `GL Requirements` is explicitly guaranteed by the GL knowledge model.

## For `PRE_UPDATE`

GL Requirements:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`
- `COLOR_MASK_R/G/B/A` = `true`
- `DEPTH_MASK` = `true`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

## For `UPDATE`

GL Requirements:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

## For `RENDER_OPAQUE`

GL Requirements:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

## For `RENDER_TRANSPARENT`

GL Requirements:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

## For `POST_UPDATE`

`BEFORE` GL Requirements:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`

`AFTER` GL Requirements:
- `FBO_DRAW` = `minecraft`
- `FBO_READ` = `minecraft`
- `VIEWPORT` = `minecraft.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `main`
- `FBO_READ` = `main`
- `VIEWPORT` = `main.size`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

The framebuffer finalization happens during this phase, which includes post-processing.
See [post-processing](post_processing.md) for details.

## For `RENDER_OVERLAY`

GL Requirements:
- `FBO_DRAW` = `minecraft`
- `FBO_READ` = `minecraft`
- `VIEWPORT` = `minecraft.size`

Real GL states / knowledge you can expect at the beginning:
- `FBO_DRAW` = `minecraft`
- `FBO_READ` = `minecraft`
- `VIEWPORT` = `minecraft.size`
- `SHADER_PROGRAM` = `0`
- `VAO` = `0`
- `VBO` = `0`
- `EBO` = `0`
- `IDB` = `0`

## For `PREPARE`

`PREPARE` is the most specialized phase here. Engine initialization and resource allocation happens
during the first execution of the `PREPARE` phase.

Nothing is meant to happen for later `PREPARE` phases, but you can still attach callbacks to `PREPARE`.

Of course, you are free to register a `PREPARE` callback for your module initialization stuff. 
(Don't forget to maintain a `init` flag to only trigger initialization once)

