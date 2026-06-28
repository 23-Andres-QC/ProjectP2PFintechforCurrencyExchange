package com.example.p2p.data.remote.model

import com.google.gson.annotations.SerializedName

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val temperature: Double = 0.7
)

data class GroqChoice(
    val message: GroqMessage
)

data class GroqResponse(
    val choices: List<GroqChoice>
)
