import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    alias(libs.plugins.javafx)
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    `signing`
}

group = "com.monkopedia"
description = "Kotlin port of the JCSG library"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
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
    testImplementation(group = "junit", name = "junit", version = "4.+")

    implementation(libs.slf4j.api)
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
        create<MavenPublication>("kcsg") {
            from(components["java"])
        }
    }
    publications.all {
        if (this !is MavenPublication) return@all

        afterEvaluate {
            pom {
                name.set("kcsg")
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
    sign(publishing.publications["kcsg"])
    useGpgCmd()
}
