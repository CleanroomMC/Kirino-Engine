# Contributing to Kirino Rendering

Kirino Engine is a domain specific engine designed for Minecraft.
- Original proposal (the current scope is slightly more generalized): [Link](https://github.com/CleanroomMC/Cleanroom/discussions/405)
- Project board (todo list): [Link](https://github.com/orgs/CleanroomMC/projects/13)

***

## Getting Started

- Fork this repo (branch: `main`)
- Clone [Cleanroom](https://github.com/CleanroomMC/Cleanroom) (branch: `fix/lwjgl`) locally
- Go to `.gitmodules`
  ```
  [submodule "projects/kirino"]
    path = projects/kirino
    url = https://github.com/CleanroomMC/Kirino-Engine.git
  ```
- Set the `url` to your fork
- ```bash
  git submodule init
  git submodule update
  ```
- Import `build.gradle` and then `./gradlew setup`

**Dev Tips**
- `./gradlew cleanroomClient` to run the project
- `./gradlew build` to build the project
- `./gradlew genPatches` to generate patches if you modified Minecraft source code
  (btw you'll have to push to Cleanroom repo if you modified Minecraft source; 
  it'd be the best you contact us first before doing so)
- `Cleanroom/projects/cleanroom/src/main/java/` is where you modify Minecraft source code
- `Cleanroom/projects/kirino/src/main/java/` is where you modify your kirino fork

## Ways to Contribute

- Report bugs via [Issues](https://github.com/CleanroomMC/Kirino-Engine/issues)
- Improve / add more java docs (typos, explanations, tutorials)
- Add unit tests / coverage tests (`Cleanroom/projects/kirino/src/test/java/`)
- Implement features. (Check [Project Board](https://github.com/orgs/CleanroomMC/projects/13) / Propose your own)
  Contact me, tttsaurus, (via Discord or GitHub issues) if you want to implement anything
- Propose specific features you want to have via Discord or GitHub issues
- Propose general ideas about Kirino Engine via [Proposal](https://github.com/CleanroomMC/Cleanroom/discussions/405)

## Code Style Convention

### General

- Use `camelCase` for methods / fields.
- Use K&R brace styling.
  
  **Bad:**
  ```java
  while (true)
  {
      // code
  }
  ```
  **Good:**
  ```java
  while (true) {
      // code
  }
  ```
- Use braces for all control statements. i.e. Always use braces `{}` for `if`, `for`, `while`, etc., 
  even if the body has only one line.
  
  **Bad:**
  ```java
  if (escape) return;
  ```
  ```java
  if (escape)
      return;
  ```
  **Good:**
  ```java
  if (escape) {
      return;
  }
  ```
- Keep lines reasonably short to maintain readability, but there is no explicit char count limit.
- Don't use `this.` if not necessary.

  **Bad:**
  ```java
  Ctor(int newValue) {
      this.value = newValue;
  }
  ```
  **Good:**
  ```java
  Ctor(int newValue) {
      value = newValue;
  }
  ```
- Add a blank line after precondition checks / early escape / chunks of code.

  **Bad:**
  ```java
  Ctor(int value) {
      Preconditions.checkArgument(value ...);
      this.value = value;
  }
  ```
  **Good:**
  ```java
  Ctor(int value) {
      Preconditions.checkArgument(value ...);
  
      this.value = value;
  }
  ```
- Start a sentence / phrase with a capital letter in Javadoc.

  **Bad:**
  ```java
  /**
   * this is...
   *
   * @param a this parameter...
   */
  Ctor(int a) {
  }
  ```
  **Good:**
  ```java
  /**
   * This is...
   *
   * @param a This parameter...
   */
  Ctor(int a) {
  }
  ```
- Line-comments begin with a lowercase letter.

  **Bad:**
  ```java
  void func() {
      // Let's...
  }
  ```
  **Good:**
  ```java
  void func() {
      // let's...
  }
  ```
- Add space after line-comment (`//`).

  **Bad:**
  ```java
  void func() {
      //let's...
  }
  ```
  **Good:**
  ```java
  void func() {
      // let's...
  }
  ```
- All error messages end with a period (`.`).

  **Bad:**
  ```java
  void func(int value) {
      Preconditions.checkArgument(value >= 0, "Argument \"value\"=%s must not be non-negative", value);
  }
  ```
  **Good:**
  ```java
  void func(int value) {
      Preconditions.checkArgument(value >= 0, "Argument \"value\"=%s must not be non-negative.", value);
  }
  ```
- Use `\" \"` to quote parameters in error messages. It'd be the best you output the values via `%s` too.

  **Good (Use it as a template):**
  ```java
  void func(int value) {
      Preconditions.checkArgument(value >= 0, "Argument \"value\"=%s must not be non-negative.", value);
  }
  ```
- Add a single space after each comma in a list of items, parameters, or arguments.

  **Bad:**
  ```java
  void func() {
      func1(new float[]{1f});
      func2(1,2,3,4+5,new float[]{1f,2f});
  }
  ```
  **Good:**
  ```java
  void func() {
      func1(new float[]{1f}); // no space needed for a singleton array; { 1f } is also fine
      func2(1, 2, 3, 4 + 5, new float[]{1f, 2f}); // { 1f, 2f } is also fine
  }
  ```
- Add a single space everywhere if possible.

  **Bad:**
  ```java
  if(a==1){
      return;
  }
  ```
  **Good:**
  ```java
  if (a == 1) {
      return;
  }
  ```
- Use `//<editor-fold desc="your desc">` & `//</editor-fold>` if the code chunk is huge and boilerplate heavy 
  _OR_ you think it would be necessary.

### Advanced

- Use `Jspecify` for `Nullable` / `NonNull` annotations; Use `Nullable` / `NonNull` on public APIs and where you think necessary.
- Use google `Preconditions` to check argument / state / nonnull preconditions instead of `Objects.requireNonNull()` or manual if-statement etc.
- Use `Preconditions` to check `NonNull` even after the `NonNull` annotations.

  **Example:**
  ```java
  @Override
  public IBuilder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
      Preconditions.checkNotNull(state, "Provided \"state\" can't be null.");
      // Preconditions.checkNotNull(state); is allowed too. Explain the error when you think it's necessary
  }
  ```
- Avoid return null and use `Optional<T>` if you think it's necessary. Avoid hotpaths for `Optional<T>` too.

### Best Practices for Reflection
TLDR: simply follow [Privileged Enclave](docs/privileged_enclave.md) rules.

- Use `ReflectionUtils` methods to get reflection-based references wherever possible.
- Get inaccessible fields, methods, or constructors using `MethodHandle`s and `VarHandle`s, not `Field`s / `Method`s / `Constructor`s.
  - If simply checking existence of fields / methods / constructors, classic reflection is fine.
  - For `set`ting valid `final` fields, classic reflection is necessary.
- If caching handles, cache them in a `static final` field directly or in a `record`.
  This allows the JVM to inline them for the fastest performance.  
  See [this article](https://jornvernee.github.io/methodhandles/2024/01/19/methodhandle-primer.html#method-handle-inlining) for more details.
  - Use a record if caching more than one handle.
- Don't use `invoke` if not necessary; use `invokeExact`.
- Write helper methods for invoking method handles and avoid `try...catch` blocks spread throughout your code.

***

**_Finally, while no style guide can cover every situation, maintaining consistency is preferred!_**
