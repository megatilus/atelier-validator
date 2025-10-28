plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.multiplatform.plugin)
    implementation(libs.kotlinx.binary.compatibility.validator.plugin)
    implementation(libs.maven.publish)
}
