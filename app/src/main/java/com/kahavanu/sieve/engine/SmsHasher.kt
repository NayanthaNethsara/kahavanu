package com.kahavanu.sieve.engine

import java.security.MessageDigest

object SmsHasher {
    fun hash(senderName: String, body: String, receivedAtEpochMillis: Long): String {
        val input = "$senderName|$body|$receivedAtEpochMillis"
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
