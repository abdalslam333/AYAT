package com.wird.ayat.widget

import android.content.Context
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.wird.ayat.data.DataManager
import com.wird.ayat.data.WirdItem

object WidgetUpdater {
    suspend fun updateWidget(context: Context) {
        DataManager.initData(context)
        val wird = DataManager.getRandomWird(context) ?: return
        
        val appPrefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
        val sysLang = if (java.util.Locale.getDefault().language.startsWith("ar")) "ar" else "en"
        val language = appPrefs.getString("app_language", sysLang) ?: sysLang
        
        var arabicText = when (wird) {
            is WirdItem.QuranGroup -> wird.arabicText
            is WirdItem.Hadith -> wird.text
        }
        var englishText = when (wird) {
            is WirdItem.QuranGroup -> wird.englishText
            is WirdItem.Hadith -> wird.englishText
        }
        
        if (language == "en" && englishText.isNotBlank()) {
            arabicText = ""
        } else if (language == "ar") {
            englishText = ""
        }
        
        val sourceText = when (wird) {
            is WirdItem.QuranGroup -> wird.source
            is WirdItem.Hadith -> if (language == "en" && wird.englishNarrator.isNotBlank()) wird.englishNarrator else wird.narrator
        }
        val explanationText = when (wird) {
            is WirdItem.QuranGroup -> wird.explanation
            is WirdItem.Hadith -> if (language == "en" && wird.englishExplanation.isNotBlank()) wird.englishExplanation else wird.explanation
        }
        val wirdType = when (wird) {
            is WirdItem.QuranGroup -> "quran"
            is WirdItem.Hadith -> "hadith"
        }
        appPrefs.edit()
            .putString("current_arabic", arabicText)
            .putString("current_english", englishText)
            .putString("current_source", sourceText)
            .putString("current_explanation", explanationText)
            .apply()
            
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(WirdGlanceWidget::class.java)
        
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[WirdGlanceWidget.prefsArabicText] = arabicText
                prefs[WirdGlanceWidget.prefsEnglishText] = englishText
                prefs[WirdGlanceWidget.prefsSourceText] = sourceText
                prefs[WirdGlanceWidget.prefsExplanation] = explanationText
                prefs[WirdGlanceWidget.prefsType] = wirdType
            }
            WirdGlanceWidget().update(context, glanceId)
        }
    }
}
