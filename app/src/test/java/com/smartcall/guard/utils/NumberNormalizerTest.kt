package com.smartcall.guard.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberNormalizerTest {

    @Test
    fun normalize_removesSpaces() {
        assertEquals("13800138000", NumberNormalizer.normalize("138 0013 8000"))
    }

    @Test
    fun normalize_removesDashes() {
        assertEquals("13800138000", NumberNormalizer.normalize("138-0013-8000"))
    }

    @Test
    fun normalize_removesParentheses() {
        assertEquals("01012345678", NumberNormalizer.normalize("(010)12345678"))
    }

    @Test
    fun normalize_removesPlus86Prefix() {
        assertEquals("13800138000", NumberNormalizer.normalize("+8613800138000"))
    }

    @Test
    fun normalize_removes86Prefix() {
        assertEquals("13800138000", NumberNormalizer.normalize("8613800138000"))
    }

    @Test
    fun normalize_keepsShortNumbers() {
        assertEquals("110", NumberNormalizer.normalize("110"))
    }

    @Test
    fun normalize_handlesEmptyString() {
        assertEquals("", NumberNormalizer.normalize(""))
    }

    @Test
    fun isMobileNumber_returnsTrueForValidMobile() {
        assertTrue(NumberNormalizer.isMobileNumber("13800138000"))
    }

    @Test
    fun isMobileNumber_returnsFalseForShortNumber() {
        assertFalse(NumberNormalizer.isMobileNumber("110"))
    }

    @Test
    fun isMobileNumber_returnsFalseForFixedLine() {
        assertFalse(NumberNormalizer.isMobileNumber("01012345678"))
    }

    @Test
    fun isFixedLine_returnsTrueForFixedLine() {
        assertTrue(NumberNormalizer.isFixedLine("01012345678"))
    }

    @Test
    fun isFixedLine_returnsFalseForMobile() {
        assertFalse(NumberNormalizer.isFixedLine("13800138000"))
    }

    @Test
    fun normalize_handlesCombinedFormat() {
        assertEquals("13800138000", NumberNormalizer.normalize("+86 138-0013-8000"))
    }
}
