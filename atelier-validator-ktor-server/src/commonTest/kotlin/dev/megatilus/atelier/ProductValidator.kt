/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.rules.min
import dev.megatilus.atelier.validator.rules.notBlank
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(val title: String, val price: Double)

val productValidator = AtelierValidator<ProductDto> {
    ProductDto::title { notBlank() }
    ProductDto::price { min(0.0) }
}
