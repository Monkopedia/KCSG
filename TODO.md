# Public API Coverage TODO

Goal: 100% test coverage of public API in `kcsg` and `kcsg-dsl`, with deterministic assertions for geometry behavior and error paths.

## Phase 0 - Infrastructure First

- [x] Update dependencies to latest stable versions, prioritizing Kotlin `2.3.x` and aligned Gradle/plugin versions; run full build/tests after upgrade. [NOTE: `:kcsg:test` has 32 pre-existing sample-test failures on both baseline and upgraded builds.]
- [x] Add coverage plugin and reporting for library modules (`kcsg`, `kcsg-dsl`) in Gradle.
- [x] Add CI coverage task (`./gradlew :kcsg:koverVerify :kcsg-dsl:koverVerify` or equivalent).
- [ ] Enforce coverage threshold gate for library modules (start at current baseline, ratchet to 100%).
- [ ] Add a dedicated coverage HTML report artifact task for local inspection.
- [ ] Add shared geometry assertion helpers (`assertVectorClose`, `assertBoundsClose`, `assertFiniteMesh`, `assertVolumeClose`) in `kcsg/src/test/java/com/monkopedia/kcsg/testutil/`.
- [ ] Add reusable temporary file + stream fixture helpers for STL/OBJ I/O tests.
- [ ] Add deterministic random/data seed utility for fuzz-style geometry checks.
- [ ] Add fake `OpOverride` fixture to assert override routing behavior.
- [ ] Add fake `KcsgHost` fixture to test DSL builder/export/cache behavior.
- [ ] Add `docs/public-api-checklist.md` and require updates when API changes.

## Phase 1 - Public API Inventory and Mapping

- [ ] Generate and commit a checklist of all public symbols from `kcsg/src/main/java/com/monkopedia/kcsg/*.kt`.
- [ ] Generate and commit a checklist of all public symbols from `kcsg-dsl/src/main/java/com/monkopedia/kcsg/*.kt`.
- [ ] Mark each symbol in the checklist with test status (`missing`, `partial`, `covered`).
- [ ] Add scenario tags in checklist (`math`, `primitive`, `boolean`, `mesh`, `io`, `dsl`, `error-path`).

## Phase 2 - Core Math and Geometry Unit Coverage

- [ ] Add `Vector3dTest` covering `get`, `plus`, `minus`, scalar/vector `times`, `divided`, `dot`, `crossed`, `magnitude`, `magnitudeSq`, `angle`, `distance`, `orthogonal`, `normalized`, `negated`, `lerp`, `project`, `collinear`, transform overloads, STL/OBJ string output, factory methods (`x/y/z/xy/xyz/yz/xz/zero/unity`), constants (`UNITY/X_ONE/Y_ONE/ZERO/Z_ONE`), and invalid index path.
- [ ] Add `TransformTest` covering `to`, `rotX/rotY/rotZ`, `rot(x,y,z)`, `rot(vec)`, `rot(from,to)`, axis rotation overload, all translate/scale overloads, `mirror`, both `transform` overloads, `isMirror`, `apply`, `from`, `unity`, and all scale-by-zero `require` failures.
- [ ] Add `PlaneTest` covering `copy`, `flip`, `splitPolygon` classification (`COPLANAR`, `FRONT`, `BACK`, `SPANNING`), `createFromPoints`, and behavior with `USE_STACKOVERFLOW_WORKAROUND` enabled/disabled.
- [ ] Add `VertexTest` covering `flip`, `interpolate`, STL/OBJ formatting overloads, transform overloads, equality/hash semantics, and `toString`.
- [ ] Add `BoundsTest` covering `center`, `bounds`, `toCSG`, `toCube`, `contains` overloads (`Vertex`, `Vector3d`, `Polygon`), `intersects` overloads (`Polygon`, `Bounds`), and string output.
- [ ] Add `PropertyStorageTest` covering set/get typed lookup, delete, contains, random color initialization behavior.

