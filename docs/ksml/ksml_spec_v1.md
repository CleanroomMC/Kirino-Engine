# Kirino Shader Meta Language (KSML)

Goals:
- Composable shader code: reuse of utility methods and structs
- Diagnosability: transpile (KSML -> GLSL) stage error handling
- Independent of GLSL: KSML doesn't build or rely on a full GLSL AST.
  KSML only operates on source-level constructs

Non-goals:
- Not substituting GLSL syntax
- No actual compilation
- No forced GLSL coding style. All GLSL features are still available
- No runtime assembling

Besides `.glsl` shader files, a new type of reusable shader lib file `.ksml` will be introduced.

A KSML file consists of several parts:
- Header (meta directives like `@module name`, `@requires lib_abc`)
- Content (glsl code that defines methods and structs)
- No main method; No uniforms; No in/out/layout variables

A GLSL file under the context of KSML should include:
- A header that imports KSML libs (like `@import lib_123`)

**Example KSML file:**
```
@module math
@gl_version 120

@export
int add(int a, int b) 
{ 
    return a + b;
}
```
**Example GLSL file (that utilizes KSML):**
```
@import math

#version 120

void main()
{
    int sum = math.add(1, 2);
}
```

## KSML Syntax

### Module & Requires

```
@module <module_name>
@requires <dep_module_name>
```

For KSML file, it must:
- Specify a _unique_ module name
- Optionally require a module as its dependency; declare `@requires` multiple times for complicated dependencies

When assembling the shader source:
- The `@import` line must be replace by the actual KSML file's content
- Dependencies must also be copied to the shader source
- Cyclic dependencies must be explicitly handled by function prototype (supported by GLSL)
  ```
  void func2();

  void func1() { func2(); }
  void func2() { func1(); }
  ```

### GL Version Requirement
Per file annotation:
```
@gl_version <version_number>
```

Per method/struct annotation:
```
@gl_requires <version_number>
```

These annotations are introduced only for the compile-time diagnostic purposes.
They don't guarantee the code are strictly written in that GLSL version.

**Example**:
```
@module math
@gl_version 120

@gl_requires 330
@export
int add(int a, int b) 
{ 
    return a + b;
}

@export
int add_3(int a, int b, int c) 
{ 
    return a + b + c;
}
```
```
@import math

#version 120

void main()
{
    int sum = math.add(1, 2);  // this line will cause a compile-time error
    sum = math.add_3(1, 2, 3); // this line will not cause a compile-time error
}
```
As a result, even if the `math` lib consists of some advanced (GL 330) function, 
we can still import the lib safely in a legacy environment.

### GLSL Macro Handling
```
#ifdef FEATURE_PBR
@export
void addons() {}
#endif
```

The `addons` method _should not_ be visible to GLSL files when `FEATURE_PBR` is undefined, and a compile-time
error must be thrown.

Although this chunk of code is invisible to the GLSL compiler when the macro is undefined. The GLSL syntax `#ifdef` should still be respected and pre-processed in KSML.

### Method/Struct Export
`@export` is like a `public` modifier that exposes the method/struct to GLSL files.
```
void internalFunc()
{
    // this method is only visible to other KSML files not GLSL files
    // (no complicated package visibility)
}

@export
void func()
{
    // this method is visible to both KSML files and GLSL files
}

@export apple
void func2()
{
    // "apple" is only visible to GLSL files
    // both "func2" and "apple" are visible to KSML files
}
```

## GLSL Assembling
Goals:
- A method like `assemble(shader, libs)`

Non-goals:
- No file IO required
- No file scanning

Input:
- `String`: shader source
- `String[]`: KSML sources
- `String[]`: features (macros)

Output:
- `String`: assembled shader source

Naming conflict is avoided by explicit namespace usage like `math.add()`.

When assembling the shader:
- `math.add()` should be replaced by an conflict-free symbol like `math_add()`, and
  the methods inside KSML files should also be renamed to match that symbol.
  References like `module.symbol` must not appear in the final assembled GLSL source
- `#line 1 "module_name"` must be inserted to the start of KSML files to enable a more readable error log
- features (like `PBR`) must be added to the shader source as GLSL macros (`#define FEATURE_PBR`)
