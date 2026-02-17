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

- default artifact: `OpenSCAD-2025.06.03.ai25586-x86_64.AppImage`
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

To upgrade the OpenSCAD build, set:

- `OPENSCAD_ARTIFACT_NAME` (for example `OpenSCAD-YYYY.MM.DD-x86_64.AppImage`)
- optionally `OPENSCAD_BASE_URL`

Then rerun `./gradlew oracleFixtures`.
