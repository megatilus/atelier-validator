/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.helpers

internal fun isCommonSpecialChar(c: Char): Boolean {
    return c in "!@#$%^&*()_+-=[]{}|;:',.<>?/~`\""
}
