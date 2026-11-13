## Direct Usage

```java
ICS.instance().text()
    .begin()
    .append("font size 20", 0, 0, 20, Color.WHITE.getRGB())
    .appendBelow("font size 15", 15, Color.WHITE.getRGB())
    .appendBelow("font size 10", 10, Color.WHITE.getRGB())
    .endDraw();
```

Similar to Minecraft's `FontRenderer`, SimpleText default implementation doesn't 
prepare GL states for the draw call. You'll have to take care of the GL state changes.
Notice that immediate services are intrinsically the antipattern of our immutable pipeline design,
so GL state leaks are inevitable.

## Transform contexts

`begin()` & `endDraw()` use a NDC canvas by default. 
To opt in custom transformations, pass a `UiTransformContext` to `begin(context)`. 
The same overload is available on `SimpleGuiRuntime`.

UI geometry still converts logical GUI coordinates to canonical NDC first. 
The context then maps NDC to homogeneous clip space. 

It never sets any GL state including framebuffer, GL viewport, depth, blending or scissor state. 

### Opt in legacy GL matrices

```java
glPushMatrix();
glTranslatef(20.5f, 30.25f, 0);
glScalef(0.75f, 0.75f, 1);
text.begin(UiTransformContext.legacyGui())
        .append("Text", 0, 0)
        .endDraw();
glPopMatrix();
```

### A top-left canvas in the world

```java
UiWorldCanvas canvas = UiWorldCanvas.builder(320, 180)
        .position(worldX, worldY, worldZ)
        .unitsPerWorldUnit(200)
        .rotation(orientation)
        .build();

// snapshot again when the camera changes
UiTransformContext context = UiTransformContext.hologram(
        UiCameraSnapshot.from(camera), 
        canvas);

gui.begin(context)
        .append(commands -> commands.rect(0, 0, 320, 180, backgroundColor))
        .endDraw();
text.begin(context)
        .append("World canvas", 8, 8)
        .endDraw();
```

Position anchors logical `(0, 0)` at the world's top-left pivot. Rotation is around that pivot. 
`worldSize(width, height)` can set world dimensions independently instead of choosing a uniform density. 

Choose the appropriate scene depth/blend state in the caller's render pass. 
An inherited scissor remains a framebuffer-space rectangle; it does not become a rotated canvas clip.
The transform context does not add local clipping or input hit testing.

## Assumptions

- Features like MSDF, ligature, BiDi, shaping are out of scope
- One Codepoint = One Glyph = One Quad (we deliberately keep it simple)
- Producer-consumer pattern (every part is replaceable)

## Advanced Usage

> Relevant classes:
> <br>· `com.cleanroommc.kirino.ui.simpletext.sdf.SDFGenerator`
> <br>· `com.cleanroommc.kirino.ui.simpletext.SimpleTextConsumer`
> <br>· `com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer`
> <br>· `com.cleanroommc.kirino.ui.simpletext.ST_FontHandle`

First of all, let's have a look at an example `SimpleTextRuntime` setup.
```java
public SimpleTextRuntime(
            @NonNull BiFunction<@Nullable ResourceLocation, ST_Config, ST_FontHandle> fontFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextConsumer> consumerFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextProducer> producerFactory,
            @NonNull ImmediateShaderAccess shaderAccess,
            @NonNull ST_Config config,
            @NonNull ResourceLocation fontRl)
```
```java
ST_Config config = new ST_Config(
                ST_FontBackendType.FREE_TYPE,
                48,
                16,
                12,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

textRuntime = new SimpleTextRuntime(
        (rl, cfg) -> {
            FT_Face face = freeTypeManager.load(rl, 0, cfg.pixelSize());
            return new FreeTypeFontHandle(face);
        },
        (context) -> {
            return new DebugTextRenderer(
                    context,
                    new SDFGeneratorBruteForceImpl(context.getConfig().sdfPadding(), context.getConfig().sdfSpread()),
                    new Tex2DGlyphAtlas(1024, 1024),
                    context.getShaderAccess());
        },
        (context) -> {
            return new DefaultTextProducer(context, context.getConfig().pixelSize());
        },
        shaderAccess,
        config,
        new ResourceLocation("kirino:fonts/jetbrains/jetbrains_mono_nl_regular.ttf"));
```

You should create your own text runtime to take full control.

### 1. **`fontFactory`**
```java
(rl, cfg) -> {
    FT_Face face = freeTypeManager.load(rl, 0, cfg.pixelSize());
    return new FreeTypeFontHandle(face);
}
```

`fontFactory` is designed to return a `ST_FontHandle` with the given `ResourceLocation` and `ST_Config`.

`FreeTypeManager.load(rl, faceIndex, pixelSize)` is how you load a font face with FreeType. 
You can get access to `FreeTypeManager` via `ICS.instance().freetype()` for your own text runtime.

`FreeTypeFontHandle` is an implementation of the interface `ST_FontHandle`.
It wraps a FreeType `FT_Face` object so SimpleText can understand and communicate with it.

