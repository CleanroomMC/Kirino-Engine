# Immediate Client Services

There are no complicated service wrappers here.
The purpose of this class is to provide common client side 
services outside the engine lifecycle as early
as possible.

With `ICS`, you'll be able to create GL shaders, draw texts and simple vector graphics, etc.

## Timeline

```
--|---Splash Process---|--
  ^
  Everything is supposed to be ready right before the Splash screen
```

# Usage Patterns

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
if (tryLoadTextRuntimeVanilla()) {
    // never cache the result since backend instance might be replaced by reloading
    textVanilla();
}
```

Don't worry that the `tryLoadTextRuntimeVanilla` call could cause loading and 
is slow. It's not and the system warms up `textVanilla` before the Splash process if possible.

For the try load call return value
- `false` means the text runtime is unavailable for the entire program lifetime,
  and `reload` cannot make it available
- Once `true` is returned, all subsequent calls that complete normally
  will also return `true`, including calls with `reload`

The availability of `textVanilla` is more than immutably determined due to
the reloading mechanism, but you can treat it as immutably determined since it does follow the pattern.

Every resource pack reload will trigger a `textVanilla` reload,
but a `textVanilla` reload call not necessarily performs the heavy reload work.
The system tries to reduce the amount of actual reload aggressively.
