import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.vannik.publish)
    signing
}

group = "com.monkopedia"
description = "Kotlin port of the JCSG library"

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
                exclude("com/monkopedia/kcsg/oracle/**")
            }
        }
    }

    // The geometry suites are far slower under Node than on the JVM: the S8
    // invariance scenario runs a full pair x opt-type boolean matrix and exceeds
    // Mocha's 2s default. Raise it rather than weaken the test.
    js(IR) {
        browser {
            testTask { useMocha { timeout = "120s" } }
        }
        nodejs {
            testTask { useMocha { timeout = "120s" } }
        }
    }
    wasmJs {
        browser {
            testTask { useMocha { timeout = "120s" } }
        }
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
                implementation(libs.kotlinx.io.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
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

val oracleGenerateFixtures by tasks.registering(Exec::class) {
    description = "Generates OpenSCAD oracle fixtures used by oracle agreement tests."
    group = "verification"
    workingDir = rootProject.projectDir
    val outputDir = layout.buildDirectory.dir("oracle-fixtures")
    commandLine(
        "bash",
        "${rootProject.projectDir}/scripts/oracle/generate_openscad_oracles.sh",
        "--output-dir",
        outputDir.get().asFile.absolutePath
    )
}

val jvmTarget = kotlin.targets.getByName("jvm") as KotlinJvmTarget

val jvmOracleTest = jvmTarget.testRuns.create("oracle") {
    executionTask.configure {
        useJUnit()
        val oracleQuick = providers.gradleProperty("kcsg.oracle.quick").getOrElse("false")
        systemProperty("kcsg.oracle.quick", oracleQuick)
        include("com/monkopedia/kcsg/oracle/**")
        dependsOn(oracleGenerateFixtures)
        shouldRunAfter(tasks.named("jvmTest"))
    }
}.executionTask

tasks.register("oracleTest") {
    description = "Runs oracle agreement tests against generated OpenSCAD fixtures."
    group = "verification"
    dependsOn(jvmOracleTest)
}

tasks.register("test") {
    description = "Runs JVM unit/regression tests (excluding oracle suite)."
    group = "verification"
    dependsOn(tasks.named("jvmTest"))
}

mavenPublishing {
    pom {
        name.set("kcsg")
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

    signAllPublications()
}

signing {
    sign(publishing.publications)
    useGpgCmd()
}

kover {
    reports {
        verify {
            rule("baseline-line-coverage") {
                minBound(81)
            }
        }
        variant("jvm") {
            filters {
                includes {
                    packages("com.monkopedia.kcsg")
                }
                excludes {
                    packages("com.monkopedia.kcsg.ext")
                }
            }
            verify {
                rule("api-package-line-coverage") {
                    minBound(95)
                }
            }
        }
    }
}
