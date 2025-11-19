package com.valtecna.iadoc.llm

interface LLMService {
    fun generateDocumentation(context: String, pro: Boolean): String
}

enum class Provider {
    OpenAI, Groq, Anthropic
}
