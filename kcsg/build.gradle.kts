import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    alias(libs.plugins.javafx)
    alias(libs.plugins.kotlin.jvm)
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
    testImplementation(group = "junit", name = "junit", version = "4.+")

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
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()
}

signing {
    sign(publishing.publications)
    useGpgCmd()
}
