import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.13"
    kotlin("jvm") version "1.8.0"
    `maven-publish`
    `signing`
}

group = "com.monkopedia"
description = "DSL wrappers and utilities for KCSG"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

javafx {
    modules = listOf("javafx.graphics", "javafx.fxml")
}

repositories {
    mavenCentral()
    jcenter()

    mavenLocal()
}

dependencies {
    implementation(project(":kcsg"))

    testImplementation(group = "junit", name = "junit", version = "4.+")

    implementation("org.slf4j:slf4j-simple:2.0.6")
    implementation(kotlin("stdlib-jdk8"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

publishing {
    publications {
        create<MavenPublication>("kcsg-dsl") {
            from(components["java"])
        }
    }
    publications.all {
        if (this !is MavenPublication) return@all

        afterEvaluate {
            pom {
                name.set("kcsg-dsl")
                description.set(project.description)
                url.set("http://www.github.com/Monkopedia/kcsg")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                    url.set("http://github.com/Monkopedia/kcsg/")
                }
            }
        }
    }
    repositories {
        maven(url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/") {
            name = "OSSRH"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["kcsg-dsl"])
    useGpgCmd()
}
