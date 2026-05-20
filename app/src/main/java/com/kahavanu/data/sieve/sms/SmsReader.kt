package com.kahavanu.data.sieve.sms

import android.content.ContentResolver
import android.net.Uri
import com.kahavanu.sieve.engine.RawSms
import javax.inject.Inject

class SmsReader @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    fun readSince(
        authorizedSenderNames: Set<String>,
        sinceEpochMillis: Long,
    ): List<RawSms> {
        val results = mutableListOf<RawSms>()

        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("address", "body", "date")
        val selection = "date > ?"
        val selectionArgs = arrayOf(sinceEpochMillis.toString())

        contentResolver.query(uri, projection, selection, selectionArgs, "date DESC")
            ?.use { cursor ->
                val addrIdx = cursor.getColumnIndexOrThrow("address")
                val bodyIdx = cursor.getColumnIndexOrThrow("body")
                val dateIdx = cursor.getColumnIndexOrThrow("date")

                while (cursor.moveToNext()) {
                    val address = cursor.getString(addrIdx) ?: continue
                    val body = cursor.getString(bodyIdx) ?: continue
                    val date = cursor.getLong(dateIdx)

                    val matchedSender = authorizedSenderNames.firstOrNull { senderName ->
                        address.contains(senderName, ignoreCase = true)
                    } ?: continue

                    results += RawSms(
                        senderName = matchedSender,
                        body = body,
                        receivedAtEpochMillis = date,
                    )
                }
            }

        return results
    }
}
