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
| `CSGBuilder` | missing | n/a |
| `BuilderContext.xyz` | missing | n/a |
| `Primitive.weighted` (DSL extension) | missing | n/a |
| `BuilderContext.roundedCube` | missing | n/a |
| `BuilderContext.cube` | missing | n/a |
| `BuilderContext.cylinder` (start/end overload) | missing | n/a |
| `BuilderContext.cylinder` (radius/height overload) | missing | n/a |
| `BuilderContext.transform` | missing | n/a |
| `Collection<CSG>.transform` | missing | n/a |
| `Collection<CSG>.times(Transform)` | missing | n/a |
| `Collection<CSG>.translate` | missing | n/a |
| `Collection<CSG>.scale(Double)` | missing | n/a |
| `Collection<CSG>.scale(x,y,z)` | missing | n/a |
| `Collection<CSG>.rot` | missing | n/a |
| `Collection<CSG>.flatten` | missing | n/a |
| `Collection<CSG>.merge` | missing | n/a |
| `BuilderContext.arrayed` | missing | n/a |
| `BuilderContext.primitives` | missing | n/a |
| `CsgDsl` | missing | n/a |
| `ImportedKcsgScript` | missing | n/a |
| `ImportedScript` | missing | n/a |
| `KcsgBuilder` | missing | n/a |
| `KcsgHost` | missing | n/a |
| `EmptyHost` | missing | n/a |
| `KcsgScript` | missing | n/a |
| DSL boolean/set operator extensions (`and`, `or`, `xor`, `plus`, `minus`, `times`) | missing | n/a |
| `Cylinder.radius` (extension property) | missing | n/a |
| `TransformBuilder` | missing | n/a |
| `Transform.translate` (DSL extension) | missing | n/a |
| `Transform.scale(Double)` (DSL extension) | missing | n/a |
| `Transform.scale(x,y,z)` (DSL extension) | missing | n/a |
| `CSG.transform` (DSL extension) | missing | n/a |
| `CSG.times(Transform)` (DSL extension) | missing | n/a |
| `Primitive.times(Transform)` (DSL extension) | missing | n/a |
| `Primitive.transform` (DSL extension) | missing | n/a |
| `CSG.translate` (DSL extension) | missing | n/a |
| `CSG.scale(Double)` (DSL extension) | missing | n/a |
| `CSG.scale(x,y,z)` (DSL extension) | missing | n/a |
| `CSG.rot` (DSL extension) | missing | n/a |
