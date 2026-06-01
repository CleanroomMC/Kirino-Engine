Java's modifiers like `public`, `private`, `package-private` are limited when we want cross-package internal
dependencies. For example, when `package1.A` wants to access `package2.B.privateField`, reflection is the only way out.

## `MethodHolder` pattern
We have a set of rules and patterns to handle the cross-package dependencies.

```java
public final class ImmediateClientServices {

    private static final ImmediateClientServices instance = new ImmediateClientServices();

    public static ImmediateClientServices instance() {
        return instance;
    }
    
    private ImmediateClientServices() {
        freeTypeManager = MethodHolder.newFreeTypeManager();
    }

    private final FreeTypeManager freeTypeManager;

    public FreeTypeManager freetype() {
        return freeTypeManager;
    }

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
}
```

Whenever you want to access private members, create a privileged sealed inner class to obtain the access.

Here's the boilerplate:
```java
private static final class MethodHolder {
    static final Delegate DELEGATE;

    static {
        DELEGATE = new Delegate(ReflectionUtils...);

        Preconditions.checkNotNull(DELEGATE...);
    }

    static ... ...() {
            
    }

    record Delegate(MethodHandle ...) {
    }
}
```

Notice that:
- We enforce the usage of `ReflectionUtils` and `MethodHandle`
- We enforce the `Preconditions` checks
- It's not necessary to use a `record` when there is only one `MethodHandle` (but `record` is always fine)

Benefits:
- Less redundant and more tightened than a centralized public `InternalApi` class
- For hot paths, `static final MethodHandle` is likely to be inlined (faster than basic reflections)
- Although the `MethodHandle` bootstrapping cost is a disadvantage for one-time privileged access,
  we treat it as acceptable just to not break the consistency
- The privileged inner class is boilerplate heavy, preventing devs to do it too often actually

In general, we treat it as a compensation to the absence of the `friend` mechanism.
