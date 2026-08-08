# Oracle Testing

This repository includes an external geometry oracle workflow that compares `kcsg` boolean results against OpenSCAD outputs rendered with independent backends (`cgal`, `manifold`).

## Why

- `kcsg` and OpenSCAD use different implementations for boolean geometry.
- Agreement on volume/bounds across engines is a strong regression signal for non-visual 3D math changes.
- This is additive to normal unit tests; it is intended as a high-confidence validation gate.

## Commands

Run oracle fixture generation:

```bash
./gradlew oracleFixtures
```

Run oracle agreement tests:

```bash
./gradlew oracleTest
```

Direct module commands:

```bash
./gradlew :kcsg:oracleGenerateFixtures
./gradlew :kcsg:oracleTest
```

## Binary Bootstrap

`scripts/oracle/install_openscad.sh` downloads a pinned Linux AppImage from `files.openscad.org`:

- default artifact: `OpenSCAD-2026.01.02.ai30348-x86_64.AppImage`
- default source: `https://files.openscad.org/snapshots`
- checksum verified via the adjacent `.sha256` file before use
- bootstrap target: Linux `x86_64`

The wrapper executes with `APPIMAGE_EXTRACT_AND_RUN=1` so it works in environments without FUSE mounts.

## Fixture Location

Generated fixtures are written to:

- `kcsg/build/oracle-fixtures/cgal/*.stl`
- `kcsg/build/oracle-fixtures/manifold/*.stl`

The oracle test suite consumes these fixtures and compares them to `kcsg` outputs for the same scenarios.

## Updating the Pin

The pinned artifact name appears **three times** in `scripts/oracle/install_openscad.sh`: at `:7`
as the `OPENSCAD_ARTIFACT_NAME` default, at `:16` in the `--help` usage text, and at `:84` as the
fallback baked into the generated `.tools/openscad/openscad` wrapper. To move the pin permanently,
change all three — missing `:16` leaves `--help` advertising the old pin — then force a reinstall:

```bash
rm -rf .tools/openscad
./gradlew oracleFixtures
```

The `rm -rf` is required. `scripts/oracle/generate_openscad_oracles.sh` runs the installer only
when `.tools/openscad/openscad` is missing, so as long as a previous install left the wrapper in
place the new AppImage is never downloaded.

`OPENSCAD_ARTIFACT_NAME` and `OPENSCAD_BASE_URL` still work for a one-off trial, but only when you
invoke the scripts directly — the wrapper resolves `OPENSCAD_ARTIFACT_NAME` at execution time and
otherwise falls back to the name baked into it, failing with `Missing OpenSCAD AppImage: ...`:

```bash
rm -rf .tools/openscad
OPENSCAD_ARTIFACT_NAME=OpenSCAD-YYYY.MM.DD-x86_64.AppImage \
    ./scripts/oracle/generate_openscad_oracles.sh
```

Going through `./gradlew` for a trial is unreliable: `oracleGenerateFixtures` is a plain `Exec`
task, so the script sees the Gradle daemon's environment rather than the one you exported. Edit the
script defaults for anything that needs to hold across Gradle invocations.
