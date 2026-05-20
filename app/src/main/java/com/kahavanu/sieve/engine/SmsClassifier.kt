package com.kahavanu.sieve.engine

interface SmsClassifier {
    fun classify(raw: RawSms): ParsedSms?
}
