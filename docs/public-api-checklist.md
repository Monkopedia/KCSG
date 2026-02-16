# Public API Checklist

This checklist tracks public API coverage status for library modules.

## Update Rule

- Any change to public API in `kcsg` or `kcsg-dsl` must update this file in the same PR.
- Mark each symbol with one of: `missing`, `partial`, `covered`.
- Add or update test references for each symbol as coverage is implemented.

## Status Legend

- `missing`: no direct assertion tests for this symbol.
- `partial`: tested indirectly or only in subset scenarios.
- `covered`: direct tests cover nominal and relevant error/edge behavior.

## Module: kcsg

| Symbol | Status | Tests |
| --- | --- | --- |
| `Bounds` | missing | n/a |
| `CSG` | missing | n/a |
| `Cube` | missing | n/a |
| `Cylinder` | missing | n/a |
| `Edge` | missing | n/a |
| `Extrude` | missing | n/a |
| `FileUtil` | missing | n/a |
| `HashingOpOverride` | missing | n/a |
| `MeshContainer` | missing | n/a |
| `ObjFile` | missing | n/a |
| `OpOverride` | missing | n/a |
| `Plane` | missing | n/a |
| `Polygon` | missing | n/a |
| `Polyhedron` | missing | n/a |
| `Primitive` | missing | n/a |
| `PropertyStorage` | missing | n/a |
| `RoundedCube` | missing | n/a |
| `STL` | missing | n/a |
| `Sphere` | missing | n/a |
| `Transform` | missing | n/a |
| `UnityModifier` | missing | n/a |
| `Vector3d` | missing | n/a |
| `Vertex` | missing | n/a |
| `WeightFunction` | missing | n/a |
| `XModifier` | missing | n/a |
| `YModifier` | missing | n/a |
| `ZModifier` | missing | n/a |

## Module: kcsg-dsl

| Symbol | Status | Tests |
| --- | --- | --- |
| _Populate in Phase 1 inventory task_ | missing | n/a |
