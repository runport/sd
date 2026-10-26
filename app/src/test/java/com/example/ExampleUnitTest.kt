package com.example

import com.example.util.StorageManagerUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPersianDigitsConversion() {
        val input = "1234567890"
        val expected = "۱۲۳۴۵۶۷۸۹۰"
        assertEquals(expected, StorageManagerUtil.toPersianDigits(input))
    }

    @Test
    fun testFormatBytesZero() {
        val formatted = StorageManagerUtil.formatBytes(0L)
        assertEquals("۰ بایت", formatted)
    }

    @Test
    fun testFormatBytesMegabytes() {
        val bytes = 100L * 1024L * 1024L // 100 MB
        val formatted = StorageManagerUtil.formatBytes(bytes)
        assertTrue(formatted.contains("مگابایت"))
        assertTrue(formatted.contains("۱۰۰"))
    }

    @Test
    fun testFormatBytesGigabytes() {
        val bytes = 2L * 1024L * 1024L * 1024L // 2 GB
        val formatted = StorageManagerUtil.formatBytes(bytes)
        assertTrue(formatted.contains("گیگابایت"))
        assertTrue(formatted.contains("۲"))
    }
}
