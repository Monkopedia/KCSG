# Repository Guidelines

## Project Structure & Module Organization
This repository is a multi-module Gradle project:
- `kcsg/`: core CSG geometry library (`src/main/java`), with multiplatform tests in `src/commonTest` and JVM-specific tests/resources in `src/test/java` and `src/jvmTest/resources`.
- `kcsg-dsl/`: Kotlin DSL wrappers and scripting helpers built on top of `kcsg`.
- `csgs/`: command-line/script runner (`com.monkopedia.csgs.MainKt`) plus packaging tasks.
- `samples/`: runnable sample models and playground entry points.

## Build, Test, and Development Commands
- `./gradlew build`: compile all modules and run tests.
- `./gradlew test` or `./gradlew :kcsg:test`: run library unit/regression tests (excludes oracle suite).
- `./gradlew :kcsg:oracleGenerateFixtures`: bootstrap pinned OpenSCAD and generate external oracle STL fixtures.
- `./gradlew :kcsg:oracleTest` or `./gradlew oracleTest`: validate `kcsg` boolean outputs against OpenSCAD (`cgal`/`manifold`) oracle fixtures.
- `./gradlew :kcsg:oracleTest -Pkcsg.oracle.quick=true`: local quick mode for remesh oracle checks (full mode remains default).
- `./gradlew :csgs:run --args "<path/to/script.csgs>"`: execute a script with the CLI.
- `./gradlew :csgs:fatJar`: produce a distributable `csgs-all-*.jar`.
- `./gradlew :samples:run -Pkcsg.sample=<SampleName>`: launch one sample entry point, e.g. `-Pkcsg.sample=Spheres`. The name is resolved against `com.monkopedia.kcsg.samples`; pass a dotted name to launch anything else (`-Pkcsg.sample=com.monkopedia.kcsg.playground.Main`). Without the property it runs `RoundedCubeSample`. Available samples are the files under `samples/src/main/java/com/monkopedia/kcsg/samples/`; each writes its STL/OBJ into `samples/build/sample-output/`.
- `./scripts/consumer-smoke.sh`: publish `:kcsg` and `:kcsg-dsl` to a throwaway file repository and compile `consumer-smoke/`, a separate build whose two modules each depend on one published coordinate and nothing else. Run it after changing a dependency declaration in either published module. Not part of `check`; CI runs it as its own job.

## Coding Style & Naming Conventions
- Follow the conventions in `.editorconfig`: 4-space indentation, LF line endings, max line length 100, no trailing commas. **Nothing in the build enforces these** — there is no ktlint or spotless configuration anywhere in the project, so `.editorconfig` is IDE-only guidance and the existing sources do not fully satisfy it. Match the surrounding file; do not reformat unrelated code to chase these rules.
- Use package roots under `com.monkopedia.kcsg`.
- Naming: `UpperCamelCase` for classes/files, `lowerCamelCase` for methods/fields, `SCREAMING_SNAKE_CASE` for constants.
- Keep geometry operations deterministic and side-effect-light.

## Testing Guidelines
- Framework: `kotlin.test` (`kotlin.test.Test`, `kotlin.test.assertEquals`) is the default and the only option for multiplatform tests. JUnit 4 (`org.junit.Test`, `org.junit.Assert`) appears only in JVM-only source sets (`src/test/...`) — the oracle suite, JVM file I/O, and the `csgs` CLI tests.
- Tests in `src/commonTest` **must** use `kotlin.test`; `org.junit` does not compile for the JS, Wasm or native targets and will break the multiplatform build.
- Test file naming: `*Test.kt` (for example, `VolumeTest.kt`, `EdgeIntersectionTest.kt`).
- Prefer regression tests for boolean operations, extrusions, and mesh edge cases.
- When outputs are mesh artifacts, store expected JVM fixtures in `kcsg/src/jvmTest/resources`.
- For bug fixes, add at least one assertion-based regression test in the same change.

## Quality Gates
- Do not merge with failing tests, even if failures appear unrelated.
- Remove temporary debug prints and unused code paths in the same PR that deprecates them.
- Avoid broad `catch (Throwable)` without explicit rationale and recovery behavior.

