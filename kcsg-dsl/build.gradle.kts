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

    js(IR) {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    wasmWasi {
        nodejs()
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
                implementation(project(":kcsg"))
                implementation(libs.kotlinx.io.core)
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
    publishToMavenCentral()

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
                minBound(94)
            }
        }
    }
}
