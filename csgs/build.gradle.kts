import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.7"
    kotlin("jvm") version "1.7.20"
}

application {
    mainClass.set("com.monkopedia.csgs.MainKt")
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

tasks.register("fatJar", type = Jar::class) {
    baseName = "${project.name}-all"
    manifest {
        attributes["Implementation-Title"] = "CSGS Script Executor"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "com.monkopedia.csgs.MainKt"
    }
    from(configurations["runtimeClasspath"].map({ if (it.isDirectory) it else zipTree(it) }))
    with(tasks["jar"] as CopySpec)
    duplicatesStrategy = DuplicatesStrategy.WARN
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.0")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kcsg-dsl"))
    implementation(project(":kcsg"))
    implementation(kotlin("scripting-jsr223"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

