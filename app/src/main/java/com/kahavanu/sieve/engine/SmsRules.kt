package com.kahavanu.sieve.engine

import com.kahavanu.domain.model.SuggestionKind

/**
 * Seed rules for Sri Lankan banks/services. Ordered by priority — first match wins.
 * Add new rules here; the classifier picks them up automatically.
 */
object SmsRules {

    // Matches LKR 1,500.00 / Rs. 1,500 / Rs 1500.50 / LKR1,200
    private val AMOUNT_RE = Regex(
        """(?:LKR|Rs\.?)\s*([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE,
    )

    // Matches "at MERCHANT NAME on" or "at MERCHANT NAME." or "POS: MERCHANT"
    private val MERCHANT_RE = Regex(
        """(?:at|to|POS[:\s]+)\s+([A-Za-z0-9][\w\s&'-]{1,40?})(?:\s+(?:on|via|Merchant|Ltd\.?|Pvt\.?)|\.|,|$)""",
        RegexOption.IGNORE_CASE,
    )

    fun extractAmount(body: String): Double? =
        AMOUNT_RE.find(body)?.groupValues?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()

    fun extractMerchant(body: String): String? =
        MERCHANT_RE.find(body)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }

    val all: List<SmsRule> = listOf(

        // ── Commercial Bank ─────────────────────────────────────────────────────

        SmsRule(
            name = "ComBank-Credit",
            senderPattern = Regex("commercial|combank", RegexOption.IGNORE_CASE),
            bodyPattern = Regex("""credit(?:ed)?|deposit|received""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = "LKR",
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
                val amount = extractAmount(body) ?: return@SmsRule null
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
            bodyPattern = Regex("""(?:Rs\.?|LKR)\s*[\d,]+""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
            bodyPattern = Regex("""(?:Rs\.?|LKR)\s*[\d,]+""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
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
            bodyPattern = Regex("""(?:credit(?:ed)?|deposit|received).*(?:LKR|Rs\.?)\s*[\d,]+""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val amount = extractAmount(body) ?: return@SmsRule null
                ParsedSms(
                    kind = SuggestionKind.INCOME,
                    amount = amount,
                    currency = "LKR",
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
            bodyPattern = Regex("""(?:debit(?:ed)?|paid|payment|purchase|charged).*(?:LKR|Rs\.?)\s*[\d,]+""", RegexOption.IGNORE_CASE),
            parse = { _, body, receivedAt ->
                val amount = extractAmount(body) ?: return@SmsRule null
                val merchant = extractMerchant(body)
                ParsedSms(
                    kind = SuggestionKind.EXPENSE,
                    amount = amount,
                    currency = "LKR",
                    title = if (merchant != null) "Payment at $merchant" else "Expense",
                    merchant = merchant,
                    txnAtEpochMillis = receivedAt,
                    confidence = 0.60f,
                )
            },
        ),
    )
}
