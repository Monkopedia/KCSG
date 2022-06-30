import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.7"
    kotlin("jvm") version "1.7.0"
}

application {
    mainClass.set("eu.mihosoft.vrl.v3d.Main")
    applicationDefaultJvmArgs = listOf("-Xss515m")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
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

    // compile group: "eu.mihosoft.ext.org.fxyz", name: "extfxyz", version: "0.4"
    // compile group: "eu.mihosoft.ext.org.fxyz", name: "extfxyz", version: "0.4", classifier: "sources"
    implementation(group = "eu.mihosoft.vvecmath", name = "vvecmath", version = "0.3.8")
    implementation(
        group = "eu.mihosoft.vvecmath",
        name = "vvecmath",
        version = "0.3.8",
        classifier = "sources"
    )
    implementation("org.slf4j:slf4j-simple:1.6.1")
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
