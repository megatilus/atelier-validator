/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures all available targets on a Kotlin Multiplatform project.
 */
@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.configureTargets() {
    // JVM
    jvm()

    // JavaScript
    js(IR) {
        nodejs()
    }

    // Apple Desktop
    macosX64()
    macosArm64()

    // Apple Mobile
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    linuxArm64()
    linuxX64()

    // Windows
    mingwX64()

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    wasmJs {
        nodejs()
    }

    wasmWasi {
        nodejs()
    }
}
