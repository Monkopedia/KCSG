plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kcsg"

include(":kcsg")
include(":kcsg-dsl")
include(":csgs")
include(":samples")
