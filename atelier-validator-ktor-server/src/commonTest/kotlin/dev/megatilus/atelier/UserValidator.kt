/*
 * Copyright (c) 2026 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier

import dev.megatilus.atelier.validator.AtelierValidator
import dev.megatilus.atelier.validator.rules.email
import dev.megatilus.atelier.validator.rules.minLength
import dev.megatilus.atelier.validator.rules.notBlank
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val name: String, val email: String, val age: Int)

val userValidator = AtelierValidator<UserDto> {
    UserDto::name {
        notBlank()
        minLength(2)
    }
    UserDto::email {
        email()
    }
}
