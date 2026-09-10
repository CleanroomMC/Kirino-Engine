## Direct Usage

```java
ICS.instance().gui().begin().append((s) -> {
    s.rectEx(10, 10, 15, 15, COLOR.getRGB())
        .radius(5f, 1)
        .emit();
    s.rectEx(35, 10, 15, 15, COLOR.getRGB())
        .radius(6f, 3)
        .emit();
    s.rectEx(60, 10, 15, 15, COLOR.getRGB())
        .radius(7f, 5)
        .emit();
});
```

Similar to Minecraft's `Tesselator`, SimpleGui default implementation doesn't
prepare GL states for the draw call. You'll have to take care of the GL state changes.
Notice that immediate services are intrinsically the antipattern of our immutable pipeline design,
so GL state leaks are inevitable.
