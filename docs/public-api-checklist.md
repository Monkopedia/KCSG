# Public API Checklist

This checklist tracks public API coverage status for library modules.

## Update Rule

- Any change to public API in `kcsg` or `kcsg-dsl` must update this file in the same PR.
- Mark each symbol with one of: `missing`, `partial`, `covered`.
- Assign one or more scenario tags: `math`, `primitive`, `boolean`, `mesh`, `io`, `dsl`, `error-path`.
- Add or update test references for each symbol as coverage is implemented.

## Status Legend

- `missing`: no direct assertion tests for this symbol.
- `partial`: tested indirectly or only in subset scenarios.
- `covered`: direct tests cover nominal and relevant error/edge behavior.

## Module: kcsg

| Symbol | Tags | Status | Tests |
| --- | --- | --- | --- |
| `Bounds` | `math`, `mesh` | missing | n/a |
| `CSG` | `boolean`, `mesh`, `io`, `error-path` | missing | n/a |
| `Cube` | `primitive`, `mesh` | missing | n/a |
| `Cylinder` | `primitive`, `mesh` | missing | n/a |
| `Edge` | `math`, `mesh`, `error-path` | missing | n/a |
| `Extrude` | `primitive`, `mesh`, `error-path` | missing | n/a |
| `FileUtil` | `io`, `error-path` | missing | n/a |
| `HashingOpOverride` | `io`, `dsl`, `error-path` | missing | n/a |
| `MeshContainer` | `mesh`, `error-path` | missing | n/a |
| `ObjFile` | `io`, `mesh`, `error-path` | missing | n/a |
| `OpOverride` | `dsl`, `io` | missing | n/a |
| `Plane` | `math`, `mesh`, `error-path` | missing | n/a |
| `Polygon` | `math`, `mesh`, `error-path` | missing | n/a |
| `Polyhedron` | `primitive`, `mesh` | missing | n/a |
| `Primitive` | `primitive`, `mesh` | missing | n/a |
| `PropertyStorage` | `mesh`, `dsl` | missing | n/a |
| `RoundedCube` | `primitive`, `mesh` | missing | n/a |
| `STL` | `io`, `mesh`, `error-path` | missing | n/a |
| `Sphere` | `primitive`, `mesh` | missing | n/a |
| `Transform` | `math`, `mesh`, `error-path` | missing | n/a |
| `UnityModifier` | `primitive`, `mesh` | missing | n/a |
| `Vector3d` | `math`, `mesh`, `error-path` | missing | n/a |
| `Vertex` | `math`, `mesh` | missing | n/a |
| `WeightFunction` | `primitive`, `mesh` | missing | n/a |
| `XModifier` | `primitive`, `mesh` | missing | n/a |
| `YModifier` | `primitive`, `mesh` | missing | n/a |
| `ZModifier` | `primitive`, `mesh` | missing | n/a |

## Module: kcsg-dsl

| Symbol | Tags | Status | Tests |
| --- | --- | --- | --- |
| `CSGBuilder` | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.xyz` | `dsl`, `math` | missing | n/a |
| `Primitive.weighted` (DSL extension) | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.roundedCube` | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.cube` | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.cylinder` (start/end overload) | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.cylinder` (radius/height overload) | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.transform` | `dsl`, `math` | missing | n/a |
| `Collection<CSG>.transform` | `dsl`, `math`, `boolean` | missing | n/a |
| `Collection<CSG>.times(Transform)` | `dsl`, `math` | missing | n/a |
| `Collection<CSG>.translate` | `dsl`, `math` | missing | n/a |
| `Collection<CSG>.scale(Double)` | `dsl`, `math`, `error-path` | missing | n/a |
| `Collection<CSG>.scale(x,y,z)` | `dsl`, `math`, `error-path` | missing | n/a |
| `Collection<CSG>.rot` | `dsl`, `math` | missing | n/a |
| `Collection<CSG>.flatten` | `dsl`, `boolean` | missing | n/a |
| `Collection<CSG>.merge` | `dsl`, `boolean` | missing | n/a |
| `BuilderContext.arrayed` | `dsl`, `primitive` | missing | n/a |
| `BuilderContext.primitives` | `dsl`, `primitive` | missing | n/a |
| `CsgDsl` | `dsl` | missing | n/a |
| `ImportedKcsgScript` | `dsl`, `io` | missing | n/a |
| `ImportedScript` | `dsl`, `io` | missing | n/a |
| `KcsgBuilder` | `dsl`, `io`, `error-path` | missing | n/a |
| `KcsgHost` | `dsl`, `io` | missing | n/a |
| `EmptyHost` | `dsl`, `error-path` | missing | n/a |
| `KcsgScript` | `dsl`, `io`, `error-path` | missing | n/a |
| DSL boolean/set operator extensions (`and`, `or`, `xor`, `plus`, `minus`, `times`) | `dsl`, `boolean` | missing | n/a |
| `Cylinder.radius` (extension property) | `dsl`, `primitive`, `error-path` | missing | n/a |
| `TransformBuilder` | `dsl`, `math` | missing | n/a |
| `Transform.translate` (DSL extension) | `dsl`, `math` | missing | n/a |
| `Transform.scale(Double)` (DSL extension) | `dsl`, `math`, `error-path` | missing | n/a |
| `Transform.scale(x,y,z)` (DSL extension) | `dsl`, `math`, `error-path` | missing | n/a |
| `CSG.transform` (DSL extension) | `dsl`, `math`, `boolean` | missing | n/a |
| `CSG.times(Transform)` (DSL extension) | `dsl`, `math` | missing | n/a |
| `Primitive.times(Transform)` (DSL extension) | `dsl`, `primitive`, `math` | missing | n/a |
| `Primitive.transform` (DSL extension) | `dsl`, `primitive`, `math` | missing | n/a |
| `CSG.translate` (DSL extension) | `dsl`, `math` | missing | n/a |
| `CSG.scale(Double)` (DSL extension) | `dsl`, `math`, `error-path` | missing | n/a |
| `CSG.scale(x,y,z)` (DSL extension) | `dsl`, `math`, `error-path` | missing | n/a |
| `CSG.rot` (DSL extension) | `dsl`, `math` | missing | n/a |
