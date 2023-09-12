import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("java")
    alias(libs.plugins.javafx)
    alias(libs.plugins.kotlin.jvm)
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
    implementation(project(":kcsg-dsl"))

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
