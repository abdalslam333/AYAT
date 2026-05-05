package com.wird.ayat.data

import android.content.Context
import android.util.Log
import android.util.JsonReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.random.Random

object DataManager {
    private val quranGroups = mutableListOf<WirdItem.QuranGroup>()
    private val hadiths = mutableListOf<WirdItem.Hadith>()
    
    var isInitialized = false
        private set

    @Synchronized
    fun initData(context: Context) {
        if (isInitialized) return
        
        try {
            loadHadiths(context)
            loadQuranWirds(context)
            isInitialized = true
        } catch (e: Exception) {
            Log.e("DataManager", "Error initializing data", e)
        }
    }
    
    private fun loadQuranWirds(context: Context) {
        try {
            val inputStream = context.assets.open("quran_wirds.json")
            val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
            
            quranGroups.clear()
            reader.beginArray()
            while (reader.hasNext()) {
                reader.beginObject()
                var arabicText = ""
                var englishText = ""
                var source = ""
                var explanation = ""
                
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "arabicText" -> arabicText = reader.nextString()
                        "englishText" -> englishText = reader.nextString()
                        "source" -> source = reader.nextString()
                        "explanation" -> explanation = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                quranGroups.add(WirdItem.QuranGroup(arabicText, englishText, source, explanation))
            }
            reader.endArray()
            reader.close()
        } catch (e: Exception) {
            Log.e("DataManager", "Error loading quran wirds", e)
        }
    }

    private fun loadHadiths(context: Context) {
        try {
            val inputStream = context.assets.open("nawawi_hadith.json")
            val reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
            
            data class HadithRaw(val title: String, val description: String, val hadithText: String)
            val rawItems = mutableListOf<HadithRaw>()
            
            reader.beginArray()
            while (reader.hasNext()) {
                reader.beginObject()
                var title = ""
                var description = ""
                var hadithText = ""
                
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "title" -> title = reader.nextString()
                        "description" -> description = reader.nextString()
                        "hadith" -> hadithText = reader.nextString()
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
                rawItems.add(HadithRaw(title, description, hadithText))
            }
            reader.endArray()
            reader.close()
            
            hadiths.clear()
            val halfSize = rawItems.size / 2
            for (i in 0 until halfSize) {
                val ar = rawItems[i]
                val en = rawItems[i + halfSize]
                if (ar.hadithText.isNotEmpty()) {
                    hadiths.add(WirdItem.Hadith(
                        text = ar.hadithText,
                        englishText = en.hadithText,
                        narrator = ar.title,
                        explanation = ar.description,
                        englishNarrator = en.title,
                        englishExplanation = en.description
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("DataManager", "Error loading hadiths", e)
        }
    }



    fun getRandomWird(context: Context): WirdItem? {
        val prefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
        val contentType = prefs.getString("content_type", "both") ?: "both"

        val hasQuran = quranGroups.isNotEmpty()
        val hasHadiths = hadiths.isNotEmpty()
        
        if (!hasQuran && !hasHadiths) return null

        return when (contentType) {
            "quran" -> if (hasQuran) quranGroups.random() else if (hasHadiths) hadiths.random() else null
            "hadith" -> if (hasHadiths) hadiths.random() else if (hasQuran) quranGroups.random() else null
            else -> {
                val isQuran = Random.nextFloat() < 0.85f
                if (isQuran && hasQuran) {
                    quranGroups.random()
                } else if (!isQuran && hasHadiths) {
                    hadiths.random()
                } else if (hasQuran) {
                    quranGroups.random()
                } else {
                    hadiths.random()
                }
            }
        }
    }
}
