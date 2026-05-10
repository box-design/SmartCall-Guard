package com.smartcall.guard.utils

object NumberNormalizer {
    fun normalize(number: String): String {
        var result = number.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        if (result.startsWith("+86")) {
            result = result.removePrefix("+86")
        }
        if (result.startsWith("86") && result.length > 11) {
            result = result.removePrefix("86")
        }

        return result
    }

    fun isMobileNumber(number: String): Boolean {
        val normalized = normalize(number)
        return normalized.length == 11 && normalized.startsWith("1")
    }

    fun isFixedLine(number: String): Boolean {
        val normalized = normalize(number)
        return normalized.length in 10..12 && normalized.startsWith("0")
    }
}
