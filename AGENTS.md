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
- `./gradlew :samples:run`: launch sample app entry points.

## Coding Style & Naming Conventions
- Follow `.editorconfig`: 4-space indentation, LF line endings, max line length 100, no trailing commas.
- Use package roots under `com.monkopedia.kcsg`.
- Naming: `UpperCamelCase` for classes/files, `lowerCamelCase` for methods/fields, `SCREAMING_SNAKE_CASE` for constants.
- Keep geometry operations deterministic and side-effect-light.

## Testing Guidelines
- Framework: JUnit 4 (`org.junit.Test`, `org.junit.Assert`).
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
- **Checklist (coverage/intent doc)** — `docs/public-api-checklist.md` records which public symbols are tested and with what scenarios. If any public API files change under `kcsg/src/main/java/com/monkopedia/kcsg/*.kt` or `kcsg-dsl/src/main/java/com/monkopedia/kcsg/*.kt`, update the checklist in the same change.
- Before opening a PR, run `./gradlew apiCheck :kcsg:test :kcsg-dsl:test :kcsg:koverXmlReport :kcsg:koverVerifyJvm :kcsg-dsl:koverVerify` (after `apiDump` if you changed public API).
- `:kcsg:koverVerifyJvm` enforces API-package coverage for `com.monkopedia.kcsg` (excluding vendored `com.monkopedia.kcsg.ext.*`), while `:kcsg:koverXmlReport` preserves full-module trend visibility.
- PR CI enforces all three: the BCV ABI check, the library coverage gate, and the public API checklist update requirement.

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
- Any PR that changes public API in `kcsg` or `kcsg-dsl` must update `docs/public-api-checklist.md` in the same change.
- PRs should include change summary, affected modules, and exact verification commands run (for example, `./gradlew :kcsg:test` or `./gradlew build`).
- Include sample output notes or screenshots when rendering behavior or exported mesh artifacts change.
