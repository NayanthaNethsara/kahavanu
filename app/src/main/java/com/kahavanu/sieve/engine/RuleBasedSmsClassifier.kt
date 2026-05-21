package com.kahavanu.sieve.engine

import javax.inject.Inject

class RuleBasedSmsClassifier @Inject constructor() : SmsClassifier {

    override fun classify(raw: RawSms): ParsedSms? {
        for (rule in SmsRules.all) {
            if (!rule.senderPattern.containsMatchIn(raw.senderName)) continue
            if (!rule.bodyPattern.containsMatchIn(raw.body)) continue
            val parsed = rule.parse(raw.senderName, raw.body, raw.receivedAtEpochMillis)
            if (parsed != null) return parsed
        }
        return null
    }
}
