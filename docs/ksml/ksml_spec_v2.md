# Kirino Shader Meta Language (KSML)

KSML is a runtime-agnostic engineering layer for GLSL. Unlike engine-specific shader systems, 
it's non-invasive and also provides modules, namespaces, and first-class IDE support.

IntelliJ IDEA Plugin: https://github.com/tttsaurus/ksml-intellij-plugin

## KSML Syntax

### `@import`

- Owner File Type: `.glsl` / (`.vert` / `.frag` / ...)
- Usage Pattern: `// @import <module_name>`
- Desc:<br>
  Put it at the beginning of a GLSL shader file to import modules
  (before the line GLSL version line `#version <gl_version>`)

Example:
```glsl
// @import test_module
// @import test_module_2

#version 330 core
```

**Notes**:<br>
- Declare `// @import <module_name>` multiple times to import multiple modules 
  instead of doing `// @import <module_name_1> <module_name_2>` which isn't allowed.
- If `module_a` relies on `module_b` internally, you're not responsible for importing `module_b` as well, 
  the compiler is responsible for resolving dependencies.
- For the compiler, matching `// @import` is a way to distinguish import lines between comment lines.

### `@module`

- Owner File Type: `.ksml`
- Usage Pattern: `@module <module_name>`
- Desc:<br>
  Put it at the beginning of a KSML shader file to define the module name

Example:
```ksml
@module test_module

```

**Notes**:<br>
- There are no restrictions on the module name, either snake case or camel case or whatever,
  but follow the regex specification `[A-Za-z_][A-Za-z_0-9]*`.
- You are allowed to have multiple files declaring the same module name, 
  but the compiler is only meant to take in the well selected source files with no module name conflict.
  (more like a trivial rule to be mentioned)

### `@requires`

- Owner File Type: `.ksml`
- Usage Pattern: `@requires <module_name>`
- Desc:<br>
  Put it after the `@module <module_name>` declaration to specify the module dependencies

Example:
```ksml
@requires test_module_2

```

**Notes**:<br>
- This declaration can be missing, which indicates no module dependency.
- Cyclic dependency is allowed (A → B; B → A), and the compiler is responsible for resolving dependencies

### `@gl_version`

- Owner File Type: `.ksml`
- Usage Pattern: `@gl_version <version_number> <version_ident>?`
- Desc:<br>
  Put it after the `@module <module_name>` declaration to specify the GL version requirement of this file.
  `<version_ident>?` can be missing, and it represents the profile type (`<version_ident>` can be `core` / `compat`).<br>
  **Note that** the meaning of an empty `<version_ident>` varies depending on the program creation context (which is dynamic).
  Specifically, empty `<version_ident>` represents the compat profile under a compat profile context, 
  but represents the core profile under a core profile context. For the compiler, the profile inference must be
  taken into the account since the compat profile is strictly a superset of the core profile when their GL version 
  numbers are the same. e.g. `GL330 compat` > `GL330 core` for the version comparison.

Examples:
```ksml
@gl_version 450 core

```
```ksml
@gl_version 450 compat

```
```ksml
@gl_version 450

```

**Notes**:<br>
- Versions other than 110, 120, 130, 140, 150, 330, 400, 410, 420, 430, 440, 450, 460 are invalid.
- The compiler should provide a config option for the program profile context hint 
  to infer the meaning of an empty `<version_ident>`.
- The GLSL shader file must have a higher GLSL version than its imported modules 
  (e.g. a valid situation: `GL330 compat` for the GLSL shader file and `GL330 core` for the KSML files). The compiler should
  otherwise warn the usage.

### `@code`

- Owner File Type: `.ksml`
- Usage Pattern: `@code """ <code_block> """`
- Desc:<br>
  Put it after the `@module <module_name>` declaration to declare your code blocks.
  A `<code_block>` must only contain one function declaration.

Example:
```ksml
@code """
// GLSL code
int func(int ok) {
    return 0;
}
"""
```

**Notes**:<br>
- You're allowed to utilize the GLSL language features inside your `<code_block>` 
  corresponding to the `@gl_version` declaration of this KSML file.
- You're allowed to utilize function overload, having multiple `@code` declaration with the same function signature,
  since the GLSL language handles function overload already. No extra work needed from the KSML compiler.

### `@export`

- Owner File Type: `.ksml`
- Usage Pattern: `@export`
- Desc:<br>
  Put it right before the `@code """ <code_block> """` declaration to annotate your code blocks.

Example:
```ksml
@export
@code """
// GLSL code
int func(int ok) {
    return 0;
}
"""
```

**Notes**:<br>
- An exported code block is visible to the GLSL shader files.
- An exported code block is visible to the KSML shader files.
- An unexported/internal code block is _**not**_ visible to the GLSL shader files. (an error should be thrown by the compiler)
- An unexported/internal code block is still visible the KSML shader files.

### `@gl_requires`

- Owner File Type: `.ksml`
- Usage Pattern: `@gl_requires <version_number> <version_ident>?`
- Desc:<br>
  Put it right before the `@code """ <code_block> """` declaration to annotate your code blocks.

Example:
```ksml
@gl_requires 330 core
@code """
// GLSL code
int func(int ok) {
    return 0;
}
"""
```

**Notes**:<br>
- It follows the same set of rules as `@gl_version`.
- It overrides the GL version requirement of this single code block.
  That is, the function inside the `@gl_requires` annotated code block may require a higher version 
  or lower version of GL.

### `@feature`

- Owner File Type: `.ksml`
- Usage Pattern: `@feature <feature_ident>`
- Desc:<br>
  Put it right before the `@code """ <code_block> """` declaration to annotate your code blocks.

Example:
```ksml
@feature PBR
@code """
// GLSL code
int func(int ok) {
    return 0;
}
"""
```

**Notes**:<br>
- It means the function inside the code block belongs to a certain feature.
- You can use any name for the feature. No need to register anything beforehand.
- The compiler should provide a config option for the features. Specifically,
  if a certain feature is toggled on, the corresponding code block is therefore visible to both GLSL and KSMl files.
  Otherwise, the code block should be treated as if it was not there.

### `module.function()`

- Owner File Type: `.ksml` / `.glsl` / (`.vert` / `.frag` / ...)
- Usage Pattern: Import the module and then call `module.function()`
- Desc:<br>
  Call `module.function()` inside valid code blocks to utilize functionalities provided by a certain KSML module.

Examples:
```glsl
// @import module_a
#version 110

void main() {
    module_a.func();
}
```
```ksml
@module module_b
@requires module_a
@gl_version 110

@code """
int func(int ok) {
    return module_a.func();
}
"""
```

**Notes**:<br>
- During the compilation, the period `.` symbol must be replaced by another valid symbol, presumably `_` or `__`.
