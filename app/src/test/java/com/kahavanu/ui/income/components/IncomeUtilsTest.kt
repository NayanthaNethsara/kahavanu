package com.kahavanu.ui.income.components

import com.kahavanu.ui.util.formatAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class IncomeUtilsTest {

    @Test
    fun `isPending returns true only for pending sourceType`() {
        assertTrue(isPending("pending"))
        assertFalse(isPending("recurrent"))
        assertFalse(isPending("one_time"))
        assertFalse(isPending(null))
        assertFalse(isPending(""))
    }

    @Test
    fun `isRecurrent returns true only for recurrent sourceType`() {
        assertTrue(isRecurrent("recurrent"))
        assertFalse(isRecurrent("pending"))
        assertFalse(isRecurrent("one_time"))
        assertFalse(isRecurrent(null))
    }

    @Test
    fun `isPersistent is true for pending and recurrent`() {
        assertTrue(isPersistent("pending"))
        assertTrue(isPersistent("recurrent"))
        assertFalse(isPersistent("one_time"))
        assertFalse(isPersistent(null))
        assertFalse(isPersistent("anything-else"))
    }

    @Test
    fun `formatAmount renders currency prefix and two-decimal amount`() {
        val previous = Locale.getDefault()
        Locale.setDefault(Locale.US)
        try {
            assertEquals("LKR 1,234.50", formatAmount(1234.5, "LKR"))
            assertEquals("USD 0.00", formatAmount(0.0, "USD"))
            assertEquals("EUR 1,000,000.00", formatAmount(1_000_000.0, "EUR"))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
