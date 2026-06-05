package com.kahavanu.sieve.engine

import com.kahavanu.domain.model.SuggestionKind

/**
 * Seed rules for Sri Lankan banks/services. Ordered by priority — first match wins.
 * Add new rules here; the classifier picks them up automatically.
 */
object SmsRules {

    /** A money amount parsed from an SMS body, together with the currency it was written in. */
    data class MoneyMatch(val amount: Double, val currency: String)

    // Currency tokens we recognize, either as a symbol ($ € £) or an ISO code (LKR/USD/EUR/GBP),
    // and either before the number ("LKR 1,500.00") or after it ("1,500.00 LKR" / "25.00 USD").
    private const val CURRENCY_TOKENS = """LKR|Rs\.?|USD|US\$|\$|EUR|€|GBP|£"""

    private val MONEY_RE = Regex(
        """(?:($CURRENCY_TOKENS)\s*([\d,]+(?:\.\d{1,2})?))""" +
            """|(?:([\d,]+(?:\.\d{1,2})?)\s*($CURRENCY_TOKENS))""",
        RegexOption.IGNORE_CASE,
    )

    // A loose "there is money mentioned" gate for body patterns (either token order).
    private val MONEY_GATE = """(?:$CURRENCY_TOKENS)\s*[\d,]+|[\d,]+\s*(?:$CURRENCY_TOKENS)"""

    // Matches "at MERCHANT NAME on" or "at MERCHANT NAME." or "POS: MERCHANT"
    private val MERCHANT_RE = Regex(
        """(?:at|to|POS[:\s]+)\s+([A-Za-z0-9][\w\s&'\-]{1,40})(?:\s+(?:on|via|Merchant|Ltd\.?|Pvt\.?)|\.|,|$)""",
        RegexOption.IGNORE_CASE,
    )

    private fun normalizeCurrency(token: String): String =
        when (token.trim().uppercase().removeSuffix(".")) {
            "RS", "LKR" -> "LKR"
            "USD", "US$", "$" -> "USD"
            "EUR", "€" -> "EUR"
            "GBP", "£" -> "GBP"
            else -> "LKR"
        }

    /**
     * Extracts the first money amount and its currency. Handles the currency token on either side
     * of the number; when only a bare "Rs"/number is present it falls back to LKR.
     */
    fun extractMoney(body: String): MoneyMatch? {
        val groups = MONEY_RE.find(body)?.groupValues ?: return null
        // Groups 1-2 = "<currency> <number>"; groups 3-4 = "<number> <currency>".
        val (numberText, token) = when {
            groups[2].isNotBlank() -> groups[2] to groups[1]
            groups[3].isNotBlank() -> groups[3] to groups[4]
            else -> return null
        }
        val amount = numberText.replace(",", "").toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
        return MoneyMatch(amount, normalizeCurrency(token))
    }

    /** Numeric value only. Kept for callers that don't care about the currency. */
    fun extractAmount(body: String): Double? = extractMoney(body)?.amount

    fun extractMerchant(body: String): String? =
        MERCHANT_RE.find(body)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

    val all: List<SmsRule> = listOf(

        // ── Commercial Bank ─────────────────────────────────────────────────────

        SmsRule(
            name = "ComBank-Credit",
            senderPattern = Regex("commercial|combank", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""credit(?:ed)?|deposit|received""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = money.currency,
                    title = "Bank Credit",
                    merchant = null,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.90f,
                )
            },
        ),

        SmsRule(
            name = "ComBank-Debit",
            senderPattern = Regex("commercial|combank", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""debit(?:ed)?|withdrawal|purchase|payment""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = if (merchant != null) "Payment at $merchant" else "Bank Debit",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.90f,
                )
            },
        ),

        // ── Sampath Bank ────────────────────────────────────────────────────────

        SmsRule(
            name = "Sampath-Credit",
            senderPattern = Regex("sampath", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""credit(?:ed)?|deposit|received""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = money.currency,
                    title = "Bank Credit",
                    merchant = null,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.88f,
                )
            },
        ),

        SmsRule(
            name = "Sampath-Debit",
            senderPattern = Regex("sampath", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""debit(?:ed)?|withdrawal|purchase|payment""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = if (merchant != null) "Payment at $merchant" else "Bank Debit",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.88f,
                )
            },
        ),

        // ── HNB ─────────────────────────────────────────────────────────────────

        SmsRule(
            name = "HNB-Credit",
            senderPattern = Regex("\\bhnb\\b|hatton", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""credit(?:ed)?|deposit|received""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = money.currency,
                    title = "Bank Credit",
                    merchant = null,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.88f,
                )
            },
        ),

        SmsRule(
            name = "HNB-Debit",
            senderPattern = Regex("\\bhnb\\b|hatton", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""debit(?:ed)?|withdrawal|purchase|payment""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = if (merchant != null) "Payment at $merchant" else "Bank Debit",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.88f,
                )
            },
        ),

        // ── BOC / NSB ────────────────────────────────────────────────────────────

        SmsRule(
            name = "BOC-Credit",
            senderPattern = Regex("\\bboc\\b|bank of ceylon|\\bnsb\\b|national savings", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""credit(?:ed)?|deposit|received""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = money.currency,
                    title = "Bank Credit",
                    merchant = null,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.87f,
                )
            },
        ),

        SmsRule(
            name = "BOC-Debit",
            senderPattern = Regex("\\bboc\\b|bank of ceylon|\\bnsb\\b|national savings", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""debit(?:ed)?|withdrawal|purchase|payment""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = if (merchant != null) "Payment at $merchant" else "Bank Debit",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.87f,
                )
            },
        ),

        // ── PickMe ───────────────────────────────────────────────────────────────

        SmsRule(
            name = "PickMe-Ride",
            senderPattern = Regex("pickme", RegexOption.IGNORE_CASE),
            bodyPattern = Regex(MONEY_GATE, RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = "PickMe Ride",
                    merchant = "PickMe",
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.85f,
                )
            },
        ),

        // ── Keells ───────────────────────────────────────────────────────────────

        SmsRule(
            name = "Keells-Purchase",
            senderPattern = Regex("keells", RegexOption.IGNORE_CASE),
            bodyPattern = Regex(MONEY_GATE, RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = "Keells Purchase",
                    merchant = "Keells",
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.85f,
                )
            },
        ),

        // ── Generic fallback — any authorized sender with a clear amount ──────────

        SmsRule(
            name = "Generic-Credit",
            senderPattern = Regex(".+"),
            bodyPattern = Regex("""(?:credit(?:ed)?|deposit|received).*(?:$MONEY_GATE)""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = money.currency,
                    title = "Income",
                    merchant = null,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.60f,
                )
            },
        ),

        SmsRule(
            name = "Generic-Debit",
            senderPattern = Regex(".+"),
            bodyPattern = Regex("""(?:debit(?:ed)?|paid|payment|purchase|charged).*(?:$MONEY_GATE)""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val money = extractMoney(body) ?: return@SmsRule null
                val amount = money.amount
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = money.currency,
                    title = if (merchant != null) "Payment at $merchant" else "Expense",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.60f,
                )
            },
        ),
    )
}
