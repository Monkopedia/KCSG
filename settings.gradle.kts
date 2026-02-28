plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kcsg"

include(":kcsg")
include(":kcsg-dsl")
include(":csgs")
include(":samples")
