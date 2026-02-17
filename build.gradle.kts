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
