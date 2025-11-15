package com.valtecna.iadoc.llm

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.valtecna.iadoc.Constants
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest

/**
 * AWS Bedrock LLM Service implementation.
 * Supports Claude 3.5 Sonnet and other Bedrock models.
 *
 * Authentication options:
 * 1. Explicit credentials: accessKeyId and secretAccessKey (recommended)
 * 2. Empty credentials: Uses default AWS credentials chain (IAM role, env vars, ~/.aws/credentials)
 *
 * @param accessKeyId AWS Access Key ID (optional, uses default credentials if empty)
 * @param secretAccessKey AWS Secret Access Key (optional, uses default credentials if empty)
 * @param model Bedrock model ID (configurable)
 * @param region AWS region (configurable)
 */
class BedrockLLMService(
    private val accessKeyId: String,
    private val secretAccessKey: String,
    private val model: String = Constants.API.BEDROCK_MODEL_DEFAULT,
    private val region: String = Constants.API.BEDROCK_REGION_DEFAULT
) : LLMService {

    private val gson = Gson()

    override fun generateDocumentation(context: String, pro: Boolean): String {
        return try {
            val client = createBedrockClient()
            val systemPrompt = buildSystemPrompt(pro)
            val userPrompt = buildUserPrompt(context)
            val payload = buildClaudePayload(systemPrompt, userPrompt)

            val request = InvokeModelRequest.builder()
                .modelId(model)
                .body(SdkBytes.fromUtf8String(payload))
                .build()

            val response = client.invokeModel(request)
            val responseBody = response.body().asUtf8String()

            extractContent(responseBody)
        } catch (e: Exception) {
            fallback("Bedrock error: ${e.message ?: "Unknown error"}")
        }
    }

    private fun createBedrockClient(): BedrockRuntimeClient {
        return if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
            // Use default AWS credentials chain (IAM role, env vars, ~/.aws/credentials)
            BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .build()
        } else {
            // Use explicit credentials from settings
            val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()
        }
    }

    private fun buildClaudePayload(systemPrompt: String, userPrompt: String): String {
        val payload = JsonObject().apply {
            addProperty("anthropic_version", "bedrock-2023-05-31")
            add("system", gson.toJsonTree(listOf(
                mapOf("type" to "text", "text" to systemPrompt)
            )))
            add("messages", gson.toJsonTree(listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf("type" to "text", "text" to userPrompt)
                    )
                )
            )))
            addProperty("max_tokens", Constants.API.MAX_TOKENS)
            addProperty("temperature", Constants.API.TEMPERATURE)
        }
        return gson.toJson(payload)
    }

    private fun buildSystemPrompt(pro: Boolean): String {
        return if (pro) Constants.Prompts.SYSTEM_PROMPT_PRO else Constants.Prompts.SYSTEM_PROMPT_FREE
    }

    private fun buildUserPrompt(context: String): String = "Code context:\n$context"

    private fun extractContent(responseBody: String): String {
        try {
            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)

            // Claude response format: { "content": [{ "type": "text", "text": "..." }], ... }
            val contentArray = jsonResponse.getAsJsonArray("content")
            if (contentArray != null && contentArray.size() > 0) {
                val firstContent = contentArray[0].asJsonObject
                val text = firstContent.get("text")?.asString

                if (text != null && text.isNotBlank()) {
                    return text
                }
            }

            return fallback("${Constants.Messages.NO_CONTENT_FIELD}: $responseBody")
        } catch (e: Exception) {
            return fallback("Failed to parse Bedrock response: ${e.message}")
        }
    }

    private fun fallback(msg: String): String = """
        <html><body>
        <h3>⚠️ Error with AWS Bedrock</h3>
        <p>$msg</p>
        </body></html>
    """.trimIndent()
}
