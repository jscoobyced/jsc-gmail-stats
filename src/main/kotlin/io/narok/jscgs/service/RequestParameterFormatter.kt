package io.narok.jscgs.service

import java.text.SimpleDateFormat

class RequestParameterFormatter {
    companion object {
        private val fromDateFormat = SimpleDateFormat("yyyy-MM-dd")
        private val toDateFormat = SimpleDateFormat("yyyy/MM/dd")

        fun formatDate(from: String): String {
            try {
                return toDateFormat.format(fromDateFormat.parse(from))
            } catch (exc: Exception) {
                return "2200/01/01"
            }
        }
    }
}