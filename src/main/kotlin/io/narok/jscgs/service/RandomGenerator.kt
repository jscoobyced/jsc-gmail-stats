package io.narok.jscgs.service

class RandomGenerator {
    companion object {
        fun getRandomString(length: Int): String {
            val startAllowedChar = ('A'..'Z') + ('a'..'z')
            if (length <= 1) return (1..1)
                .map { startAllowedChar.random() }
                .joinToString("")
            val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
            return (1..1)
                .map { startAllowedChar.random() }
                .joinToString("") + (1..length - 1)
                .map { allowedChars.random() }
                .joinToString("")
        }
    }
}