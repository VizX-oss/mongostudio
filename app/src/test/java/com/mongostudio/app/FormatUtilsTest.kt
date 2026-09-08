package com.mongostudio.app

import com.mongostudio.app.data.model.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun testFormatBytesZero() {
        assertEquals("0 B", FormatUtils.formatBytes(0))
        assertEquals("0 B", FormatUtils.formatBytes(-10))
    }

    @Test
    fun testFormatBytesUnits() {
        assertTrue(FormatUtils.formatBytes(1024).contains("KB"))
        assertTrue(FormatUtils.formatBytes(1024 * 1024).contains("MB"))
        assertTrue(FormatUtils.formatBytes(1024L * 1024L * 1024L).contains("GB"))
    }

    @Test
    fun testFormatUptime() {
        assertEquals("Unknown", FormatUtils.formatUptime(null))
        assertEquals("Unknown", FormatUtils.formatUptime(0))
        assertEquals("1m 15s", FormatUtils.formatUptime(75))
        assertEquals("2h 0m", FormatUtils.formatUptime(7200))
        assertEquals("1d 0h", FormatUtils.formatUptime(86400))
    }
}
