import kotlinx.validation.ExperimentalBCVApi

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.bcv)
}

// Binary-compatibility-validator: mechanical public-ABI gate for the library modules.
// Only :kcsg and :kcsg-dsl are published libraries; :csgs (CLI app) and :samples are not.
apiValidation {
    ignoredProjects.addAll(listOf("csgs", "samples"))
    // Validate the non-JVM targets (JS/Wasm/native/Apple) too, not just the JVM .api dump.
    @OptIn(ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
}

tasks.register("coverageHtml") {
    group = "verification"
    description = "Generates HTML coverage reports for kcsg and kcsg-dsl."
    dependsOn(":kcsg:koverHtmlReport", ":kcsg-dsl:koverHtmlReport")

    doLast {
        println("Coverage reports:")
        println(" - kcsg: ${project.rootDir}/kcsg/build/reports/kover/html/index.html")
        println(" - kcsg-dsl: ${project.rootDir}/kcsg-dsl/build/reports/kover/html/index.html")
    }
}

tasks.register("oracleFixtures") {
    group = "verification"
    description = "Downloads OpenSCAD and generates oracle fixtures for kcsg."
    dependsOn(":kcsg:oracleGenerateFixtures")
}

tasks.register("oracleTest") {
    group = "verification"
    description = "Runs oracle agreement tests against OpenSCAD-generated fixtures."
    dependsOn(":kcsg:oracleTest")
}
