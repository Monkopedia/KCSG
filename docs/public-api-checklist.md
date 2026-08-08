# Public API Checklist

This checklist tracks public API coverage status for library modules.

## Relationship to BCV (the mechanical ABI gate)

There are two complementary public-API gates, with distinct roles:

- **BCV (`binary-compatibility-validator`)** is the *mechanical ABI gate*. It tracks the actual public ABI of `:kcsg` and `:kcsg-dsl` as committed dumps (JVM `.api` + multiplatform `.klib.api` under each module's `api/`) and fails CI (`./gradlew apiCheck`) on any public-API change that hasn't been re-dumped with `./gradlew apiDump`. It answers *"did the public surface change?"* — exhaustively and automatically.
- **This checklist** is the *coverage/intent doc*. It answers *"which public symbols are tested, and how well?"* BCV does not track test coverage, and this checklist does not mechanically detect surface changes; they are intentionally separate. Both are required on a public-API PR.

## Update Rule

- Any change to public API in `kcsg` or `kcsg-dsl` must update this file in the same PR (and re-run `./gradlew apiDump` so the BCV dumps match — see above).
- Mark each symbol with one of: `missing`, `partial`, `covered`.
- Assign one or more scenario tags: `math`, `primitive`, `boolean`, `mesh`, `io`, `dsl`, `error-path`.
- Add or update test references for each symbol as coverage is implemented.

## Status Legend

- `missing`: no direct assertion tests for this symbol.
- `partial`: tested indirectly or only in subset scenarios.
- `covered`: direct tests cover nominal and relevant error/edge behavior.

## Coverage Gate Scope

- `kcsg` has two coverage views:
- Full-module trend report: `./gradlew :kcsg:koverXmlReport` (includes vendored `com.monkopedia.kcsg.ext.*` packages).
- API gate: `./gradlew :kcsg:koverVerifyJvm` scoped to `com.monkopedia.kcsg` and excluding `com.monkopedia.kcsg.ext.*`.
- `kcsg-dsl` gate remains module-scoped via `./gradlew :kcsg-dsl:koverVerify`.

## Recent API Notes

- `Edge.equals`/`Edge.hashCode` no longer contradict each other on endpoint order. `equals` stays direction-independent (`Edge(a, b) == Edge(b, a)`, which `boundaryEdgesOfPlaneGroup` relies on to count an edge and its reverse as one), but it now requires a genuine pairing of endpoints instead of allowing both of the other edge's endpoints to match the *same* endpoint of this edge — that made every degenerate `Edge(a, a)` equal to every edge incident to `a`, and broke symmetry and *structural* transitivity. `hashCode` is now symmetric in `p1`/`p2` (`p1.hashCode() + p2.hashCode()`; addition rather than `xor`, which collapses every `Edge(a, a)` onto one constant). **Scope of the fix — read before relying on it:** `Edge` is still **not** safe as a `HashSet`/`HashMap` key, and `Edge.equals` is still **not** a true equivalence relation. Both remaining defects live one level down in `Vector3d`/`Vertex`, not in `Edge`: `Vector3d.equals` compares with a tolerance (`Plane.TOL`, 1e-12) while `Vector3d.hashCode` hashes the exact bit pattern, so `-0.0` and `0.0` are equal with different hash codes (a guaranteed counterexample, and cylinder caps produce `-0.0` from `cos`/`sin`), and tolerance comparison is non-transitive by construction (`x = 0`, `1e-12`, `2e-12`). `Edge` derives both its equality and its hash from its vertices, so no `Edge`-level change can repair either — tracked as issue #65. Consequently the `O(n^2)` `edges.count { it == e }` scan in `boundaryEdgesOfPlaneGroup` **must stay**: replacing it with a hash-based frequency count (`edges.groupingBy { it }.eachCount()`) changes boundary output on ordinary models, including a plain `Cylinder(1.0, 2.0, 8)` (10 → 9 boundary polygons) and several STL fixtures. **No public API change** — signatures are unchanged and `apiCheck` is byte-identical. Boundary output is unchanged for well-formed input (verified byte-for-byte across 20 models: primitives, boolean chains, hull, extrude and coplanar groups); it can differ only for polygons carrying a zero-length edge, where the old comparison was non-transitive and the result was ill-defined either way. Regression tests: `EdgeApiTest.reversedEdgesHashAlikeAndCollapseInHashContainers`, `EdgeApiTest.equalsIsTransitiveAroundDegenerateEdges`.
- **Breaking:** removed `Edge.Companion.polygons(List<Edge>, Plane)`. It was a semantically identical duplicate of `Edge.Companion.toPolygons(List<Edge>, Plane)` — inherited from upstream JCSG's leftover `_toPolygons`, which the Kotlin conversion renamed into a legitimate-looking public name. `toPolygons` is the single entry point and is unchanged; callers of `polygons` should switch to it. **Public API change** — dumps regenerated via `./gradlew apiDump` (`kcsg/api/kcsg.api` and `kcsg/api/kcsg.klib.api` lose the `polygons` entry). Binary- and source-breaking, so the next release is a minor bump (0.5.0).
- Removed a leftover `print("edge: ...")` debug call from the private `Edge.Companion.boundaryPaths` walk. **No public API change** — the surrounding `logger` calls already trace the walk.
- `CSG.color(...)` now mutates the receiver (including every polygon's `PropertyStorage`) and returns `this`, instead of returning a colored copy while writing the color where nothing read it. **No public API change** — signatures are unchanged and `apiCheck` is byte-identical — but it is a behavior change: coloring the result of `union`/`difference`/`intersect`/`hull` now takes effect (it previously silently no-op'd), chained `color()` calls are last-wins, and coloring a union collapses its per-primitive materials to one color. Use `csg.copy().color(...)` for the non-mutating form.
- Fixed binary STL import (`STL.file` / `STL.from` for binary STL): the internal `STLLoader.readFully` passed kotlinx-io `readAtMostTo`'s third argument as a length instead of the END INDEX it expects, so any read that straddled a kotlinx-io segment boundary (i.e. any non-tiny binary STL) under-read and then threw, aborting the import. **No public API change** — internal fix in vendored `ext/imagej/STLLoader.kt`; regression test added (`STLTest.binaryStlReadFullyHandlesReadsAcrossSegmentBoundaries`).
- `CSG.difference(List)`/`intersect(List)` and the polygon-bounds union/difference optimizations now use stdlib `partition()`/`reduce()` internally instead of hand-rolled loops. **No public API change** — private/body-only refactor; signatures are unchanged and `apiCheck` is byte-identical.
- Removed a dead commented-out Java block from `Polygon.kt` (an abandoned `concaveToConvex` triangulation experiment + a nested QuickHull3D attempt). **No public API change** — the deleted lines were inert comments.
- `KcsgHost` gains `stlVersion(stlName): String` (default `""`). It is folded into the cache key of an `stl()` property so a host that returns a version token (e.g. the source STL's modification time) invalidates cached results when the imported STL changes. The default `""` preserves prior name-only keying, so existing host implementations are unaffected.
- `STL.file` / `STL.from` now parse ASCII STL coordinates as `Double` instead of `Float`. ASCII STL is written with full double precision, so the prior float32 parse truncated each coordinate to ~7 significant digits; that error flipped BSP plane-classification on subsequent boolean ops, so a cached intermediate CSG reloaded from STL was not equivalent to the freshly-computed one (the disk cache was not transparent). Binary STL remains float32 (inherent to the format).
- `XModifier`/`YModifier`/`ZModifier` now expose a public read-only `centered` property (previously private). `HashingOpOverride` now incorporates the `WeightFunction` passed to `CSG.weighted` into the cache key (built-in modifiers are hashed by type + `centered`; a custom `WeightFunction` throws during hashing — disable caching via `csg(allowCaching = false)` for such builds). The hasher also now content-hashes `Polygon` (was identity `hashCode`), length-frames `Polyhedron` points/faces, and its fallback branch fails loud instead of silently using `hashCode()`. These change content hashes for affected models (cache cold-starts once).
- `Cube.centered` is now a public `var` (previously private), mirroring `RoundedCube.centered`. The `HashingOpOverride` cache key for `Cube` now incorporates this flag, so a `Cube` and its `noCenter()` variant no longer collide to the same content hash. Existing cache entries for `Cube`-containing models are invalidated (hashes change once).
- `io`-surface APIs now use `kotlinx.io.files.Path` as the primary path abstraction across `kcsg` and `kcsg-dsl`.
- `io` stream-surface APIs now use `kotlinx.io.Source` (for example `OpOverride.source`, `STL.from`, and `ObjFile.objSource`/`mtlSource`).
- JVM compatibility extensions are provided for renamed/retargeted APIs (legacy `InputStream`/`java.nio.file.Path` call patterns and `ObjFile.objStream`/`mtlStream`).
- `Edge` closest-point/intersection APIs now use nullable returns (`getClosestPointOrNull`/`getIntersectionOrNull`) and keep JVM-only legacy `Optional` wrappers in `JvmIoCompat`.
- `Logger`/`TaggedLogger` delegate logging APIs are now part of `kcsg` public API and are backend-agnostic (no direct SLF4J dependency required in `kcsg`/`kcsg-dsl`).
- `kcsg` and `kcsg-dsl` are now built as Kotlin Multiplatform libraries (JVM/JS/Wasm/native families), with portable API tests moved to `commonTest`; JVM-only test coverage remains for legacy JVM wrappers and filesystem-specific paths.

## Module: kcsg

| Symbol | Tags | Status | Tests |
| --- | --- | --- | --- |
| `Bounds` | `math`, `mesh` | covered | `BoundsTest.centerAndBoundsAreDerivedFromMinAndMax` |
| `CSG` | `boolean`, `mesh`, `io`, `error-path` | covered | `CSGBooleanTest.singleOverloadsProduceExpectedVolumes`; `PrimitiveInteractionMatrixTest.scenarioS9OptimizationParity`; `CSGRemeshTest.remeshTriangulatesPolygonsAndPreservesVolumeAndBounds`; `CSGSerializationTest.colorAppliesToBooleanOperationResult`; `CSGSerializationTest.chainedColorCallsKeepTheLastColor`; `CSGSerializationTest.coloringACopyLeavesTheSourceUntouched` |
| `Cube` | `primitive`, `mesh` | covered | `CubeTest.constructorsInitializeCenterAndDimensions`; `CubeTest.centeredFlagDefaultsTrueAndTogglesWithNoCenter`; `CubeTest.noCenterShiftsCubeToPositiveOctantFromOrigin` |
| `Cylinder` | `primitive`, `mesh` | covered | `CylinderTest.constructorsPopulateGeometryFields` |
| `Edge` | `math`, `mesh`, `error-path` | covered | `EdgeApiTest.containsEqualsAndHashCode`, `EdgeApiTest.reversedEdgesHashAlikeAndCollapseInHashContainers`, `EdgeApiTest.equalsIsTransitiveAroundDegenerateEdges` |
| `Extrude` | `primitive`, `mesh`, `error-path` | covered | `ExtrudeTest.pointsOverloadsProduceEquivalentExtrusions` |
| `FileUtil` | `io`, `error-path` | covered | `FileUtilTest.writeAndReadRoundTrip` |
| `HashingOpOverride` | `io`, `dsl`, `error-path` | covered | `HashingOpOverrideTest.hashingSequenceIsDeterministicForSameInputs`; `HashingOpOverrideTest.builtInWeightFunctionsAreHashedDistinctly`; `HashingOpOverrideTest.customWeightFunctionFailsLoud`; `HashingOpOverrideTest.polygonIsHashedByContentNotIdentity`; `HashingOpOverrideTest.polyhedronFaceGroupingAffectsHash`; `HashingOpOverrideTest.unhashableTypeFailsLoud` |
| `Logger` | `io`, `dsl`, `error-path` | covered | `LoggerDelegateTest.companionDispatchesAllLevelsWithTagAndThrowable` |
| `TaggedLogger` | `io`, `dsl` | covered | `LoggerDelegateTest.taggedLoggerUsesConfiguredTag` |
| `KcsgColor` | `mesh` | covered | `CSGSerializationTest.colorInfluencesMaterialOutputAndUnsupportedObjArgThrows`; `CSGSerializationTest.chainedColorCallsKeepTheLastColor` |
| `ObjFile` | `io`, `mesh`, `error-path` | covered | `ObjFileTest.objAndMtlAccessorsAndStreams` |
| `OpOverride` | `dsl`, `io` | covered | `OpOverrideContractTest.overrideDispatchesAcrossCsgStlBoundsAndPrimitiveEntrypoints` |
| `Plane` | `math`, `mesh`, `error-path` | covered | `PlaneTest.splitPolygonClassifiesAllCases` |
| `Polygon` | `math`, `mesh`, `error-path` | covered | `PolygonComprehensiveTest.constructorsAndIsValid` |
| `Polyhedron` | `primitive`, `mesh` | covered | `PolyhedronTest.listAndArrayConstructorsPopulatePointsAndFaces` |
| `Primitive` | `primitive`, `mesh` | covered | `PrimitiveDefaultBehaviorTest.toCsgUsesToPolygonsResult` |
| `PropertyStorage` | `mesh`, `dsl` | covered | `PropertyStorageTest.setAndTypedGetLookup` |
| `RoundedCube` | `primitive`, `mesh` | covered | `RoundedCubeTest.constructorsPopulateFields` |
| `STL` | `io`, `mesh`, `error-path` | covered | `STLTest.stlFileLoadsCsgFromDisk`; `STLTest.asciiStlRoundTripPreservesDoublePrecision` |
| `Sphere` | `primitive`, `mesh` | covered | `SphereTest.constructorsPopulateProperties` |
| `Transform` | `math`, `mesh`, `error-path` | covered | `TransformTest.unityFromAndToMatrixValues` |
| `UnityModifier` | `primitive`, `mesh` | covered | `UnityModifierTest.unityModifierAlwaysReturnsOne` |
| `Vector3d` | `math`, `mesh`, `error-path` | covered | `Vector3dTest.factoryMethodsAndConstants` |
| `Vertex` | `math`, `mesh` | covered | `VertexTest.flipAndInterpolate` |
| `WeightFunction` | `primitive`, `mesh` | covered | `WeightFunctionAndModifierTest.weightedInvokesWeightFunctionAndAppliesWeightsToCopy` |
| `XModifier` | `primitive`, `mesh` | covered | `AxisModifierTest.xModifierSupportsCenteredAndNonCenteredModes` |
| `YModifier` | `primitive`, `mesh` | covered | `AxisModifierTest.yModifierSupportsCenteredAndNonCenteredModes` |
| `ZModifier` | `primitive`, `mesh` | covered | `AxisModifierTest.zModifierSupportsCenteredAndNonCenteredModes` |

## Module: kcsg-dsl

| Symbol | Tags | Status | Tests |
| --- | --- | --- | --- |
| `CSGBuilder` | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.xyz` | `dsl`, `math` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `Primitive.weighted` (DSL extension) | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.roundedCube` | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.cube` | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.cylinder` (start/end overload) | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.cylinder` (radius/height overload) | `dsl`, `primitive` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `BuilderContext.transform` | `dsl`, `math` | covered | `DslPrimitiveFactoryTest.primitiveFactoriesAndWeightedExtension` |
| `Collection<CSG>.transform` | `dsl`, `math`, `boolean` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.times(Transform)` | `dsl`, `math` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.translate` | `dsl`, `math` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.scale(Double)` | `dsl`, `math`, `error-path` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.scale(x,y,z)` | `dsl`, `math`, `error-path` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.rot` | `dsl`, `math` | covered | `DslCollectionExtensionsTest.collectionTransformTimesTranslateScaleAndRot` |
| `Collection<CSG>.flatten` | `dsl`, `boolean` | covered | `DslCollectionExtensionsTest.flattenAndMergeProduceEquivalentResults` |
| `Collection<CSG>.merge` | `dsl`, `boolean` | covered | `DslCollectionExtensionsTest.flattenAndMergeProduceEquivalentResults` |
| `BuilderContext.arrayed` | `dsl`, `primitive` | covered | `DslCollectionExtensionsTest.arrayedAndPrimitivesBuilderHelpers` |
| `BuilderContext.primitives` | `dsl`, `primitive` | covered | `DslCollectionExtensionsTest.arrayedAndPrimitivesBuilderHelpers` |
| `CsgDsl` | `dsl` | covered | `DslOperationsTest.operationOverloadsAcrossCsgAndPrimitivePairs`; `DslJvmStaticCoverageTest.transformAndCsgWrapperStaticsAreInvocableFromJvm` |
| `ImportedKcsgScript` | `dsl`, `io` | covered | `ImportedScriptTest.importedKcsgScriptSurfacesExportsTargetsAndGet` |
| `ImportedScript` | `dsl`, `io` | covered | `ImportedScriptTest.importedKcsgScriptSurfacesExportsTargetsAndGet` |
| `KcsgBuilder` | `dsl`, `io`, `error-path` | covered | `KcsgBuilderTest.primitiveCsgImportStlAndExportFlow`; `KcsgBuilderTest.csgRemeshDefaultsToTrueAndCanBeDisabled` |
| `KcsgHost` | `dsl`, `io` | covered | `KcsgHostTest.emptyHostDefaultsAndErrorPaths`; `KcsgScriptJvmIoTest.stlVersionTokenChangesDependentCacheHash` |
| `EmptyHost` | `dsl`, `error-path` | covered | `KcsgHostTest.emptyHostDefaultsAndErrorPaths` |
| `KcsgScript` | `dsl`, `io`, `error-path` | covered | `KcsgScriptTest.overrideExportGenerateExportsTargetsAndCacheDelegation` |
| DSL boolean/set operator extensions (`and`, `or`, `xor`, `plus`, `minus`, `times`) | `dsl`, `boolean` | covered | `DslOperationsTest.operationOverloadsAcrossCsgAndPrimitivePairs` |
| `Cylinder.radius` (extension property) | `dsl`, `primitive`, `error-path` | covered | `CylinderRadiusPropertyTest.getterAndSetterOnEvenCylinder` |
| `TransformBuilder` | `dsl`, `math` | covered | `DslTransformExtensionsTest.transformBuilderAndTransformExtensionOverloads` |
| `Transform.translate` (DSL extension) | `dsl`, `math` | covered | `DslTransformExtensionsTest.transformBuilderAndTransformExtensionOverloads` |
| `Transform.scale(Double)` (DSL extension) | `dsl`, `math`, `error-path` | covered | `DslTransformExtensionsTest.transformBuilderAndTransformExtensionOverloads` |
| `Transform.scale(x,y,z)` (DSL extension) | `dsl`, `math`, `error-path` | covered | `DslTransformExtensionsTest.transformBuilderAndTransformExtensionOverloads` |
| `CSG.transform` (DSL extension) | `dsl`, `math`, `boolean` | covered | `DslTransformExtensionsTest.csgAndPrimitiveTransformExtensions` |
| `CSG.times(Transform)` (DSL extension) | `dsl`, `math` | covered | `DslTransformExtensionsTest.csgAndPrimitiveTransformExtensions` |
| `Primitive.times(Transform)` (DSL extension) | `dsl`, `primitive`, `math` | covered | `DslTransformExtensionsTest.csgAndPrimitiveTransformExtensions` |
| `Primitive.transform` (DSL extension) | `dsl`, `primitive`, `math` | covered | `DslTransformExtensionsTest.csgAndPrimitiveTransformExtensions` |
| `CSG.translate` (DSL extension) | `dsl`, `math` | covered | `DslTransformExtensionsTest.csgTranslateScaleAndRotExtensions` |
| `CSG.scale(Double)` (DSL extension) | `dsl`, `math`, `error-path` | covered | `DslTransformExtensionsTest.csgTranslateScaleAndRotExtensions` |
| `CSG.scale(x,y,z)` (DSL extension) | `dsl`, `math`, `error-path` | covered | `DslTransformExtensionsTest.csgTranslateScaleAndRotExtensions` |
| `CSG.rot` (DSL extension) | `dsl`, `math` | covered | `DslTransformExtensionsTest.csgTranslateScaleAndRotExtensions` |