## Phase 3 - Primitive and Mesh Construction Coverage

- [ ] Add `CubeTest` for all constructors, `toPolygons`, `toCSG`, `noCenter`, and bounds/volume invariants.
- [ ] Add `SphereTest` for both constructors, `toPolygons`, `toCSG`, resolution effects, and volume convergence tolerance.
- [ ] Add `CylinderTest` for all constructors, frustum vs cylinder behavior, `toPolygons`, `toCSG`, and slice-count effects.
- [ ] Add `RoundedCubeTest` for constructor variants, `toPolygons`, `noCenter`, and corner radius/resolution impact.
- [ ] Add `PolyhedronTest` for list and array constructors, `toPolygons`, and `getProperties`.
- [ ] Add `PrimitiveDefaultBehaviorTest` for `Primitive.toCSG` and storage propagation.

## Phase 4 - CSG Boolean and Transform API Coverage

- [ ] Add `CSGBooleanTest` for `union`, `difference`, `intersect` single/list/vararg overloads.
- [ ] Add `CSGHullTest` for `hull()` and list/vararg overloads.
- [ ] Add `CSGCoreTest` for `copy`, `polygons`, `optimization`, `dumbUnion`, `bounds`, `computeVolume`, `transformed`, `weighted`.
- [ ] Add `CSGSerializationTest` for `toStlString` overloads, `toObj`, `toObjString`, `color`.
- [ ] Add `CSGFactoryAndOverrideTest` for all `fromPolygons` overloads, `setDefaultOptType`, `opOverride`, and `withOverride` restoration semantics.
- [ ] Add `CSGOptTypeParityTest` validating result parity across `NONE`, `CSG_BOUND`, and `POLYGON_BOUND` for representative operations.

## Phase 5 - Polygon, Edge, Extrude, and Topology Coverage

- [ ] Expand `Polygon` tests to cover constructors, `isValid`, `copy`, `flip/flipped`, STL conversion overloads, `toTriangles`, `translate/translated`, `transform/transformed`, `bounds`, `centroid`, `contains(Vector3d)`, `contains(Polygon)`, `storage` getter/setter, `fromPoints` overloads, and `fromConcavePoints` overloads.
- [ ] Add `PolygonErrorPathTest` for invalid polygon creation (`<3` vertices) and deprecated `intersects` unsupported path.
- [ ] Expand `Edge` tests to cover `contains`, `equals/hashCode`, `getClosestPoint`, `getIntersection`, `toVertices`, `toPoints`, `toPolygons`, `polygons`, `boundaryPathsWithHoles`, and `boundaryPolygons`.
- [ ] Add `ExtrudeTest` for `points` overloads, `combine`, `toCW`, `isCCW`, top/bottom toggles, and negative-z rejection path.

## Phase 6 - File and Hashing API Coverage

- [ ] Add `FileUtilTest` for `write`, `read`, `toStlFile`.
- [ ] Add `STLTest` for `STL.file` and `STL.from` using both file and stream sources.
- [ ] Add `ObjFileTest` for `toFiles`, `obj`, `mtl`, `objStream`, `mtlStream`, and extension normalization.
- [ ] Add `HashingOpOverrideTest` for deterministic hash behavior over `CSG`, `Transform`, `Vector3d`, primitives, missing/existing file paths, and input stream hashing.
- [ ] Add explicit test that `CSG.withOverride` restores previous override after exception.

## Phase 7 - Modifiers, Weight Functions, and Misc Public APIs

- [ ] Add `WeightFunctionAndModifierTest` for `WeightFunction.eval` via `CSG.weighted`.
- [ ] Add `AxisModifierTest` for `XModifier`, `YModifier`, `ZModifier`, and centered/non-centered behavior.
- [ ] Add `UnityModifierTest` for constant weight behavior.
- [ ] Add `MeshContainerTest` for dimensions, bounds, meshes/materials accessors, `getAsMeshViews`, and constructor precondition mismatch.
- [ ] Add `OpOverrideContractTest` covering custom override dispatch paths from `CSG`, `STL`, `Bounds`, and `Primitive`.

