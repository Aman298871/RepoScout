package com.example.reposcout.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeFormatter {
    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val userFacingDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatIsoDate(isoDateString: String?): String {
        if (isoDateString.isNullOrBlank()) return "N/A"
        return try {
            val date: Date = isoParser.parse(isoDateString) ?: return isoDateString
            userFacingDateFormatter.format(date)
        } catch (_: Exception) {
            isoDateString.take(10) // fallback to YYYY-MM-DD
        }
    }

    fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