It's totally fine and recommended to implement other `ST_FontHandle` child classes to utilize
other font rasterizer libs, _**BUT**_ you'll have to modify Kirino Engine source to actually add the support (presumably via PRs)

**Additional `ST_FontHandle` Steps**:
- Add a new enum type to `ST_FontBackendType`
- Implement the corresponding `ST_FontHandle` (following the spec listed in the Javadoc)
- Hide interactions with native libs elsewhere

### 2. **`consumerFactory`**
```java
(context) -> {
    return new DebugTextRenderer(
            context,
            new SDFGeneratorBruteForceImpl(context.getConfig().sdfPadding(), context.getConfig().sdfSpread()),
            new Tex2DGlyphAtlas(1024, 1024),
            context.getShaderAccess());
}
```

`consumerFactory` is designed to return a `SimpleTextConsumer` with the given `SimpleTextRuntime`.

The arguments you'll have to input to the constructor totally depend on your `SimpleTextConsumer` implementation.

**Tips**:
- `context.getShaderAccess()` helps you to create shaders
- `Tex2DGlyphAtlas` is an implementation of `AbstractPagedAtlas<TPage, TBitmap extends ST_Bitmap>`.
  You're free to implement your own atlas
- `SDFGeneratorBruteForceImpl` is an implementation of `SDFGenerator`.
  You're free to implement your own SDF generator

### 3. **`producerFactory`**
```java
(context) -> {
    return new DefaultTextProducer(context, context.getConfig().pixelSize());
}
```

`producerFactory` is designed to return a `SimpleTextProducer` with the given `SimpleTextRuntime`.

And `SimpleTextProducer` is where you implement your text shaping logic. The default implementation is
heavily coupled with FreeType since it fetches the kerning info from FreeType.

### Scope & Limitations
The scope of SimpleText is simply creating a text runtime for the engine editor GUIs.
As a result, MSDF, ligature, BiDi, and advanced features are automatically out of scope for SimpleText.

However, the interfaces and specs are all modular and codepoint oriented (instead of `char`s). e.g. It's totally
possible to implement emoji rendering with your own `SimpleTextProducer` & `SimpleTextConsumer` implementations.

For more details, please look into the source code and Javadoc.

## Advanced Usage Examples

Without a ResourceLocation
```java
float logicalWidth = 720, logicalHeight = 360;
ST_Config config = new ST_Config(
        ST_FontBackendType.FREE_TYPE, 48, 16, 12,
        FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);
FT_Face face = freeType.load(fontResource, 0, config.pixelSize());

// custom shader source loader: works before Minecraft's resource manager is ready
ImmediateShaderAccess shaders = ImmediateShaderAccess.fromSourceLoader(rl -> {
    try (InputStream input = MinecraftResourceUtils.getEarlyInputStream(rl)) {
        return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
});

AttributeLayout layout = new AttributeLayout();
layout.push(new Stride(0));
VAO dummyVao = new VAO(layout, null, (VBOView[]) null);

SimpleGuiRuntime gui = new SimpleGuiRuntime(shaders, dummyVao, logicalWidth, logicalHeight);
SimpleGuiRuntime underline = new SimpleGuiRuntime(shaders, dummyVao, logicalWidth, logicalHeight);
SimpleGuiRuntime strike = new SimpleGuiRuntime(shaders, dummyVao, logicalWidth, logicalHeight);
SimpleTextRuntime text = new SimpleTextRuntime(
        cfg -> new FreeTypeFontHandle(face),
        runtime -> new DefaultTextRenderer(
                runtime, 
                new Tex2DArrayGlyphAtlas(1024, 1024), 
                shaders, 
                256,
                logicalWidth, 
                logicalHeight),
        runtime -> new DefaultTextProducer(runtime, config.pixelSize()),
        shaders, 
        underline, 
        strike, 
        config);
```

With a ResourceLocation
```java
import com.cleanroommc.kirino.utils.ResourceLocation;

ResourceLocation fontResource = new ResourceLocation(
        "kirino:fonts/jetbrains/jetbrains_mono_nl_regular.ttf");

SimpleGuiRuntime underline = new SimpleGuiRuntime(shaders, dummyVao, logicalWidth, logicalHeight);
SimpleGuiRuntime strike = new SimpleGuiRuntime(shaders, dummyVao, logicalWidth, logicalHeight);
SimpleTextRuntime resourceText = new SimpleTextRuntime(
        (rl, cfg) -> new FreeTypeFontHandle(freeType.load(rl, 0, cfg.pixelSize())),
        runtime -> new DefaultTextRenderer(
                runtime, 
                new Tex2DArrayGlyphAtlas(1024, 1024), 
                shaders, 
                256,
                logicalWidth, 
                logicalHeight),
        runtime -> new DefaultTextProducer(runtime, runtime.getConfig().pixelSize()),
        shaders,    
        underline, 
        strike, 
        config, 
        fontResource);
```
