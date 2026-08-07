import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.vannik.publish)
    signing
}

group = "com.monkopedia"
description = "DSL wrappers and utilities for KCSG"

repositories {
    mavenCentral()

    mavenLocal()
}

@OptIn(ExperimentalWasmDsl::class)
kotlin {
    jvmToolchain(8)

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        testRuns.named("test") {
            executionTask.configure {
                useJUnit()
            }
        }
    }

    // The geometry suites are far slower under Node than on the JVM: the S8
    // invariance scenario runs a full pair x opt-type boolean matrix and exceeds
    // Mocha's 2s default. Raise it rather than weaken the test.
    js(IR) {
        browser()
        nodejs {
            testTask { useMocha { timeout = "120s" } }
        }
    }
    wasmJs {
        browser()
        nodejs {
            testTask { useMocha { timeout = "120s" } }
        }
    }
    wasmWasi {
        nodejs {
            testTask { useMocha { timeout = "120s" } }
        }
    }

    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/java")
            dependencies {
                // `api`, not `implementation`: kcsg-owned types (CSG, Primitive, Transform,
                // Cube, Cylinder, RoundedCube, WeightFunction) and kotlinx.io.files.Path appear
                // in this module's public signatures, so they have to reach a consumer's
                // *compile* classpath. Under `implementation` Gradle omits them from the
                // published `*ApiElements` variant on jvm/js/wasm and consumers cannot name
                // them. See scripts/consumer-smoke.sh and issue #51.
                api(project(":kcsg"))
                api(libs.kotlinx.io.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            kotlin.srcDir("src/test/java")
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

tasks.register("test") {
    description = "Runs JVM tests for kcsg-dsl."
    group = "verification"
    dependsOn(tasks.named("jvmTest"))
}

// Set only by scripts/consumer-smoke.sh. In this mode the module publishes UNSIGNED to a
// throwaway file repository under <root>/build/consumer-smoke-repo so that a separate
// consumer build can resolve the real published metadata. Never set it for a real release:
// signing is what Maven Central rejects the upload without.
val consumerSmokePublish = providers.gradleProperty("kcsg.consumerSmoke").isPresent

mavenPublishing {
    pom {
        name.set("kcsg-dsl")
        description.set(project.description)
        url.set("https://www.github.com/Monkopedia/kcsg")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("monkopedia")
                name.set("Jason Monk")
                email.set("monkopedia@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/Monkopedia/kcsg.git")
            developerConnection.set("scm:git:ssh://github.com/Monkopedia/kcsg.git")
            url.set("https://github.com/Monkopedia/kcsg/")
        }
    }
    publishToMavenCentral(automaticRelease = true)

    if (!consumerSmokePublish) {
        signAllPublications()
    }
}

if (!consumerSmokePublish) {
    signing {
        sign(publishing.publications)
        useGpgCmd()
    }
} else {
    publishing {
        repositories {
            maven {
                name = "consumerSmoke"
                url = rootProject.layout.buildDirectory
                    .dir("consumer-smoke-repo").get().asFile.toURI()
            }
        }
    }
}

kover {
    reports {
        verify {
            rule("baseline-line-coverage") {
                minBound(94)
            }
        }
    }
}
