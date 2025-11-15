package com.valtecna.iadoc.llm

import com.valtecna.iadoc.Constants
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class GroqLLMService(
    private val apiKey: String,
    private val model: String = Constants.API.GROQ_MODEL_DEFAULT
) : LLMService {
    override fun generateDocumentation(context: String, pro: Boolean): String {
        if (apiKey.isBlank()) return fallback(Constants.Messages.API_KEY_NOT_CONFIGURED)

        val client = HttpClient.newHttpClient()
        val systemPrompt = buildSystemPrompt(pro)
        val userPrompt = buildUserPrompt(context)
        val payload = buildJsonPayload(systemPrompt, userPrompt)
        val request = buildHttpRequest(payload)

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                extractContent(response.body())
            } else {
                fallback("HTTP ${response.statusCode()}: ${response.body().take(Constants.Limits.ERROR_MESSAGE_MAX_LENGTH)}")
            }
        } catch (e: Exception) {
            fallback(e.message ?: "error")
        }
    }

    private fun buildJsonPayload(systemPrompt: String, userPrompt: String): String {
        return """
            {"model":"$model","messages":[
              {"role":"system","content":${jsonString(systemPrompt)}},
              {"role":"user","content":${jsonString(userPrompt)}}
            ],"temperature":${Constants.API.TEMPERATURE}}
        """.trimIndent()
    }

    private fun buildHttpRequest(payload: String): HttpRequest {
        return HttpRequest.newBuilder()
            .uri(URI.create(Constants.API.GROQ_BASE_URL))
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()
    }

    private fun buildSystemPrompt(pro: Boolean): String {
        return if (pro) Constants.Prompts.SYSTEM_PROMPT_PRO else Constants.Prompts.SYSTEM_PROMPT_FREE
    }

    private fun buildUserPrompt(context: String): String = "Code context:\n$context"

    private fun jsonString(s: String): String {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
    }

    private fun extractContent(body: String): String {
        val idx = body.indexOf(Constants.JSON.CONTENT_FIELD)
        if (idx == -1) {
            return fallback("${Constants.Messages.NO_CONTENT_FIELD}: ${body.take(Constants.Limits.ERROR_MESSAGE_MAX_LENGTH)}")
        }

        var i = idx + Constants.JSON.CONTENT_FIELD_LENGTH
        val sb = StringBuilder()

        while (i < body.length) {
            val c = body[i]
            if (c == '\"' && (i == idx + Constants.JSON.CONTENT_FIELD_LENGTH || body[i - 1] != '\\')) break
            if (c == '\\' && i + 1 < body.length && body[i + 1] == '\\') {
                sb.append('\\')
                i++
            } else {
                sb.append(c)
            }
            i++
        }

        var result = sb.toString()
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")

        result = decodeUnicodeEscapes(result)

        if (result.isBlank()) return fallback(Constants.Messages.EMPTY_CONTENT)
        return result
    }

    private fun decodeUnicodeEscapes(input: String): String {
        val pattern = Regex("\\\\u([0-9a-fA-F]{${Constants.Limits.UNICODE_HEX_LENGTH}})")
        return pattern.replace(input) { matchResult ->
            val code = matchResult.groupValues[1].toInt(16)
            code.toChar().toString()
        }
    }

    private fun fallback(msg: String): String = """
        <html><body>
        <h3>⚠️ Error con Groq</h3>
        <p>$msg</p>
        </body></html>
    """.trimIndent()
}
