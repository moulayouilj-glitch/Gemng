package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.GameHistoryEntity
import com.example.data.models.GameReport
import com.example.data.models.DevelopmentMilestone
import com.example.data.models.GameAspect
import com.example.data.models.GuideTip
import com.example.ui.AnalysisUiState
import com.example.ui.ChatMessage
import com.example.ui.GameViewModel
import com.example.ui.theme.MyApplicationTheme

// Premium color parameters representing cyber slate aesthetic
private val CyberBgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF070B18), Color(0xFF111827))
)
private val CyberCardBg = Color(0xFF1E293B).copy(alpha = 0.7f)
private val CyberNeonMint = Color(0xFF10B981) // Neon Green
private val CyberNeonBlue = Color(0xFF0EA5E9) // Neon Blue
private val CyberNeonPink = Color(0xFFEC4899) // Neon Pink
private val SlateTextSecondary = Color(0xFF94A3B8)

// Localization lookup table for bilingual support (Arabic / English)
private val locales = mapOf(
    "ar" to mapOf(
        "app_title" to "رفيــق الألـعاب الذكـي",
        "app_subtitle" to "دليلك الشخصي بالذكاء الاصطناعي لتحليل أي لعبة، مراحل تطويرها، أسرارها وطرق احترافها.",
        "search_hint" to "اكتب اسم أي لعبة هنا لاستكشافها...",
        "search_btn" to "تحليـل بالذكاء الاصطناعي",
        "recent_searches" to "الأرشيف ومخزن الألعاب السابق استكشافها",
        "suggestions" to "اقتراحات الألعاب الشائعة:",
        "creator" to "المطور / المبتكر الرئيسي:",
        "release_date" to "تاريخ الإطلاق الأولي:",
        "stages_title" to "Chronology ✦ خط زمن مراحل النمو وتطور اللعبة",
        "details_tab" to "تحليل وبنية اللعبة",
        "guide_tab" to "دليل احترافي وأسرار",
        "chat_tab" to "الرفيق التفاعلي للعبة",
        "no_history" to "لم تقم باستكشاف أي لعبة بعد. ابدأ بكتابة اسم لعبتك المفضلة الآن!",
        "history_clear" to "مسح كامل السجلات",
        "chat_placeholder" to "اسأل الرفيق الذكي أي سؤال إضافي عن هذه اللعبة...",
        "chat_send" to "إرسال",
        "chat_initial" to "مرحباً بك! أنا رفيق اللعبة الذكي الخاص بك. اسألني عن تفاصيل قصة اللعبة (Lore)، آليات اللعب، أفضل الأسلحة أو الأماكن السرية!",
        "chat_loading" to "جاري تحميل الإجابة من قاعدة المعرفة للعبة...",
        "error_title" to "عذراً، واجهنا مشكلة",
        "empty_title" to "جاهز لرحلة استكشاف الألعاب؟",
        "empty_desc" to "أدخل اسم أي لعبة فيديو (مثل: Cyberpunk, Minecraft, FIFA) أو انقر على الشارات المقترحة أعلاه لتحصل على لوحة تحليلية متكاملة كالمحترفين.",
        "loading_analysis" to "جاري التواصل مع العقل الاصطناعي... نجمع تفاصيل التطوير والقصة ونحضر النصائح الإستراتيجية لك..."
    ),
    "en" to mapOf(
        "app_title" to "Smart Game Companion",
        "app_subtitle" to "Your personal AI gaming database to analyze development milestones, mechanics, and master strategies.",
        "search_hint" to "Enter any game name to analyze...",
        "search_btn" to "Analyze with AI",
        "recent_searches" to "Local Game Vault & History",
        "suggestions" to "Popular Suggestions:",
        "creator" to "Lead Developer / Studio:",
        "release_date" to "Initial Launch Date:",
        "stages_title" to "Chronology ✦ Crucial Development Timeline",
        "details_tab" to "Mechanics & Details",
        "guide_tab" to "Pro Strategy & Secrets",
        "chat_tab" to "Interactive Game Companion",
        "no_history" to "You have not analyzed any games yet. Try searching for a game now!",
        "history_clear" to "Clear History Vault",
        "chat_placeholder" to "Ask the companion any follow-up question...",
        "chat_send" to "Send",
        "chat_initial" to "Hello! I am your interactive companion for this game. Ask me about build setups, story lore, hidden achievements, or boss strategies!",
        "chat_loading" to "AI companion is crafting an answer from gaming lore...",
        "error_title" to "An Error Occured",
        "empty_title" to "Ready to Analyze Games?",
        "empty_desc" to "Write down a video game title or select one from the recommendation chips above to construct a detailed hub mapping design choices to pro secrets.",
        "loading_analysis" to "Calling Generative AI engine... Decrypting code history, system rules, and design details..."
    )
)

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                val currentLang by viewModel.languageCode.collectAsStateWithLifecycle()
                
                // Enforce RTL context if language is Arabic
                val layoutDirection = if (currentLang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr
                
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyberBgGradient)
                    ) { innerPadding ->
                        GameCompanionDashboard(
                            viewModel = viewModel,
                            lang = currentLang,
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCompanionDashboard(
    viewModel: GameViewModel,
    lang: String,
    modifier: Modifier = Modifier
) {
    val analysisState by viewModel.analysisState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var searchInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Helper translation accessor
    val getString = { key: String -> locales[lang]?.get(key) ?: key }

    val popularGames = listOf(
        "Elden Ring",
        "Minecraft",
        "The Witcher 3",
        "Hollow Knight",
        "Grand Theft Auto V",
        "Cyberpunk 2077"
    )

    LazyColumn(
        modifier = modifier
            .background(CyberBgGradient)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Custom Cyber Header ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // GameController Brand Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Logo icon",
                        tint = CyberNeonMint,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = getString("app_title"),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(3.dp)
                                .background(CyberNeonMint, shape = RoundedCornerShape(2.dp))
                        )
                    }
                }

                // Interactive Control Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home/Reset option
                    IconButton(
                        onClick = { viewModel.resetToHome() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .testTag("home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home button",
                            tint = Color.White
                        )
                    }

                    // On-the-fly Language Toggle Switch
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberNeonBlue.copy(alpha = 0.2f),
                            contentColor = CyberNeonBlue
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberNeonBlue.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("lang_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == "ar") "English" else "العربية",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Subtitle slogan
        item {
            Text(
                text = getString("app_subtitle"),
                color = SlateTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            )
        }

        // --- 2. Input Search Field ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text(getString("search_hint"), color = SlateTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_search_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberNeonMint,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.8f),
                            unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchInput.isNotBlank()) {
                                viewModel.analyzeGame(searchInput)
                                keyboardController?.hide()
                            }
                        }),
                        trailingIcon = {
                            if (searchInput.isNotEmpty()) {
                                IconButton(onClick = { searchInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear text",
                                        tint = SlateTextSecondary
                                    )
                                }
                            }
                        }
                    )

                    Button(
                        onClick = {
                            if (searchInput.isNotBlank()) {
                                viewModel.analyzeGame(searchInput)
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_search_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberNeonMint,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(imageVector = Icons.Default.QueryStats, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getString("search_btn"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // --- 3. Quick Suggestions Row ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = getString("suggestions"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(popularGames) { game ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CyberCardBg)
                                .border(
                                    BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    searchInput = game
                                    viewModel.analyzeGame(game)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CyberNeonBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = game,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Space divider
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // --- 4. Content Area: Custom Animation Transition ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                when (val state = analysisState) {
                    is AnalysisUiState.Idle -> {
                        GameCompanionIdleView(
                            getString = getString,
                            history = history,
                            onLoad = { entity ->
                                searchInput = entity.title
                                viewModel.restoreGameFromHistory(entity)
                            },
                            onDelete = { title -> viewModel.deleteFromHistory(title) },
                            onClearAll = { viewModel.clearHistory() }
                        )
                    }
                    is AnalysisUiState.Loading -> {
                        GameCompanionLoadingView(getString = getString)
                    }
                    is AnalysisUiState.Error -> {
                        GameCompanionErrorView(
                            message = state.message,
                            getString = getString,
                            onRetry = {
                                if (searchInput.isNotBlank()) {
                                    viewModel.analyzeGame(searchInput)
                                }
                            }
                        )
                    }
                    is AnalysisUiState.Success -> {
                        GameCompanionSuccessView(
                            report = state.report,
                            viewModel = viewModel,
                            getString = getString
                        )
                    }
                }
            }
        }

        // Bottom space padding
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

// --- SUB-VIEWS IMPLEMENTATIONS ---

@Composable
fun GameCompanionIdleView(
    getString: (String) -> String,
    history: List<GameHistoryEntity>,
    onLoad: (GameHistoryEntity) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glorious glowing controller custom-drawn Illustration
        CyberGamepadIllustration(
            modifier = Modifier
                .size(240.dp)
                .padding(vertical = 12.dp)
        )

        Text(
            text = getString("empty_title"),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = getString("empty_desc"),
            color = SlateTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        // Local historical searches panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = CyberNeonBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = getString("recent_searches"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (history.isNotEmpty()) {
                        Text(
                            text = getString("history_clear"),
                            color = CyberNeonPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onClearAll() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (history.isEmpty()) {
                    Text(
                        text = getString("no_history"),
                        color = SlateTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.03f))
                                    .clickable { onLoad(item) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = SlateTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = item.title,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.creator,
                                            color = SlateTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDelete(item.title) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = CyberNeonPink.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameCompanionLoadingView(getString: (String) -> String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            color = CyberNeonMint,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = getString("loading_analysis"),
            color = Color.White,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun GameCompanionErrorView(
    message: String,
    getString: (String) -> String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F162A).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CyberNeonPink.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = CyberNeonPink,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = getString("error_title"),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                color = SlateTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = CyberNeonPink),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (getString("app_title").contains("رفيق")) "أعد المحاولة" else "Retry",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GameCompanionSuccessView(
    report: GameReport,
    viewModel: GameViewModel,
    getString: (String) -> String
) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    var followUpInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Success View State: 0 -> Mechanics Tab, 1 -> Pro Strategy Tab, 2 -> AI Chat Tab
    var activeTabIdx by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- A. GORGEOUS HERO HEADER CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.5.dp, CyberNeonMint.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Glow tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberNeonMint.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ANALYZED BY AI COMPANION ✦",
                        color = CyberNeonMint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                // Custom Game Title display with dynamic letters
                Text(
                    text = report.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )

                // Sub-Metadata details
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getString("creator"),
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = report.creator,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getString("release_date"),
                            color = SlateTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = report.releaseDate,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Synopsis description
                Text(
                    text = report.shortDescription,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                // High contrast horizontal genre Chips in hero screen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    report.genreTags.forEach { genre ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = genre,
                                color = CyberNeonBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- B. CHRONOLOGY TIMELINE (TRACING DEVELOPMENT STAGES) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CyberNeonBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = getString("stages_title"),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Render developmental milestones step-by-step with lines
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    report.developmentChronology.forEachIndexed { idx, milestone ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Draw decorative vertical line and glowing dot
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == 0) CyberNeonMint else CyberNeonBlue)
                                        .border(
                                            BorderStroke(
                                                2.dp,
                                                if (idx == 0) CyberNeonMint.copy(alpha = 0.4f) else CyberNeonBlue.copy(
                                                    alpha = 0.4f
                                                )
                                            ), CircleShape
                                        )
                                )
                                if (idx < report.developmentChronology.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(50.dp)
                                            .background(Color.White.copy(alpha = 0.15f))
                                    )
                                }
                            }

                            // Growth content card details next to line
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = milestone.stageName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = milestone.detail,
                                    color = SlateTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- C. DUAL SECTOR INTERACTIVE SEGMENTS (TABS) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // High contrast Tab indicators
            TabRow(
                selectedTabIndex = activeTabIdx,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTabIdx]),
                        color = CyberNeonMint
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = activeTabIdx == 0,
                    onClick = { activeTabIdx = 0 },
                    text = {
                        Text(
                            getString("details_tab"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = activeTabIdx == 1,
                    onClick = { activeTabIdx = 1 },
                    text = {
                        Text(
                            getString("guide_tab"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
                Tab(
                    selected = activeTabIdx == 2,
                    onClick = { activeTabIdx = 2 },
                    text = {
                        Text(
                            getString("chat_tab"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            AnimatedContent(
                targetState = activeTabIdx,
                label = "AspectTabTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Mechanics content view
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.mechanicsAndStructure.forEach { aspect ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = aspect.title,
                                            color = CyberNeonMint,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = aspect.details,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Strategy Guide view
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            report.proGuide.forEach { tip ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tip.tipTitle,
                                                color = CyberNeonBlue,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CyberNeonPink.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tip.category.uppercase(),
                                                    color = CyberNeonPink,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = tip.tipDescription,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Live AI Chat Companion view
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                            border = BorderStroke(1.dp, CyberNeonMint.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Scrollable conversation logger
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 240.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Welcome initial prompt
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = getString("chat_initial"),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    chatHistory.forEach { chatMsg ->
                                        val isUser = chatMsg.sender == "user"
                                        Box(
                                            modifier = Modifier
                                                .align(if (isUser) Alignment.End else Alignment.Start)
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                                    )
                                                )
                                                .background(if (isUser) CyberNeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                                .border(
                                                    BorderStroke(
                                                        0.5.dp,
                                                        if (isUser) CyberNeonBlue.copy(alpha = 0.4f) else Color.White.copy(
                                                            alpha = 0.1f
                                                    )
                                                ),
                                                RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                                    )
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = chatMsg.text,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    // Chat load indicator
                                    if (isChatLoading) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.Start)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = CyberNeonMint,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = getString("chat_loading"),
                                                color = SlateTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Interactive Text Sender input
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = followUpInput,
                                        onValueChange = { followUpInput = it },
                                        placeholder = { Text(getString("chat_placeholder"), color = SlateTextSecondary, fontSize = 11.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chat_input"),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.White),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberNeonBlue,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            if (followUpInput.isNotBlank() && !isChatLoading) {
                                                viewModel.askFollowUp(followUpInput, report)
                                                followUpInput = ""
                                                keyboardController?.hide()
                                            }
                                        },
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(CyberNeonBlue)
                                            .size(40.dp)
                                            .testTag("chat_send_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = getString("chat_send"),
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberGamepadIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // Draw multiple glowing ambient radial fields
        drawCircle(
            color = CyberNeonMint.copy(alpha = 0.05f),
            radius = centerX * 0.7f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = CyberNeonBlue.copy(alpha = 0.04f),
            radius = centerX * 0.45f,
            center = Offset(centerX, centerY)
        )

        // Gamepad body contour (beautiful, custom cyberpunk controller outline path)
        val bodyPath = Path().apply {
            moveTo(centerX - 100f, centerY - 60f)
            lineTo(centerX + 100f, centerY - 60f)
            quadraticTo(centerX + 180f, centerY - 50f, centerX + 180f, centerY + 20f)
            quadraticTo(centerX + 150f, centerY + 100f, centerX + 110f, centerY + 80f)
            quadraticTo(centerX, centerY + 40f, centerX - 110f, centerY + 80f)
            quadraticTo(centerX - 150f, centerY + 100f, centerX - 180f, centerY + 20f)
            quadraticTo(centerX - 180f, centerY - 50f, centerX - 100f, centerY - 60f)
            close()
        }

        drawPath(
            path = bodyPath,
            color = Color(0xFF1E293B),
            style = Fill
        )

        // Draw neon glow outlines
        drawPath(
            path = bodyPath,
            color = CyberNeonMint,
            style = Stroke(width = 4f)
        )

        // Controller D-Pad (Left hand)
        drawCircle(
            color = CyberNeonBlue,
            radius = 16f,
            center = Offset(centerX - 90f, centerY + 10f)
        )
        drawRect(
            color = CyberNeonBlue,
            topLeft = Offset(centerX - 102f, centerY - 2f),
            size = Size(24f, 24f)
        )

        // Controller Buttons (Right hand - glowing action points)
        drawCircle(
            color = CyberNeonPink,
            radius = 12f,
            center = Offset(centerX + 90f, centerY - 10f)
        )
        drawCircle(
            color = CyberNeonMint,
            radius = 12f,
            center = Offset(centerX + 115f, centerY + 15f)
        )

        // Controller Analog Joysticks
        drawCircle(
            color = Color(0xFF334155),
            radius = 24f,
            center = Offset(centerX - 40f, centerY + 35f)
        )
        drawCircle(
            color = CyberNeonBlue,
            radius = 14f,
            center = Offset(centerX - 40f, centerY + 35f)
        )

        drawCircle(
            color = Color(0xFF334155),
            radius = 24f,
            center = Offset(centerX + 40f, centerY + 35f)
        )
        drawCircle(
            color = CyberNeonMint,
            radius = 14f,
            center = Offset(centerX + 40f, centerY + 35f)
        )
    }
}

// Simple layout scroll holder in chat
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
