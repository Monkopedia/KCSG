# KCSG

KCSG is a Kotlin Multiplatform constructive solid geometry library with a first-class Kotlin DSL. Originally forked from [JCSG](https://github.com/miho/JCSG), it has been substantially reimagined — the DSL is the primary interface, boolean operations compose naturally with Kotlin operators, and it runs on JVM, JS, WASM, Native, and every Apple platform. KCSG is the geometry engine behind [Konstructor](https://github.com/Monkopedia/Konstructor).

<p align="center">
  <img src="docs/images/kodee.png" alt="Kodee, the Kotlin mascot, modeled as a 3D relief in KCSG" width="360">
</p>

> Kotlin's mascot **Kodee**, modeled in KCSG (built in [Konstructor](https://github.com/Monkopedia/Konstructor), which uses KCSG) — a single-colour relief from 2D contours via `Extrude.points` + boolean depth. The runnable source is [`kodee.csgs`](kodee.csgs). "Kodee by JetBrains s.r.o." is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) and adapted here into a 3D relief.

## Example

```kotlin
val sensor by csg {
    val sensorTall = roundedCube {
        dimensions = xyz(sensorWidth, sensorLength, sensorHeight * 2)
        cornerRadius = corners
    }
    val sensorBounds = cube {
        dimensions = xyz(sensorWidth, sensorLength, sensorHeight)
    }
    sensorTall and sensorBounds
}

val base by csg {
    val block = cube {
        dimensions = xyz(baseWidth, baseLength, baseHeight)
    }
    block - positionedSensor - xCyl - yCyl
}
```

Primitives like `cube`, `sphere`, `cylinder`, and `roundedCube` are built with type-safe builders. Combine them with `and` (intersection), `-` (difference), and `or` (union), or use the operator equivalents `*`, `-`, and `+`. Transforms chain via `translate`, `rot`, and `scale`.

## Setup

KCSG is published on Maven Central as `com.monkopedia:kcsg` and `com.monkopedia:kcsg-dsl`.

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.monkopedia:kcsg:0.4.2")
    implementation("com.monkopedia:kcsg-dsl:0.4.2")
}
```

For Kotlin Multiplatform projects, add the dependencies in `commonMain` and Gradle will resolve the appropriate platform artifact.

## Command Line Tool

KCSG includes `csgs`, a standalone command line tool that runs `.csgs` script files and generates STL or OBJ output directly — no Kotlin project required. The fat jar is attached to each [GitHub release](https://github.com/Monkopedia/KCSG/releases).

```bash
java -jar csgs-all-0.4.2.jar sensor.csgs -e base -o out/
```

This makes KCSG usable as a scripting environment for 3D modeling. Write geometry in the DSL, run it from the command line, and get mesh output.

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).

## Acknowledgments

- [Michael Hoffer](https://github.com/miho) for creating [JCSG](https://github.com/miho/JCSG)
- [Evan Wallace](https://github.com/evanw) for [csg.js](https://github.com/evanw/csg.js), the original JavaScript CSG implementation
