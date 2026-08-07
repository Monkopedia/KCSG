plugins {
    alias(libs.plugins.kotlin.jvm)
}

val kcsgVersion: String = providers.gradleProperty("kcsgVersion").orNull
    ?: error("-PkcsgVersion=<version> is required; run scripts/consumer-smoke.sh")

dependencies {
    // ⚠️ `com.monkopedia:kcsg` MUST BE THE ONLY DECLARED DEPENDENCY HERE.
    //
    // In particular do not add kotlinx-io: whether kcsg puts kotlinx.io.files.Path on a
    // consumer's compile classpath is the assertion. See issue #51.
    implementation("com.monkopedia:kcsg:$kcsgVersion")
}
