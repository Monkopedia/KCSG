/*
 * A SEPARATE Gradle build. It is deliberately not included in the root settings.gradle.kts:
 * if it were, `:kcsg` and `:kcsg-dsl` would resolve as project dependencies and the whole
 * point -- resolving the *published* artifacts through their Gradle module metadata -- would
 * be lost. Driven by ../scripts/consumer-smoke.sh.
 */
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        // Share the root build's Kotlin version so the consumer never drifts from the
        // compiler that produced the artifacts.
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
    repositories {
        // exclusiveContent, not a plain maven {}: without it a missing or malformed local
        // publication would silently fall through to Maven Central and the smoke test would
        // compile against the LAST RELEASE instead of the tree under test -- a green (or a
        // red) that says nothing about this commit.
        exclusiveContent {
            forRepository {
                maven {
                    name = "kcsgConsumerSmoke"
                    url = java.io.File(
                        startParameter.projectProperties["kcsgRepo"]
                            ?: error("-PkcsgRepo=<dir> is required; run scripts/consumer-smoke.sh")
                    ).toURI()
                }
            }
            filter { includeGroup("com.monkopedia") }
        }
        mavenCentral()
    }
}

rootProject.name = "kcsg-consumer-smoke"

include(":dsl-only")
include(":core-only")
