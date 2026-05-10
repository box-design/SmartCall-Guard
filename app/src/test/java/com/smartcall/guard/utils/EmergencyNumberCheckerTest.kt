package com.smartcall.guard.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyNumberCheckerTest {

    @Test
    fun isEmergency_returnsTrueFor110() {
        assertTrue(EmergencyNumberChecker.isEmergency("110"))
    }

    @Test
    fun isEmergency_returnsTrueFor120() {
        assertTrue(EmergencyNumberChecker.isEmergency("120"))
    }

    @Test
    fun isEmergency_returnsTrueFor119() {
        assertTrue(EmergencyNumberChecker.isEmergency("119"))
    }

    @Test
    fun isEmergency_returnsTrueFor122() {
        assertTrue(EmergencyNumberChecker.isEmergency("122"))
    }

    @Test
    fun isEmergency_returnsTrueFor999() {
        assertTrue(EmergencyNumberChecker.isEmergency("999"))
    }

    @Test
    fun isEmergency_returnsTrueFor10086() {
        assertTrue(EmergencyNumberChecker.isEmergency("10086"))
    }

    @Test
    fun isEmergency_returnsTrueFor10010() {
        assertTrue(EmergencyNumberChecker.isEmergency("10010"))
    }

    @Test
    fun isEmergency_returnsTrueFor10000() {
        assertTrue(EmergencyNumberChecker.isEmergency("10000"))
    }

    @Test
    fun isEmergency_returnsFalseForNormalNumber() {
        assertFalse(EmergencyNumberChecker.isEmergency("13800138000"))
    }

    @Test
    fun isEmergency_returnsFalseForUnknownEmergency() {
        assertFalse(EmergencyNumberChecker.isEmergency("114"))
    }

    @Test
    fun isEmergency_handlesSpacesInNumber() {
        assertTrue(EmergencyNumberChecker.isEmergency("1 1 0"))
    }
}
