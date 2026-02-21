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

## Coverage Gate Scope

- `kcsg` has two coverage views:
- Full-module trend report: `./gradlew :kcsg:koverXmlReport` (includes vendored `com.monkopedia.kcsg.ext.*` packages).
- API gate: `./gradlew :kcsg:koverVerifyJvm` scoped to `com.monkopedia.kcsg` and excluding `com.monkopedia.kcsg.ext.*`.
- `kcsg-dsl` gate remains module-scoped via `./gradlew :kcsg-dsl:koverVerify`.

## Module: kcsg

| Symbol | Tags | Status | Tests |
| --- | --- | --- | --- |
| `Bounds` | `math`, `mesh` | covered | `BoundsTest.centerAndBoundsAreDerivedFromMinAndMax` |
| `CSG` | `boolean`, `mesh`, `io`, `error-path` | covered | `CSGBooleanTest.singleOverloadsProduceExpectedVolumes`; `PrimitiveInteractionMatrixTest.scenarioS9OptimizationParity`; `CSGRemeshTest.remeshTriangulatesPolygonsAndPreservesVolumeAndBounds` |
| `Cube` | `primitive`, `mesh` | covered | `CubeTest.constructorsInitializeCenterAndDimensions` |
| `Cylinder` | `primitive`, `mesh` | covered | `CylinderTest.constructorsPopulateGeometryFields` |
| `Edge` | `math`, `mesh`, `error-path` | covered | `EdgeApiTest.containsEqualsAndHashCode` |
| `Extrude` | `primitive`, `mesh`, `error-path` | covered | `ExtrudeTest.pointsOverloadsProduceEquivalentExtrusions` |
| `FileUtil` | `io`, `error-path` | covered | `FileUtilTest.writeAndReadRoundTrip` |
| `HashingOpOverride` | `io`, `dsl`, `error-path` | covered | `HashingOpOverrideTest.hashingSequenceIsDeterministicForSameInputs` |
| `MeshContainer` | `mesh`, `error-path` | covered | `MeshContainerTest.dimensionsBoundsAccessorsAndMeshViews` |
| `ObjFile` | `io`, `mesh`, `error-path` | covered | `ObjFileTest.objAndMtlAccessorsAndStreams` |
| `OpOverride` | `dsl`, `io` | covered | `OpOverrideContractTest.overrideDispatchesAcrossCsgStlBoundsAndPrimitiveEntrypoints` |
| `Plane` | `math`, `mesh`, `error-path` | covered | `PlaneTest.splitPolygonClassifiesAllCases` |
| `Polygon` | `math`, `mesh`, `error-path` | covered | `PolygonComprehensiveTest.constructorsAndIsValid` |
| `Polyhedron` | `primitive`, `mesh` | covered | `PolyhedronTest.listAndArrayConstructorsPopulatePointsAndFaces` |
| `Primitive` | `primitive`, `mesh` | covered | `PrimitiveDefaultBehaviorTest.toCsgUsesToPolygonsResult` |
| `PropertyStorage` | `mesh`, `dsl` | covered | `PropertyStorageTest.setAndTypedGetLookup` |
| `RoundedCube` | `primitive`, `mesh` | covered | `RoundedCubeTest.constructorsPopulateFields` |
| `STL` | `io`, `mesh`, `error-path` | covered | `STLTest.stlFileLoadsCsgFromDisk` |
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
| `KcsgHost` | `dsl`, `io` | covered | `KcsgHostTest.emptyHostDefaultsAndErrorPaths` |
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
