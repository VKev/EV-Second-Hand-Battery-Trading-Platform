package com.example.evsecondhand.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.graphics.Typeface
import com.example.evsecondhand.ui.screen.chatbot.ChatLink
import com.example.evsecondhand.ui.screen.chatbot.LinkType

object ChatbotFormatter {

    /**
     * Parse vehicle/battery links from chatbot response
     * Based on web service logic
     */
    fun parseVehicleLinks(answer: String): List<ChatLink> {
        val links = mutableListOf<ChatLink>()
        val processedIds = mutableSetOf<String>()

        // Find all URLs with their IDs
        val urlPattern = Regex("https?://[^\\s)]+/(vehicle|battery)/([\\w-]+)")
        val matches = urlPattern.findAll(answer)

        for (match in matches) {
            val type = match.groupValues[1] // "vehicle" or "battery"
            val id = match.groupValues[2]

            if (processedIds.contains(id)) continue

            // Get text before this URL (up to 300 chars)
            val textBefore = answer.substring(
                maxOf(0, match.range.first - 300),
                match.range.first
            )

            var title = if (type == "vehicle") "Xem chi tiết xe" else "Xem chi tiết pin"

            // Strategy 1: Direct colon format
            val colonMatch = Regex("""\\*\s+(?:\*\*)?([^*:\n]+?)(?:\*\*)?: [^:]*?$""").find(textBefore)
            if (colonMatch != null) {
                val candidate = colonMatch.groupValues[1].trim()
                if (isValidProductName(candidate)) {
                    title = candidate
                    processedIds.add(id)
                    links.add(ChatLink(id, title, if (type == "vehicle") LinkType.VEHICLE else LinkType.BATTERY))
                    continue
                }
            }

            // Strategy 2: Markdown link
            val mdLinkPattern = Regex("""\[([^\]]+)\]\([^)]+\)""")
            val mdLinkMatch = mdLinkPattern.find(
                answer.substring(
                    maxOf(0, match.range.first - 100),
                    minOf(answer.length, match.range.first + 100)
                )
            )
            if (mdLinkMatch != null) {
                val candidate = mdLinkMatch.groupValues[1].trim()
                if (isValidProductName(candidate)) {
                    title = candidate
                    processedIds.add(id)
                    links.add(ChatLink(id, title, if (type == "vehicle") LinkType.VEHICLE else LinkType.BATTERY))
                    continue
                }
            }

            // Strategy 3: Find ALL bold text in context
            val boldMatches = mutableListOf<String>()
            val boldPattern = Regex("""\*\*([^*]+?)\*\*""")
            boldPattern.findAll(textBefore).forEach { boldMatch ->
                boldMatches.add(boldMatch.groupValues[1].trim())
            }

            if (boldMatches.isNotEmpty()) {
                val validNames = boldMatches.filter { isValidProductName(it) }
                if (validNames.isNotEmpty()) {
                    // Pick the LAST valid name (closest to URL)
                    title = validNames.last()
                }
            }

            processedIds.add(id)
            links.add(ChatLink(id, title, if (type == "vehicle") LinkType.VEHICLE else LinkType.BATTERY))
        }

        return links
    }

    /**
     * Check if text looks like a valid product name
     */
    private fun isValidProductName(text: String): Boolean {
        if (text.isEmpty() || text.length < 3 || text.length > 60) return false

        // Must contain at least one letter
        if (!text.any { it.isLetter() }) return false

        // Exclude prices
        if (Regex("""^\d+[,.]?\d*\s*(VND|USD|đ|vnđ)$""", RegexOption.IGNORE_CASE).matches(text)) {
            return false
        }

        // Exclude if it's mostly numbers
        val numberCount = text.count { it.isDigit() }
        if (numberCount > text.length * 0.7) return false

        // Exclude common filler phrases
        val fillerPhrases = listOf(
            "với giá", "giá", "vnd", "tại đây", "xem chi tiết", "thông tin",
            "mời bạn", "ghé thăm", "bạn có thể", "để biết", "rất ấn tượng",
            "mẫu xe này", "chiếc xe", "sản phẩm"
        )

        val lowerText = text.lowercase()
        for (phrase in fillerPhrases) {
            if (lowerText == phrase || (lowerText.contains(phrase) && lowerText.length < 30)) {
                return false
            }
        }

        return true
    }

    /**
     * Format markdown-style text to styled text
     * Remove URLs as they will be shown as buttons
     */
    fun formatChatMessage(text: String): SpannableStringBuilder {
        // Remove all URLs from text
        var formattedText = text
            .replace(Regex("""https?://[^\s]+"""), "") // Remove all URLs
            .replace(Regex("""^\*\s+""", RegexOption.MULTILINE), "• ") // Convert markdown bullets
            .replace(Regex("""\n\n+"""), "\n\n") // Max double line breaks
            .trim()

        val builder = SpannableStringBuilder()
        var currentIndex = 0

        // Find and apply bold formatting
        val boldPattern = Regex("""\*\*(.+?)\*\*""")
        val matches = boldPattern.findAll(formattedText)

        var lastEnd = 0
        for (match in matches) {
            // Add text before match
            builder.append(formattedText.substring(lastEnd, match.range.first))

            // Add bold text
            val boldText = match.groupValues[1]
            val start = builder.length
            builder.append(boldText)
            builder.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                builder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            lastEnd = match.range.last + 1
        }

        // Add remaining text
        if (lastEnd < formattedText.length) {
            builder.append(formattedText.substring(lastEnd))
        }

        return builder
    }

    /**
     * Get clean text without markdown and URLs for display
     */
    fun getCleanText(text: String): String {
        return text
            .replace(Regex("""https?://[^\s]+"""), "")
            .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
            .replace(Regex("""^\*\s+""", RegexOption.MULTILINE), "• ")
            .replace(Regex("""\n\n+"""), "\n\n")
            .trim()
    }
}
