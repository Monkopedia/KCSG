import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("java")
    alias(libs.plugins.kotlin.jvm)
}

application {
    mainClass.set("com.monkopedia.csgs.MainKt")
    applicationDefaultJvmArgs = listOf("-Xss515m")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()

    mavenLocal()
}

tasks.register("fatJar", type = Jar::class) {
    archiveBaseName = "${project.name}-all"
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
    testImplementation(group = "junit", name = "junit", version = "4.13.2")

    implementation(libs.clikt)
    implementation(libs.kotlinx.io.core)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":kcsg-dsl"))
    implementation(project(":kcsg"))
    implementation(kotlin("scripting-jsr223"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.compilerOptions {
    jvmTarget.set(JvmTarget.JVM_1_8)
}
