/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.helpers

internal fun passwordMessage(
    minLength: Int,
    requireUppercase: Boolean,
    requireLowercase: Boolean,
    requireDigit: Boolean,
    requireSpecialChar: Boolean
): String {
    val requirements = mutableListOf<String>()
    requirements.add("at least $minLength characters")
    if (requireUppercase) requirements.add("uppercase letter")
    if (requireLowercase) requirements.add("lowercase letter")
    if (requireDigit) requirements.add("digit")
    if (requireSpecialChar) requirements.add("special character")

    return "Password must contain ${requirements.joinToString(", ")}"
}
