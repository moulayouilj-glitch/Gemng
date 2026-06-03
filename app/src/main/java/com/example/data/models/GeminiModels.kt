package com.example.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null,
    val topP: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Game Companion Report Models ---

@JsonClass(generateAdapter = true)
data class GameReport(
    val title: String,
    val creator: String,
    val releaseDate: String,
    val shortDescription: String,
    val genreTags: List<String>,
    val developmentChronology: List<DevelopmentMilestone>,
    val mechanicsAndStructure: List<GameAspect>,
    val proGuide: List<GuideTip>
)

@JsonClass(generateAdapter = true)
data class DevelopmentMilestone(
    val stageName: String,
    val detail: String
)

@JsonClass(generateAdapter = true)
data class GameAspect(
    val title: String,
    val details: String
)

@JsonClass(generateAdapter = true)
data class GuideTip(
    val category: String,
    val tipTitle: String,
    val tipDescription: String
)
