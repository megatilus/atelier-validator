import dev.megatilus.atelier.configureTargets

plugins {
    id("atelier-validator.kmp-conventions")
    id("atelier-validator.publishing")
}

kotlin {
    configureTargets()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":atelier-validator-core"))
                api(libs.kotlinx.datetime)
            }
        }
    }
}
