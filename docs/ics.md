# Immediate Client Services

There are no complicated service wrappers here.
The purpose of this class is to provide common client side 
services outside the engine lifecycle as early
as possible.

With `ICS`, you'll be able to create GL shaders, draw texts and simple vector graphics, etc.

> See:<br>
> - [SimpleText Usage](simpletext/simpletext_usage.md)
> - [SimpleGUI Usage](simplegui/simplegui_usage.md)

## Timeline

```
--|---Splash Process---|--
  ^
  Everything is supposed to be ready right before the Splash screen
```

```
--|---Splash Process---|--
  ^                    ^
  |                    |
  Default vanilla font is loaded here
                       |
                       Resource pack font is loaded here
```

## Usage Patterns

The availability of multiple services are conditioned, including
- `text`
- `gui`
- `dummyVao`
- `textVanilla`

But their accessors are guaranteed to be non-null and fail fast.

Availability of `text`, `gui`, `dummyVao` is determined immutably 
once `ICS` is constructed.

```java
// either do 

if (textAvailable()) {
    text();
}
if (guiAvailable()) {
    gui();
}
if (dummyVaoAvailable()) {
    dummyVao();
}

// OR

assertFullAvailability();
// you could cache the borrowed runtimes here if available
text();
gui();
dummyVao();
```

`textVanilla` does follow the same pattern but the lifecycle and initialization
behind it is much more complicated.

```java
if (textVanillaAvailable()) {
    // never cache the result since backend instance might be replaced by reloading
    textVanilla();
}
```

The availability of `textVanilla` is more than immutably determined due to
the reloading mechanism, and `assertFullAvailability` does not guarantee its availability.

Every resource pack reload will trigger a `textVanilla` reload,
but a `textVanilla` reload call not necessarily performs the heavy reload work.
The system tries to reduce the amount of actual reload aggressively.

## Availability Summary

- `text` requires GL46
- `gui` requires GL46
- `dummyVao` requires GL30
- `textVanilla` requires GL46 AND other conditions including font loading working properly

Other services have no requirement.
