# ReflectionUtils

> Relevant classes:
> <br>· `com.cleanroommc.kirino.utils.ReflectionUtils`

It provides a set of utility functions to get a `MethodHandle` for methods/fields/constructors.

Common usage pattern:
```java
// create an inner class to hold MethodHandle
private static final class MethodHolder {
    static final Delegate DELEGATE;

    static {
        DELEGATE = new Delegate(ReflectionUtils.getConstructor(FreeTypeManager.class));

        Preconditions.checkNotNull(DELEGATE.freeTypeManagerCtor);
    }

    static FreeTypeManager newFreeTypeManager() {
        try {
            return (FreeTypeManager) DELEGATE.freeTypeManagerCtor.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    record Delegate(MethodHandle freeTypeManagerCtor) {
    }
}
```
