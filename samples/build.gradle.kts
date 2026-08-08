// `java` resolves to the Gradle java extension inside a build script, which
// shadows the java.* packages — these have to be imported, not fully qualified.
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")
    id("java")
    alias(libs.plugins.kotlin.jvm)
}

// This module is 46 standalone entry points (44 in the samples package, 2 in
// playground), not one application, so `run` takes the sample to launch rather
// than hard-coding one of them:
//
//   ./gradlew :samples:run -Pkcsg.sample=Spheres
//   ./gradlew :samples:run -Pkcsg.sample=com.monkopedia.kcsg.playground.Main
//
// A bare simple name resolves against com.monkopedia.kcsg.samples; a name
// containing a dot is used verbatim, which is how the playground entry points
// stay reachable. Available samples are the files under
// samples/src/main/java/com/monkopedia/kcsg/samples/.
val selectedSample: Provider<String> = providers.gradleProperty("kcsg.sample")
    .map { name -> if (name.contains('.')) name else "com.monkopedia.kcsg.samples.$name" }
    .orElse("com.monkopedia.kcsg.samples.RoundedCubeSample")

application {
    mainClass.set(selectedSample)
    applicationDefaultJvmArgs = listOf("-Xss515m")
}

// Samples write their STL/OBJ output through relative paths, so `run` would drop
// artefacts into samples/ — the module's own source directory. Give it a build
// directory to write into instead.
tasks.named<JavaExec>("run") {
    val outputDir = layout.buildDirectory.dir("sample-output")
    workingDir = outputDir.get().asFile
    doFirst { workingDir.mkdirs() }
}

// The `application` plugin resolves mainClass only when `run` executes. A
// mainClass naming a class that does not exist therefore builds green and fails
// only for whoever follows the docs — which is exactly how the pre-fork
// eu.mihosoft.vrl.v3d.Main survived :samples:build being added to CI in #38.
// Resolving the class at `check` time closes that gap; it does not run the
// sample, which would be slow and would write output files.
val verifySampleMainClass = tasks.register("verifySampleMainClass") {
    group = "verification"
    description = "Fails if the configured sample entry point does not exist or has no main method."

    val mainClassName = selectedSample
    val runtimeClasspath = files(sourceSets["main"].runtimeClasspath)
    inputs.property("mainClass", mainClassName)
    inputs.files(runtimeClasspath).withNormalizer(ClasspathNormalizer::class)

    doLast {
        val name = mainClassName.get()
        val urls = runtimeClasspath.files.map { it.toURI().toURL() }.toTypedArray()
        URLClassLoader(urls, ClassLoader.getPlatformClassLoader()).use { loader ->
            val cls = try {
                loader.loadClass(name)
            } catch (e: ClassNotFoundException) {
                throw GradleException(
                    "application.mainClass = '$name' does not exist on the :samples runtime " +
                        "classpath. Pass -Pkcsg.sample=<SampleName>; the available samples are " +
                        "the files under samples/src/main/java/com/monkopedia/kcsg/samples/."
                )
            }
            val hasMain = cls.methods.any { method ->
                method.name == "main" &&
                    Modifier.isStatic(method.modifiers) &&
                    method.parameterTypes.contentEquals(arrayOf(Array<String>::class.java))
            }
            if (!hasMain) {
                throw GradleException(
                    "'$name' exists but has no `static void main(String[])`, so " +
                        ":samples:run cannot launch it. Kotlin entry points need @JvmStatic."
                )
            }
            logger.lifecycle("Sample entry point resolves: $name")
        }
    }
}

tasks.named("check") { dependsOn(verifySampleMainClass) }

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()

    mavenLocal()
}

dependencies {
    implementation(project(":kcsg"))
    implementation(project(":kcsg-dsl"))

    testImplementation(group = "junit", name = "junit", version = "4.13.2")

    implementation(libs.kotlinx.io.core)
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
