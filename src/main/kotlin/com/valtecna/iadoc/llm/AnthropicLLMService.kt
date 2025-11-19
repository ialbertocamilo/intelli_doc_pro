package com.valtecna.iadoc.llm

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.valtecna.iadoc.Constants
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class AnthropicLLMService(
    private val apiKey: String,
    private val model: String = Constants.API.ANTHROPIC_MODEL_DEFAULT
) : LLMService {

    private val gson = Gson()
    private val httpClient = HttpClient.newHttpClient()

    override fun generateDocumentation(context: String, pro: Boolean): String {
        return try {
            val payload = buildPayload(buildSystemPrompt(), buildUserPrompt(context))
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            extractContent(response.body())
        } catch (e: Exception) {
            fallback(parseError(e))
        }
    }

    private fun buildPayload(system: String, user: String): String = gson.toJson(JsonObject().apply {
        addProperty("model", model)
        addProperty("max_tokens", Constants.API.MAX_TOKENS)
        addProperty("temperature", Constants.API.TEMPERATURE)
        add("system", gson.toJsonTree(system))
        add("messages", gson.toJsonTree(listOf(mapOf("role" to "user", "content" to user))))
    })

    private fun buildSystemPrompt(): String = Constants.Prompts.SYSTEM_PROMPT_PRO

    private fun buildUserPrompt(context: String): String = "Code context:\n$context"

    private fun extractContent(body: String): String {
        return try {
            gson.fromJson(body, JsonObject::class.java)
                .getAsJsonArray("content")
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString
                ?.takeIf { it.isNotBlank() }
                ?: fallback("${Constants.Messages.NO_CONTENT_FIELD}: $body")
        } catch (e: Exception) {
            fallback("Parse error: ${e.message}")
        }
    }

    private fun parseError(e: Exception): String = when {
        e.message?.contains("401") == true -> "Invalid API Key"
        e.message?.contains("403") == true -> "Access forbidden"
        e.message?.contains("429") == true -> "Rate limit exceeded"
        else -> e.message ?: "Unknown error"
    }

    private fun fallback(msg: String): String = "<html><body><h3>⚠️ Anthropic Error</h3><p>$msg</p></body></html>"
}
