package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.GameRepository
import com.example.data.api.GeminiClient
import com.example.data.db.GameDatabase
import com.example.data.db.GameHistoryEntity
import com.example.data.models.*
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Loading : AnalysisUiState
    data class Success(val report: GameReport) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class ChatMessage(
    val sender: String, // "user" or "companion"
    val text: String
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    
    // UI Language: "ar" (default) or "en"
    private val _languageCode = MutableStateFlow("ar")
    val languageCode: StateFlow<String> = _languageCode.asStateFlow()

    // Key Analysis State
    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisState: StateFlow<AnalysisUiState> = _analysisState.asStateFlow()

    // Q&A Conversation State
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Moshi Adapters for serializing / deserializing Room fields
    private val moshi = GeminiClient.getMoshi()
    private val reportAdapter = moshi.adapter(GameReport::class.java)
    private val genreListAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    init {
        val db = GameDatabase.getDatabase(application)
        repository = GameRepository(db.gameHistoryDao())
    }

    // Historical entries observed reactively from Room
    val history: StateFlow<List<GameHistoryEntity>> = repository.history
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleLanguage() {
        _languageCode.value = if (_languageCode.value == "ar") "en" else "ar"
    }

    /**
     * Clear current model details to show greeting dashboard
     */
    fun resetToHome() {
        _analysisState.value = AnalysisUiState.Idle
        _chatHistory.value = emptyList()
    }

    /**
     * Delete an entry from history
     */
    fun deleteFromHistory(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGame(title)
        }
    }

    /**
     * Clear all searches
     */
    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    /**
     * Instantly restore an analyzed game from the history cache without hitting Gemini api
     */
    fun restoreGameFromHistory(entity: GameHistoryEntity) {
        viewModelScope.launch {
            try {
                val report = withContext(Dispatchers.Default) {
                    reportAdapter.fromJson(entity.jsonContent)
                }
                if (report != null) {
                    _analysisState.value = AnalysisUiState.Success(report)
                    _chatHistory.value = emptyList() // clear chats on new selection
                } else {
                    _analysisState.value = AnalysisUiState.Error("Failed to parse cached game details.")
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error("Error loading cached game: ${e.message}")
            }
        }
    }

    /**
     * Invoke direct REST api to get structured details about the game
     */
    fun analyzeGame(gameName: String) {
        if (gameName.isBlank()) return

        _analysisState.value = AnalysisUiState.Loading
        _chatHistory.value = emptyList()

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                _analysisState.value = AnalysisUiState.Error(
                    if (_languageCode.value == "ar")
                        "برجاء تهيئة مفتاح API الخاص بـ Gemini في نافذة الأسرار (Secrets panel) في Google AI Studio للبدء."
                    else
                        "Please configure your Gemini API Key in the Secrets panel in Google AI Studio to proceed."
                )
                return@launch
            }

            val prompt = buildGamePrompt(gameName, _languageCode.value)
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                )
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(apiKey, request)
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (rawText != null) {
                    val cleaned = cleanJsonResponse(rawText)
                    val report = withContext(Dispatchers.Default) {
                        reportAdapter.fromJson(cleaned)
                    }

                    if (report != null) {
                        _analysisState.value = AnalysisUiState.Success(report)

                        // Save in local Room Database instantly
                        saveToLocalDb(report)
                    } else {
                        _analysisState.value = AnalysisUiState.Error(
                            if (_languageCode.value == "ar") "فشل تحميل البيانات بشكل صحيح. يرجى المحاولة لاحقاً."
                            else "Failed to parse game data structure. Please retry."
                        )
                    }
                } else {
                    _analysisState.value = AnalysisUiState.Error(
                        if (_languageCode.value == "ar") "لم يستجب خادم الذكاء الاصطناعي بالبيانات المطلوبة."
                        else "AI service did not return any text output."
                    )
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error("Error: ${e.message}")
            }
        }
    }

    /**
     * Ask follow-up question
     */
    fun askFollowUp(question: String, currentReport: GameReport) {
        if (question.isBlank() || _isChatLoading.value) return

        val userMsg = ChatMessage("user", question)
        _chatHistory.value = _chatHistory.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val prompt = buildFollowUpPrompt(currentReport, question, _languageCode.value)
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            try {
                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(apiKey, request)
                }
                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                val reply = rawText ?: (
                    if (_languageCode.value == "ar") "عذراً، لم أستطع الحصول على رد لتساؤلك."
                    else "Sorry, I could not retrieve an answer for you."
                )

                _chatHistory.value = _chatHistory.value + ChatMessage("companion", reply)
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    "companion",
                    "Error: ${e.message}"
                )
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    private fun saveToLocalDb(report: GameReport) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val genreString = genreListAdapter.toJson(report.genreTags)
                val jsonString = reportAdapter.toJson(report)
                
                val entity = GameHistoryEntity(
                    title = report.title,
                    creator = report.creator,
                    releaseDate = report.releaseDate,
                    genreJson = genreString,
                    jsonContent = jsonString
                )
                repository.insertGame(entity)
            } catch (e: java.lang.Exception) {
                // Fail silently or log database insertion issues
            }
        }
    }

    private fun cleanJsonResponse(raw: String): String {
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun buildGamePrompt(gameName: String, langCode: String): String {
        val languageName = if (langCode == "ar") "Arabic" else "English"
        val fallbackExample = if (langCode == "ar") {
            """
            {
              "title": "اسم اللعبة الفعلي والمصحح",
              "creator": "اسم المطور أو الناشر",
              "releaseDate": "تاريخ الإصدار الأولي",
              "shortDescription": "وصف شيق ودقيق ومختصر يتكون من ٣ أسطر عن اللعبة بشكل مذهل",
              "genreTags": ["تصنيف ١", "تصنيف ٢"],
              "developmentChronology": [
                { "stageName": "التطوير الأولي", "detail": "تفاصيل دقيقة لمرحلة الفكرة والمشاركة الباكرة" },
                { "stageName": "ألفا وبيتا", "detail": "استعراض لاهتمامات اللاعبين والمشاكل المحلولة" },
                { "stageName": "الإطلاق الرسمي", "detail": "تفاصيل استراتيجية وعلمية ليوم الإطلاق والنجاح" }
              ],
              "mechanicsAndStructure": [
                { "title": "أسلوب اللعب والتحكم", "details": "شرح لآليات الحركة والتفاعل وكيفية إمتاع اللاعبين" },
                { "title": "متطلبات التشغيل والبيئة", "details": "شرح للأجهزة والمنصات التي تدعمها اللعبة بشكل تفصيلي" }
              ],
              "proGuide": [
                { "category": "نصائح للمبتدئين", "tipTitle": "التركيز والاستكشاف", "tipDescription": "كيفية البداية بشكل آمن وتدريجي دون خسارة مستلزماتك" }
              ]
            }
            """.trimIndent()
        } else {
            """
            {
              "title": "Corrected Game Title",
              "creator": "Studio or Publisher Developer",
              "releaseDate": "Initial Launch Date",
              "shortDescription": "An engaging, beautiful, brief 3-line synopsis of the game's theme and appeal",
              "genreTags": ["Genre1", "Genre2"],
              "developmentChronology": [
                { "stageName": "Concept & Pre-production", "detail": "Details of the design boards and planning phases" },
                { "stageName": "Alpha/Beta Phase", "detail": "Player tests, feedback, and key modifications made" },
                { "stageName": "Global Launch", "detail": "Sales numbers, critical feedback, launch build challenges" }
              ],
              "mechanicsAndStructure": [
                { "title": "Core Gameplay Loop", "details": "Explanation of interaction models, control feel, and pacing" },
                { "title": "Supported Platforms & Specs", "details": "Consoles, PC, mobile compatibility, and technical specs" }
              ],
              "proGuide": [
                { "category": "Beginner Tip", "tipTitle": "Explore Carefully", "tipDescription": "How to initiate resource collection and understand risks in early sessions" }
              ]
            }
            """.trimIndent()
        }

        return """
        You are an expert game analyst and AI gaming companion.
        Generate a highly educational, accurate, and comprehensive review and companion details for the game: "$gameName".
        
        You MUST output your response in $languageName.
        All text fields in the JSON response must be in $languageName.
        
        Your response must be a SINGLE valid JSON object containing comprehensive gaming analysis, exactly conforming to this JSON structure:
        
        $fallbackExample
        
        Please ensure the following details are covered fully:
        1. Correct spelling of the game's title, creator, publisher, and developmental stages.
        2. At least 3 development chronological stages (e.g., Pre-development/Concept, Production/Tests, Release/Post-launch Expansion) under "developmentChronology" outlining how it evolved.
        3. At least 2 or 3 mechanics panels (like Level structures, controls, systems, platforms) under "mechanicsAndStructure".
        4. At least 3 expert strategies (one beginner, one pro, one secrets) under "proGuide".
        
        CRITICAL: Only output the raw JSON. Do not include any HTML, markdown backticks or ```json block wrappers. Output purely the parsable JSON string.
        """.trimIndent()
    }

    private fun buildFollowUpPrompt(gameReport: GameReport, userQuestion: String, langCode: String): String {
        val languageName = if (langCode == "ar") "Arabic" else "English"
        return """
        You are the smart gaming companion for the game "${gameReport.title}" created by "${gameReport.creator}".
        
        Here is the brief context of the game:
        - Description: ${gameReport.shortDescription}
        - Release Date: ${gameReport.releaseDate}
        
        The user has a follow-up question:
        "$userQuestion"
        
        Answer this question in a friendly, helpful, and insightful manner. Be very knowledgeable about gaming lore, tips, and strategies.
        
        You MUST respond in $languageName. Keep your response concise (under 150 words) and nicely formatted with bullet points if applicable. Do not wrap in markdown code blocks.
        """.trimIndent()
    }
}
