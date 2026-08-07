plugins {
    alias(libs.plugins.kotlin.jvm)
}

val kcsgVersion: String = providers.gradleProperty("kcsgVersion").orNull
    ?: error("-PkcsgVersion=<version> is required; run scripts/consumer-smoke.sh")

dependencies {
    // ⚠️ `com.monkopedia:kcsg-dsl` MUST BE THE ONLY DECLARED DEPENDENCY HERE.
    //
    // Adding `com.monkopedia:kcsg` would put the kcsg types on this module's compile
    // classpath directly and the test would pass no matter how kcsg-dsl declares them --
    // which is exactly how :csgs and :samples masked issue #51 for the whole 0.5.0 cycle.
    // Whether kcsg reaches this compile classpath is the assertion.
    implementation("com.monkopedia:kcsg-dsl:$kcsgVersion")
}
