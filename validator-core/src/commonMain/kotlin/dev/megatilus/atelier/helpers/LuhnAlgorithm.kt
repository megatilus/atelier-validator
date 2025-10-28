/*
 * Copyright (c) 2025 Megatilus and contributors.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package dev.megatilus.atelier.helpers

internal fun isValidLuhn(cardNumber: String): Boolean {
    if (!cardNumber.all { it.isDigit() }) return false

    var sum = 0
    var alternate = false

    for (i in cardNumber.length - 1 downTo 0) {
        var digit = cardNumber[i].digitToInt()

        if (alternate) {
            digit *= 2
            if (digit > 9) digit -= 9
        }

        sum += digit
        alternate = !alternate
    }

    return sum % 10 == 0
}
