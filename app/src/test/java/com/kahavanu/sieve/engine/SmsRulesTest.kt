package com.kahavanu.sieve.engine

import com.kahavanu.domain.model.SuggestionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SmsRulesTest {

    @Test
    fun `extractMoney reads LKR amounts written before the number`() {
        val m = SmsRules.extractMoney("Your account credited with LKR 1,500.00 today.")!!
        assertEquals(1500.0, m.amount, 0.0)
        assertEquals("LKR", m.currency)
    }

    @Test
    fun `extractMoney handles Rs with and without a dot or space`() {
        assertEquals(1500.0, SmsRules.extractMoney("debited Rs.1500")!!.amount, 0.0)
        assertEquals(1200.5, SmsRules.extractMoney("paid Rs 1,200.50")!!.amount, 0.0)
    }

    @Test
    fun `extractMoney detects USD by symbol and code, either side of the number`() {
        assertEquals("USD", SmsRules.extractMoney("received \$540.00 via remittance")!!.currency)
        assertEquals("USD", SmsRules.extractMoney("purchase of 25.00 USD at Amazon")!!.currency)
    }

    @Test
    fun `extractMoney detects EUR and GBP symbols`() {
        assertEquals("EUR", SmsRules.extractMoney("credited €300 to your account")!!.currency)
        assertEquals("GBP", SmsRules.extractMoney("charged £49.99")!!.currency)
    }

    @Test
    fun `extractMoney ignores bare numbers with no currency token`() {
        assertNull(SmsRules.extractMoney("Your OTP is 482913, valid for 5 minutes."))
    }

    @Test
    fun `extractMoney takes the transaction amount, not the trailing balance`() {
        val m = SmsRules.extractMoney("Debited LKR 2,000 at Keells. Avbl bal LKR 120,000.")!!
        assertEquals(2000.0, m.amount, 0.0)
    }

    private fun classify() = RuleBasedSmsClassifier()

    @Test
    fun `commercial bank credit is parsed as LKR income`() {
        val parsed = classify().classify(
            RawSms("Commercial Bank", "Your account credited with LKR 50,000.00.", 1_000L)
        )
        assertNotNull(parsed)
        assertEquals(SuggestionKind.INCOME, parsed!!.kind)
        assertEquals(50000.0, parsed.amount, 0.0)
        assertEquals("LKR", parsed.currency)
    }

    @Test
    fun `foreign currency remittance keeps its detected currency`() {
        val parsed = classify().classify(
            RawSms("Commercial Bank", "Your account has been credited with USD 540.00.", 1_000L)
        )
        assertNotNull(parsed)
        assertEquals(SuggestionKind.INCOME, parsed!!.kind)
        assertEquals("USD", parsed.currency)
    }

    @Test
    fun `debit at a merchant is parsed as an expense with the merchant name`() {
        val parsed = classify().classify(
            RawSms("HNB", "Your card was debited LKR 3,250.00 at KEELLS SUPER on 12/05.", 1_000L)
        )
        assertNotNull(parsed)
        assertEquals(SuggestionKind.EXPENSE, parsed!!.kind)
        assertEquals(3250.0, parsed.amount, 0.0)
        assertEquals("KEELLS SUPER", parsed.merchant)
    }

    @Test
    fun `unknown sender with a clear debit falls back to the generic rule`() {
        val parsed = classify().classify(
            RawSms("SomeBank", "Payment of \$25.00 charged to your card.", 1_000L)
        )
        assertNotNull(parsed)
        assertEquals(SuggestionKind.EXPENSE, parsed!!.kind)
        assertEquals("USD", parsed.currency)
    }
}
