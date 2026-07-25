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

## Advanced Usage

> Relevant classes:
> <br>· `com.cleanroommc.kirino.ui.simpletext.sdf.SDFGenerator`
> <br>· `com.cleanroommc.kirino.ui.simpletext.SimpleTextConsumer`
> <br>· `com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer`
> <br>· `com.cleanroommc.kirino.ui.simpletext.ST_FontHandle`

First of all, let's have a look at our example `SimpleTextRuntime` setup.
```java
public SimpleTextRuntime(
            @NonNull BiFunction<ResourceLocation, ST_Config, ST_FontHandle> fontFactory,
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
