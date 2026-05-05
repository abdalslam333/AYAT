package com.wird.ayat.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.material3.ColorProviders
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.layout.fillMaxWidth
import com.wird.ayat.MainActivity
import com.wird.ayat.R

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        WidgetUpdater.updateWidget(context)
    }
}

class WirdGlanceWidget : GlanceAppWidget() {

    companion object {
        val prefsArabicText = stringPreferencesKey("arabic_text")
        val prefsEnglishText = stringPreferencesKey("english_text")
        val prefsSourceText = stringPreferencesKey("source_text")
        val prefsExplanation = stringPreferencesKey("explanation")
        val prefsType = stringPreferencesKey("wird_type")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val appPrefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
                val themePref = appPrefs.getString("app_theme", "system") ?: "system"
                val customHex = appPrefs.getString("widget_color_hex", null)
                val customAlpha = appPrefs.getFloat("widget_alpha", 0.9f)
                val fontSizeInt = appPrefs.getInt("widget_font_size", 18)

                val sysBg = GlanceTheme.colors.background
                val sysOnBg = GlanceTheme.colors.onBackground
                val sysPrimary = GlanceTheme.colors.primary

                val baseBgColor = if (customHex != null) {
                    val c = android.graphics.Color.parseColor(customHex)
                    val alphaInt = (customAlpha * 255).toInt()
                    Color(android.graphics.Color.argb(alphaInt, android.graphics.Color.red(c), android.graphics.Color.green(c), android.graphics.Color.blue(c)))
                } else {
                    when (themePref) {
                        "dark" -> {
                            val alphaInt = (customAlpha * 255).toInt()
                            Color(android.graphics.Color.argb(alphaInt, 30, 30, 30))
                        }
                        "light" -> {
                            val alphaInt = (customAlpha * 255).toInt()
                            Color(android.graphics.Color.argb(alphaInt, 245, 245, 245))
                        }
                        else -> null
                    }
                }
                
                val bgColor = if (baseBgColor != null) ColorProvider(baseBgColor, baseBgColor) else sysBg

                val onBgColor = if (customHex != null) {
                    if (customHex.equals("#FFFFFF", ignoreCase = true) || customHex.equals("#F5F5F5", ignoreCase = true)) {
                        ColorProvider(Color.Black, Color.Black)
                    } else {
                        ColorProvider(Color.White, Color.White)
                    }
                } else {
                    when (themePref) {
                        "dark" -> ColorProvider(Color.White, Color.White)
                        "light" -> ColorProvider(Color.Black, Color.Black)
                        else -> sysOnBg
                    }
                }
                
                val primaryColor = when (themePref) {
                    "dark" -> ColorProvider(Color(0xFFBB86FC), Color(0xFFBB86FC))
                    "light" -> ColorProvider(Color(0xFF6200EE), Color(0xFF6200EE))
                    else -> sysPrimary
                }

                val prefs = currentState<Preferences>()
                val arabic = prefs[prefsArabicText] ?: "قبس"
                val english = prefs[prefsEnglishText] ?: ""
                val source = prefs[prefsSourceText] ?: ""

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgColor)
                        .padding(16.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    // Centered Text Content
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = arabic,
                            style = TextStyle(
                                color = onBgColor,
                                fontSize = fontSizeInt.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = GlanceModifier.padding(bottom = 8.dp)
                        )
                        
                        if (english.isNotEmpty()) {
                            Text(
                                text = english,
                                style = TextStyle(
                                    color = onBgColor,
                                    fontSize = (fontSizeInt - 2).sp,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = GlanceModifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        if (source.isNotEmpty()) {
                            Text(
                                text = source,
                                style = TextStyle(
                                    color = primaryColor,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                    
                    // Bottom Right Refresh Button
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_popup_sync),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<RefreshActionCallback>())
                        )
                    }
                }
            }
        }
    }
}