## Phase 8 - `kcsg-dsl` Full Coverage (Currently 0 Tests)

- [ ] Add `DslPrimitiveFactoryTest` for `BuilderContext.xyz`, `cube`, `roundedCube`, `cylinder` overloads, and `Primitive.weighted`.
- [ ] Add `DslOperationsTest` for all `and/or/xor/plus/minus/times` overloads across `CSG` and `Primitive`.
- [ ] Add `DslTransformExtensionsTest` for `TransformBuilder.unity`, `Transform.translate/scale` extension overloads, `CSG.transform`, `CSG.times(Transform)`, `Primitive.times(Transform)`, `Primitive.transform`, and `CSG.translate/scale/rot`.
- [ ] Add `DslCollectionExtensionsTest` for collection `transform`, `times`, `translate`, `scale` overloads, `rot`, `flatten`, `merge`, `arrayed`, and `primitives`.
- [ ] Add `CylinderRadiusPropertyTest` for `Cylinder.radius` extension getter/setter and uneven-cylinder failure path.
- [ ] Add `KcsgBuilderTest` for `primitive`, `csg`, `import`, `stl`, `export`, and caching/override behavior.
- [ ] Add `KcsgScriptTest` for `overrideExport`, `generateExports`, `generateTarget`, `exports`, `targets`, and host-backed cache delegation.
- [ ] Add `ImportedScriptTest` for `ImportedKcsgScript` (`exports`, `targets`, `get`).
- [ ] Add `KcsgHostTest` for `EmptyHost` default behavior and error paths.

## Phase 9 - Primitive Interaction Matrix (High Detail Regression Set)

- [ ] Create a reusable scenario harness (`GeometryScenarioHarness`) that runs each case under all optimization modes.
- [ ] Add matrix case `S1` disjoint solids: union volume equals sum, intersection is empty, difference unchanged.
- [ ] Add matrix case `S2` partial overlap: intersection non-empty, union volume less than sum, bounds correctness.
- [ ] Add matrix case `S3` full containment: subtraction cavity semantics and containment invariants.
- [ ] Add matrix case `S4` identical solids: idempotence of union/intersection and empty difference.
- [ ] Add matrix case `S5` face tangency: near-zero intersection volume, no invalid polygons.
- [ ] Add matrix case `S6` edge tangency: stable topology, finite coordinates, no degenerate blow-ups.
- [ ] Add matrix case `S7` vertex tangency: same invariants as tangency stress cases.
- [ ] Add matrix case `S8` transformed overlap: rotation/translation/scale invariance checks.
- [ ] Add matrix case `S9` optimization parity across `NONE`, `CSG_BOUND`, `POLYGON_BOUND`.
- [ ] Execute S1-S9 for pairs: `Cube-Cube`, `Cube-Sphere`, `Cube-Cylinder`, `Sphere-Sphere`, `Sphere-Cylinder`, `Cylinder-Cylinder`.
- [ ] Execute S1-S4 + S8 for `RoundedCube` interactions (`RoundedCube-Cube`, `RoundedCube-Sphere`).
- [ ] Execute S1-S4 + S8 for `Polyhedron` interactions (`Polyhedron-Cube`, `Polyhedron-Sphere`).
- [ ] For all matrix outputs, assert manifold sanity checks (finite vertices, no zero-area triangles after triangulation).

## Phase 10 - CI Ratchet and Long-Term Maintenance

- [ ] Add CI job that runs full library tests and coverage verification on pull requests.
- [ ] Add CI failure for missing update to `docs/public-api-checklist.md` when public API changes.
- [ ] Add contributor guidance in `AGENTS.md` referencing the checklist and coverage gate workflow.
- [ ] Raise module thresholds to 100% once all above tasks are complete and stable.
- [ ] Add a final audit pass that maps each public symbol to at least one explicit test method name.
