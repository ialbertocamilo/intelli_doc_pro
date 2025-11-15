package com.valtecna.iadoc.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.llm.Provider

enum class Plan { FREE, PRO }

@State(name = "DocProSettings", storages = [Storage("DocProSettings.xml")])
@Service(Service.Level.APP)
class DocProSettingsState : PersistentStateComponent<DocProSettingsState> {
    var provider: Provider = Provider.OpenAI
    var plan: Plan = Plan.FREE
    var apiKey: String = ""
    var dailyCount: Int = 0
    var lastResetEpochDay: Long = 0
    var freeDailyLimit: Int = Constants.Limits.FREE_DAILY_LIMIT

    // Model configuration per provider
    var openaiModel: String = Constants.API.OPENAI_MODEL_DEFAULT
    var groqModel: String = Constants.API.GROQ_MODEL_DEFAULT
    var bedrockModel: String = Constants.API.BEDROCK_MODEL_DEFAULT
    var bedrockRegion: String = Constants.API.BEDROCK_REGION_DEFAULT

    // Bedrock-specific credentials (separate from apiKey which is for OpenAI/Groq)
    var bedrockAccessKeyId: String = ""
    var bedrockSecretAccessKey: String = ""

    override fun getState(): DocProSettingsState = this
    override fun loadState(state: DocProSettingsState) { XmlSerializerUtil.copyBean(state, this) }

    fun resetIfNewDay(epochDay: Long) {
        if (lastResetEpochDay != epochDay) {
            lastResetEpochDay = epochDay
            dailyCount = 0
        }
    }
}

