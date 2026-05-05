package com.wird.ayat

import android.os.Bundle
import android.os.Build
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import com.wird.ayat.ui.theme.DailyWirdTheme
import com.wird.ayat.widget.WirdAppWidgetReceiver
import com.wird.ayat.widget.WidgetUpdater
import android.content.Context
import dev.jeziellago.compose.markdowntext.MarkdownText
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.wird.ayat.widget.WidgetUpdateWorker

val DeepDarkBlue = Color(0xFF0B1521)
val CardDarkBlue = Color(0xFF111A2C)
val ButtonDarkBlue = Color(0xFF1A233A)
val SoftGold = Color(0xFFD4AF37)
val TextGrey = Color(0xFFA0AAB2)

fun getLangString(lang: String, ar: String, en: String): String {
    return if (lang == "ar") ar else en
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Schedule periodic updates
        val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            updateRequest
        )
        
        setContent {
            DailyWirdTheme(darkTheme = true) { // Force dark theme for the sleek look
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepDarkBlue
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.islamic_pattern),
                            contentDescription = "Background Pattern",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().alpha(0.05f)
                        )
                        AppNavigation()
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "explanation") {
        composable("explanation") {
            ExplanationScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate("help") }
            )
        }
        composable("help") {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplanationScreen(onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val appPrefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
    
    val sysLang = if (java.util.Locale.getDefault().language.startsWith("ar")) "ar" else "en"
    var currentLang by remember { mutableStateOf(appPrefs.getString("app_language", sysLang) ?: sysLang) }
    
    var currentArabic by remember { mutableStateOf(appPrefs.getString("current_arabic", "") ?: "") }
    var currentEnglish by remember { mutableStateOf(appPrefs.getString("current_english", "") ?: "") }
    var currentSource by remember { mutableStateOf(appPrefs.getString("current_source", "") ?: "") }
    var currentExplanation by remember { mutableStateOf(appPrefs.getString("current_explanation", "") ?: "") }
    
    DisposableEffect(appPrefs) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            when (key) {
                "current_arabic" -> currentArabic = prefs.getString("current_arabic", "") ?: ""
                "current_english" -> currentEnglish = prefs.getString("current_english", "") ?: ""
                "current_source" -> currentSource = prefs.getString("current_source", "") ?: ""
                "current_explanation" -> currentExplanation = prefs.getString("current_explanation", "") ?: ""
                "app_language" -> currentLang = prefs.getString("app_language", sysLang) ?: sysLang
            }
        }
        appPrefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            appPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                if (currentArabic.isNotEmpty() || currentEnglish.isNotEmpty()) {
                    if (currentArabic.isNotEmpty()) {
                        Text(
                            text = currentArabic,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 48.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    if (currentEnglish.isNotEmpty()) {
                        Text(
                            text = "\"$currentEnglish\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SoftGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    if (currentSource.isNotEmpty()) {
                        Text(
                            text = currentSource.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextGrey,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                    
                    if (currentExplanation.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDarkBlue)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = getLangString(currentLang, "التفسير", "EXPLANATION"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextGrey,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                MarkdownText(
                                    markdown = currentExplanation,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    textAlign = if (currentLang == "ar") TextAlign.Right else TextAlign.Left
                                )
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val copiedText = mutableListOf<String>()
                            if (currentArabic.isNotEmpty()) copiedText.add(currentArabic)
                            if (currentEnglish.isNotEmpty()) copiedText.add(currentEnglish)
                            if (currentSource.isNotEmpty()) copiedText.add(currentSource)
                            copiedText.add("\nShared via Qabas App")
                            val clip = ClipData.newPlainText("Verse", copiedText.joinToString("\n\n"))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, getLangString(currentLang, "تم النسخ", "Copied to clipboard"), Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonDarkBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(getLangString(currentLang, "نسخ الورد", "Copy Verse"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(getLangString(currentLang, "لا يوجد محتوى محدد بعد. يرجى تفعيل الويدجت.", "No content selected yet. Please check the widget or update content from settings."), textAlign = TextAlign.Center, color = TextGrey)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val context = LocalContext.current
    val appPrefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()
    
    val sysLang = if (java.util.Locale.getDefault().language.startsWith("ar")) "ar" else "en"
    var currentLang by remember { mutableStateOf(appPrefs.getString("app_language", sysLang) ?: sysLang) }
    
    var transparency by remember { mutableStateOf(appPrefs.getFloat("widget_alpha", 1f)) }
    var selectedColorHex by remember { mutableStateOf(appPrefs.getString("widget_color_hex", "#111A2C") ?: "#111A2C") }
    var fontSize by remember { mutableStateOf(appPrefs.getInt("widget_font_size", 18).toFloat()) }
    
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    val presetColors = listOf(
        "#FFFFFF", // Soft White
        "#1A233A", // Lighter Blue
        "#0B1521", // Deep Dark Blue
        "#111A2C", // Card Blue
        "#000000"  // Charcoal Black
    )
    
    fun saveAndRefreshWidget() {
        appPrefs.edit()
            .putFloat("widget_alpha", transparency)
            .putString("widget_color_hex", selectedColorHex)
            .putInt("widget_font_size", fontSize.toInt())
            .apply()
        coroutineScope.launch {
            val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(com.wird.ayat.widget.WirdGlanceWidget::class.java)
            val widget = com.wird.ayat.widget.WirdGlanceWidget()
            for (id in glanceIds) {
                widget.update(context, id)
            }
        }
    }
    
    fun updateLanguage(lang: String) {
        currentLang = lang
        appPrefs.edit().putString("app_language", lang).apply()
        coroutineScope.launch {
            WidgetUpdater.updateWidget(context) // Immediately update content to new lang
        }
        showLanguageDialog = false
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(getLangString(currentLang, "اختر اللغة", "Choose Language")) },
            text = {
                Column {
                    TextButton(onClick = { updateLanguage("ar") }) { Text("العربية", color = Color.White) }
                    TextButton(onClick = { updateLanguage("en") }) { Text("English", color = Color.White) }
                }
            },
            confirmButton = {},
            containerColor = CardDarkBlue,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(getLangString(currentLang, "إبلاغ عن مشكلة", "Report Issue")) },
            text = { 
                Text(getLangString(currentLang, 
                    "يمكنك مشاركة الخطأ الظاهر في التطبيق عبر الإيميل. يرجى إرفاق صورة للخطأ إن أمكن.", 
                    "You can share the error shown in the app via email. Please attach a screenshot of the error if possible.")) 
            },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:alwagea22@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Qabas - Report Issue")
                    }
                    context.startActivity(Intent.createChooser(intent, getLangString(currentLang, "إرسال عبر", "Send via")))
                }) {
                    Text(getLangString(currentLang, "إرسال", "Send"), color = SoftGold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text(getLangString(currentLang, "إلغاء", "Cancel"), color = TextGrey)
                }
            },
            containerColor = CardDarkBlue,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(getLangString(currentLang, "الإعدادات", "Settings"), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkBlue)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = getLangString(currentLang, "لون الخلفية", "BACKGROUND COLOR"),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGrey,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            presetColors.forEach { hex ->
                                val isSelected = selectedColorHex == hex
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .clickable { 
                                            selectedColorHex = hex
                                            saveAndRefreshWidget()
                                        }
                                        .padding(2.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color.Transparent)
                                                .padding(2.dp)
                                        ) {
                                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                                drawCircle(
                                                    color = SoftGold,
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLangString(currentLang, "الشفافية", "TRANSPARENCY"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrey,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${(transparency * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Slider(
                            value = transparency,
                            onValueChange = { transparency = it },
                            onValueChangeFinished = { saveAndRefreshWidget() },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = ButtonDarkBlue
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(getLangString(currentLang, "شفاف", "CLEAR"), style = MaterialTheme.typography.labelSmall, color = TextGrey)
                            Text(getLangString(currentLang, "صلب", "SOLID"), style = MaterialTheme.typography.labelSmall, color = TextGrey)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLangString(currentLang, "حجم الخط", "FONT SIZE"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrey,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${fontSize.toInt()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            onValueChangeFinished = { saveAndRefreshWidget() },
                            valueRange = 16f..22f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = ButtonDarkBlue
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("16", style = MaterialTheme.typography.labelSmall, color = TextGrey)
                            Text("22", style = MaterialTheme.typography.labelSmall, color = TextGrey)
                        }
                    }
                }
            }
            
            val listItems = listOf(
                Triple(Icons.Outlined.Add, getLangString(currentLang, "إضافة ويدجت", "Add Widget"), {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                            val provider = ComponentName(context, WirdAppWidgetReceiver::class.java)
                            appWidgetManager.requestPinAppWidget(provider, null, null)
                        } else {
                            Toast.makeText(context, getLangString(currentLang, "غير مدعوم", "Not supported"), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, getLangString(currentLang, "اضغط مطولاً على الشاشة الرئيسية لإضافة الويدجت", "Long press home screen to add widget"), Toast.LENGTH_SHORT).show()
                    }
                }),
                Triple(Icons.Outlined.Refresh, getLangString(currentLang, "تحديث المحتوى", "Change Content"), {
                    Toast.makeText(context, getLangString(currentLang, "جاري التحديث...", "Updating..."), Toast.LENGTH_SHORT).show()
                    coroutineScope.launch {
                        WidgetUpdater.updateWidget(context)
                        Toast.makeText(context, getLangString(currentLang, "تم التحديث!", "Updated successfully!"), Toast.LENGTH_SHORT).show()
                    }
                }),
                Triple(Icons.Outlined.Language, getLangString(currentLang, "اللغة", "Language"), { showLanguageDialog = true }),
                Triple(Icons.Outlined.Share, getLangString(currentLang, "مشاركة التطبيق", "Share App"), {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out ${context.getString(R.string.app_name)} app!")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                }),
                Triple(Icons.Outlined.AccountCircle, getLangString(currentLang, "تابعني", "Follow Me"), {
                    val uri = Uri.parse("https://www.instagram.com/alwagea.a.w/")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.instagram.android") }
                    try { context.startActivity(intent) } catch (e: ActivityNotFoundException) { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }),
                Triple(Icons.Outlined.Flag, getLangString(currentLang, "إبلاغ عن مشكلة", "Report Issue"), { showReportDialog = true }),
                Triple(Icons.Outlined.Info, getLangString(currentLang, "طريقة الاستخدام", "How to use"), { onNavigateToHelp() })
            )
            
            items(listItems) { (icon, title, onClick) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = title, tint = TextGrey, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    if (title == "Language" || title == "اللغة") {
                        Text(if (currentLang == "ar") "العربية" else "English", color = TextGrey, fontSize = 14.sp)
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val appPrefs = context.getSharedPreferences("wird_settings", Context.MODE_PRIVATE)
    val sysLang = if (java.util.Locale.getDefault().language.startsWith("ar")) "ar" else "en"
    val currentLang = appPrefs.getString("app_language", sysLang) ?: sysLang

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(getLangString(currentLang, "طريقة الاستخدام", "How to use"), color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDarkBlue)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = getLangString(currentLang,
                                "1. قم بالضغط مطولاً على مساحة فارغة في الشاشة الرئيسية لجهازك.\n\n" +
                                "2. اختر 'التطبيقات المصغرة' أو 'Widgets' من القائمة التي تظهر.\n\n" +
                                "3. ابحث عن تطبيق 'قبس'.\n\n" +
                                "4. اسحب الويدجت (التطبيق المصغر) وقم بوضعه في الشاشة الرئيسية.\n\n" +
                                "5. يمكنك النقر على أيقونة التحديث داخل الويدجت لتغيير الآية أو الحديث في أي وقت.\n\n" +
                                "6. اضغط على الويدجت لفتح التطبيق ورؤية التفسير الكامل وتغيير إعدادات الألوان.",
                                "1. Long press on an empty space on your home screen.\n\n" +
                                "2. Select 'Widgets' from the menu.\n\n" +
                                "3. Look for the 'Qabas' app.\n\n" +
                                "4. Drag the widget and place it on your home screen.\n\n" +
                                "5. You can click the refresh icon inside the widget to change the verse or hadith at any time.\n\n" +
                                "6. Click the widget to open the app to read the full explanation and change color settings."
                            ),
                            color = Color.White,
                            fontSize = 16.sp,
                            lineHeight = 28.sp,
                            textAlign = if (currentLang == "ar") TextAlign.Right else TextAlign.Left
                        )
                    }
                }
                
                Text(
                    text = getLangString(currentLang, "شارك التطبيق .. شارك الأجر", "Share the app .. Share the reward"),
                    color = SoftGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            }
        }
    }
}
