package com.valtecna.iadoc.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.llm.Provider

@State(name = "DocProSettings", storages = [Storage("DocProSettings.xml")])
@Service(Service.Level.APP)
class DocProSettingsState : PersistentStateComponent<DocProSettingsState> {
    var provider: Provider = Provider.Anthropic

    // API Keys per provider
    var openaiApiKey: String = ""
    var groqApiKey: String = ""
    var anthropicApiKey: String = ""

    // Model configuration per provider
    var openaiModel: String = Constants.API.OPENAI_MODEL_DEFAULT
    var groqModel: String = Constants.API.GROQ_MODEL_DEFAULT
    var anthropicModel: String = Constants.API.ANTHROPIC_MODEL_DEFAULT
    var bedrockModel: String = Constants.API.BEDROCK_MODEL_DEFAULT
    var bedrockRegion: String = Constants.API.BEDROCK_REGION_DEFAULT

    // Complexity analysis feature toggle
    var showComplexityHints: Boolean = true

    // Security analysis feature toggle
    var showSecurityHints: Boolean = true

    // Performance analysis feature toggle
    var showPerformanceHints: Boolean = true

    override fun getState(): DocProSettingsState = this
    override fun loadState(state: DocProSettingsState) { XmlSerializerUtil.copyBean(state, this) }
}

