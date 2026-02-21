import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.testing.Test

plugins {
    id("java")
    alias(libs.plugins.javafx)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kover)
    alias(libs.plugins.vannik.publish)
    signing
}

group = "com.monkopedia"
description = "Kotlin port of the JCSG library"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

javafx {
    modules = listOf("javafx.graphics", "javafx.fxml")
}

repositories {
    mavenCentral()

    mavenLocal()
}

dependencies {
    testImplementation(group = "junit", name = "junit", version = "4.13.2")

    implementation(libs.slf4j.api)
    implementation(kotlin("stdlib-jdk8"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
}

tasks.named<Test>("test") {
    useJUnit()
    exclude("com/monkopedia/kcsg/samples/**")
    exclude("com/monkopedia/kcsg/oracle/**")
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

tasks.register<Test>("oracleTest") {
    description = "Runs oracle agreement tests against generated OpenSCAD fixtures."
    group = "verification"
    useJUnit()
    val oracleQuick = providers.gradleProperty("kcsg.oracle.quick").getOrElse("false")
    systemProperty("kcsg.oracle.quick", oracleQuick)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("com/monkopedia/kcsg/oracle/**")
    dependsOn(oracleGenerateFixtures)
    shouldRunAfter(tasks.named("test"))
}

tasks.register<Test>("sampleTest") {
    description = "Runs sample model integration tests."
    group = "verification"
    useJUnit()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("com/monkopedia/kcsg/samples/**")
    shouldRunAfter(tasks.named("test"))
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
    publishToMavenCentral()

    signAllPublications()
}

signing {
    sign(publishing.publications)
    useGpgCmd()
}

kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.add("sampleTest")
        }
    }
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
