@file:OptIn(ExperimentalBCVApi::class)

import kotlinx.validation.ExperimentalBCVApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    id("atelier-validator.base")
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

kotlin {
    explicitApi()
    jvmToolchain(21)

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    languageVersion.set(KotlinVersion.DEFAULT)
                }
            }
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            if (buildType == NativeBuildType.RELEASE) {
                freeCompilerArgs += "-Xbinary=smallBinary=true"
            }
        }
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

apiValidation {
    klib {
        enabled = true
    }
}
