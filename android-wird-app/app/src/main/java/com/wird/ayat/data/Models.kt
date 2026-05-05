package com.wird.ayat.data

sealed class WirdItem {
    data class QuranGroup(
        val arabicText: String,
        val englishText: String,
        val source: String,
        val explanation: String = ""
    ) : WirdItem()

    data class Hadith(
        val text: String,
        val englishText: String,
        val narrator: String,
        val explanation: String = "",
        val englishNarrator: String = "",
        val englishExplanation: String = ""
    ) : WirdItem()
}
