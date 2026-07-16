package com.taoufikcode.chat.data.network

import com.taoufikcode.core.domain.logging.KrossChatLogger
import io.ktor.client.statement.bodyAsText
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readLine
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiContent(val role: String, val parts: List<GeminiPart>)

@Serializable
data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

@Serializable
data class GeminiCandidate(val content: GeminiContentResponse? = null)

@Serializable
data class GeminiContentResponse(val parts: List<GeminiPartResponse>? = null)

@Serializable
data class GeminiPartResponse(val text: String? = null)

class GeminiClient(
    private val json: Json,
    private val logger: KrossChatLogger
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    fun generateResponseStream(apiKey: String, history: List<GeminiContent>): Flow<String> = flow {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?alt=sse&key=$apiKey"
        val request = GeminiRequest(contents = history)

        logger.i("GeminiClient") { "Starting Gemini request. History size: ${history.size}" }

        try {
            httpClient.preparePost(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.execute { response ->
                logger.i("GeminiClient") { "HTTP response received. Status: ${response.status.value}" }
                if (response.status.value !in 200..299) {
                    val errorBody = response.bodyAsText()
                    logger.e("GeminiClient") { "Error response body: $errorBody" }
                    throw Exception("HTTP ${response.status.value}: $errorBody")
                }

                val channel = response.bodyAsChannel()
                var chunkCount = 0
                while (!channel.isClosedForRead) {
                    val line = channel.readLine() ?: break
                    logger.d("GeminiClient") { "Received line: $line" }
                    if (line.startsWith("data: ")) {
                        val jsonStr = line.removePrefix("data: ").trim()
                        if (jsonStr.isNotEmpty()) {
                            try {
                                val chunk = json.decodeFromString<GeminiResponse>(jsonStr)
                                val text = chunk.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                if (text != null) {
                                    chunkCount++
                                    emit(text)
                                }
                            } catch (e: Exception) {
                                logger.w("GeminiClient") { "Failed to parse chunk JSON: ${e.message}" }
                            }
                        }
                    }
                }
                logger.i("GeminiClient") { "Streaming completed. Total chunks received: $chunkCount" }
            }
        } catch (e: Exception) {
            logger.e("GeminiClient", e) { "Error in generateResponseStream: ${e.message}" }
            emit("\n[Error generating response: ${e.message}]")
        }
    }
}