## Coverage & API Gates
There are two complementary public-API gates; a public-API change must satisfy both:
- **BCV (mechanical ABI gate)** — `binary-compatibility-validator` tracks the actual public ABI of `:kcsg` and `:kcsg-dsl` (JVM `.api` + multiplatform `.klib.api` under each module's `api/`). Any public-API change must be reflected by running `./gradlew apiDump` and committing the updated dumps; CI runs `./gradlew apiCheck` and fails on an undumped change. `:csgs` and `:samples` are not libraries and are excluded.
- **Checklist (coverage/intent doc)** — `docs/public-api-checklist.md` records which public symbols are tested and with what scenarios. The gate keys off the **BCV dumps, not source paths**: `coverage.yml` diffs `kcsg/api/` and `kcsg-dsl/api/` against the PR base, and requires `docs/public-api-checklist.md` to change in the same PR only when those dumps change. A refactor that touches API-path source files but leaves the ABI byte-identical does not need a checklist edit.
- Before opening a PR, run the command CI runs (`coverage.yml`), plus `apiDump` first if you changed public API:
  `./gradlew apiCheck :kcsg:test :kcsg-dsl:test :kcsg:oracleTest :csgs:test :samples:build :kcsg:koverXmlReport :kcsg:koverVerifyJvm :kcsg-dsl:koverXmlReport :kcsg-dsl:koverVerify`
  CI additionally executes the `commonTest` suites on JS, Wasm and linuxX64 in a separate job (`:kcsg:jsNodeTest`, `:kcsg:wasmJsNodeTest`, `:kcsg:wasmWasiNodeTest`, `:kcsg:linuxX64Test` and the `:kcsg-dsl` equivalents); run those too if you touched `commonMain`/`commonTest`.
  The same job also runs the js/wasmJs suites in headless Chrome (`:kcsg:jsBrowserTest`, `:kcsg:wasmJsBrowserTest` and the `:kcsg-dsl` equivalents). Locally these need a browser karma can find: export `CHROME_BIN=$(command -v chromium)` if you do not have `google-chrome` on `PATH`. Browser mocha timeouts come from `<module>/karma.config.d/`, not from the `useMocha` DSL — calling `useMocha` inside `browser { testTask { .. } }` swaps karma out for a node mocha runner, which changes the npm dependency set (invalidating `kotlin-js-store/yarn.lock`) and stops the tests running in a browser at all.
- `:kcsg:koverVerifyJvm` enforces API-package coverage for `com.monkopedia.kcsg` (excluding vendored `com.monkopedia.kcsg.ext.*`), while `:kcsg:koverXmlReport` preserves full-module trend visibility.
- PR CI enforces all three: the BCV ABI check, the library coverage gate, and the public API checklist update requirement.

## Dependency Declaration Rule
- A dependency whose types appear in a **public signature** of `:kcsg` or `:kcsg-dsl` must be declared `api(...)`, not `implementation(...)`. Gradle publishes `implementation` dependencies only into the runtime variant, so on jvm/js/wasm a consumer resolving the artifact from a repository cannot name the types the API hands it. `apiCheck` cannot detect this — it validates the ABI's shape, not whether the ABI is resolvable. `./scripts/consumer-smoke.sh` is the gate that can.

## Architecture Guardrails
- Keep core mesh/boolean algorithms in `kcsg`; `kcsg-dsl` and `csgs` should orchestrate, not duplicate core logic.
- Prefer small, cohesive files; split files that grow beyond roughly 600 lines by responsibility.
- Use injectable collaborators for file I/O, clocks, or random behavior to keep tests deterministic.
- For caching or memoization, document keying and invalidation strategy and test both hit and refresh paths.

## Autonomous TODO Execution
- If `TODO.md` exists and has clear unchecked items (`- [ ]`), execute them autonomously in order without waiting for prompts.
- Selection rule: pick the top-most unchecked item in the highest remaining section, then continue sequentially.
- For each completed item:
  - implement the change end-to-end,
  - run verification before commit (`./gradlew build` plus narrower module tests when appropriate),
  - mark the item as complete in `TODO.md` (`- [x]`),
  - create a focused commit,
  - immediately start the next unchecked item.
- If an item is blocked (missing credentials, external dependency, unclear requirement), append a durable marker in `TODO.md` as `[BLOCKED: <reason>]`, commit that update, and continue to the next actionable item.
- Stop only when no actionable unchecked items remain or explicit user intervention is required.

## Non-Prompting Execution Rules
- For clear `TODO.md` work, do not ask permission-seeking questions; execute and report outcomes.
- Ask the user only when truly blocked by missing required input, unavailable credentials, or explicit approval requirements.
- When blocked, ask one concise unblock question, then resume autonomous execution.
- Do not ask the user to run commands that can be run by the agent.

## Commit & Pull Request Guidelines
- Use short, imperative commit subjects (for example, `Fix bug in splitPolygons`, `Bump patch version`).
- Keep commits focused by module or behavior; avoid unrelated refactors in feature fixes.
- Any PR that changes the public ABI of `kcsg` or `kcsg-dsl` — i.e. that changes the BCV dumps under `kcsg/api/` or `kcsg-dsl/api/` — must update `docs/public-api-checklist.md` in the same change. Touching a source file under an API path without moving the dumps does not trigger this.
- PRs should include change summary, affected modules, and exact verification commands run (for example, `./gradlew :kcsg:test` or `./gradlew build`).
- Include sample output notes or screenshots when rendering behavior or exported mesh artifacts change.
