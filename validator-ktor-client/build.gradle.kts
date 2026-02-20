import dev.megatilus.atelier.configureTargets

plugins {
    id("atelier-validator.kmp-conventions")
    id("atelier-validator.publishing")
    kotlin("plugin.serialization").version(libs.versions.kotlin)
}

kotlin {
    configureTargets(excludeTargets = setOf("wasmWasi"))

    sourceSets {
        commonMain.dependencies {
            api(project(":validator-core"))
            api(libs.ktor.client.core)
            implementation(libs.kotlinx.serialization)
        }

        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
