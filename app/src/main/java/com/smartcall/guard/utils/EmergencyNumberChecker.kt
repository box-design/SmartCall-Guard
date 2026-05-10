package com.smartcall.guard.utils

object EmergencyNumberChecker {
    private val emergencyNumbers = setOf(
        "110", "120", "119", "122", "999",
        "10086", "10010", "10000"
    )

    fun isEmergency(number: String): Boolean {
        return emergencyNumbers.contains(number)
    }
}
