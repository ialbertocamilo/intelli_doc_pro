package com.valtecna.iadoc.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.license.LicenseChecker
import com.valtecna.iadoc.llm.Provider
import java.awt.BorderLayout
import java.awt.Font
import java.awt.event.ItemEvent
import javax.swing.*

class DocProConfigurable : Configurable {
    private val panel = JPanel(BorderLayout())
    private val providerBox = JComboBox(Provider.values())
    private val apiKeyField = JTextField()
    private val apiKeyRow = JPanel(BorderLayout())  // Store reference to hide/show
    private val licenseStatusLabel = JBLabel()

    // Model configuration per provider
    private val openaiModelBox = JComboBox(Constants.API.OPENAI_MODELS)
    private val groqModelBox = JComboBox(Constants.API.GROQ_MODELS)
    private val bedrockModelBox = JComboBox(Constants.API.BEDROCK_MODELS)
    private val bedrockRegionField = JTextField()  // Changed to TextField

    // Bedrock-specific credential fields
    private val bedrockAccessKeyIdField = JTextField()
    private val bedrockSecretAccessKeyField = JPasswordField()

    // Panels for provider-specific settings
    private val openaiSettingsPanel = JPanel()
    private val groqSettingsPanel = JPanel()
    private val bedrockSettingsPanel = JPanel()
    private val providerSettingsContainer = JPanel(BorderLayout())

    init {
        val form = JPanel()
        form.layout = BoxLayout(form, BoxLayout.Y_AXIS)

        val licenseRow = JPanel(BorderLayout())
        licenseRow.add(JLabel("License Status:"), BorderLayout.WEST)
        updateLicenseStatus()
        licenseRow.add(licenseStatusLabel, BorderLayout.CENTER)

        val providerRow = JPanel(BorderLayout())
        providerRow.add(JLabel("LLM Provider:"), BorderLayout.WEST)
        providerRow.add(providerBox, BorderLayout.CENTER)

        // API Key row (only for OpenAI and Groq - NOT for Bedrock)
        apiKeyRow.add(JLabel("API Key:"), BorderLayout.WEST)
        apiKeyRow.add(apiKeyField, BorderLayout.CENTER)

        // Setup provider-specific panels
        setupOpenAISettings()
        setupGroqSettings()
        setupBedrockSettings()

        // Add listener to show/hide provider-specific settings and API Key field
        providerBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                updateProviderSettings()
            }
        }

        form.add(licenseRow)
        form.add(Box.createVerticalStrut(10))
        form.add(providerRow)
        form.add(apiKeyRow)
        form.add(Box.createVerticalStrut(15))
        form.add(providerSettingsContainer)

        panel.add(form, BorderLayout.NORTH)
    }

    private fun setupOpenAISettings() {
        openaiSettingsPanel.layout = BoxLayout(openaiSettingsPanel, BoxLayout.Y_AXIS)

        val modelRow = JPanel(BorderLayout())
        modelRow.add(JLabel("OpenAI Model:"), BorderLayout.WEST)
        modelRow.add(openaiModelBox, BorderLayout.CENTER)

        openaiSettingsPanel.add(modelRow)
    }

    private fun setupGroqSettings() {
        groqSettingsPanel.layout = BoxLayout(groqSettingsPanel, BoxLayout.Y_AXIS)

        val modelRow = JPanel(BorderLayout())
        modelRow.add(JLabel("Groq Model:"), BorderLayout.WEST)
        modelRow.add(groqModelBox, BorderLayout.CENTER)

        groqSettingsPanel.add(modelRow)
    }

    private fun setupBedrockSettings() {
        bedrockSettingsPanel.layout = BoxLayout(bedrockSettingsPanel, BoxLayout.Y_AXIS)

        val accessKeyIdRow = JPanel(BorderLayout())
        accessKeyIdRow.add(JLabel("AWS Access Key ID:"), BorderLayout.WEST)
        accessKeyIdRow.add(bedrockAccessKeyIdField, BorderLayout.CENTER)

        val secretAccessKeyRow = JPanel(BorderLayout())
        secretAccessKeyRow.add(JLabel("AWS Secret Access Key:"), BorderLayout.WEST)
        secretAccessKeyRow.add(bedrockSecretAccessKeyField, BorderLayout.CENTER)

        val regionRow = JPanel(BorderLayout())
        regionRow.add(JLabel("AWS Region:"), BorderLayout.WEST)
        regionRow.add(bedrockRegionField, BorderLayout.CENTER)

        val modelRow = JPanel(BorderLayout())
        modelRow.add(JLabel("Bedrock Model:"), BorderLayout.WEST)
        modelRow.add(bedrockModelBox, BorderLayout.CENTER)

        bedrockSettingsPanel.add(accessKeyIdRow)
        bedrockSettingsPanel.add(Box.createVerticalStrut(5))
        bedrockSettingsPanel.add(secretAccessKeyRow)
        bedrockSettingsPanel.add(Box.createVerticalStrut(5))
        bedrockSettingsPanel.add(regionRow)
        bedrockSettingsPanel.add(Box.createVerticalStrut(5))
        bedrockSettingsPanel.add(modelRow)
    }

    private fun updateProviderSettings() {
        providerSettingsContainer.removeAll()

        // Show/hide API Key field based on provider
        // Bedrock uses its own Access Key ID and Secret Access Key fields
        val isBedrock = providerBox.selectedItem == Provider.Bedrock
        apiKeyRow.isVisible = !isBedrock

        when (providerBox.selectedItem as? Provider) {
            Provider.OpenAI -> providerSettingsContainer.add(openaiSettingsPanel, BorderLayout.NORTH)
            Provider.Groq -> providerSettingsContainer.add(groqSettingsPanel, BorderLayout.NORTH)
            Provider.Bedrock -> providerSettingsContainer.add(bedrockSettingsPanel, BorderLayout.NORTH)
            else -> {}
        }
        providerSettingsContainer.revalidate()
        providerSettingsContainer.repaint()
    }

    private fun updateLicenseStatus() {
        val isPro = LicenseChecker.isPro()
        if (isPro) {
            licenseStatusLabel.text = "PRO (Valid license detected)"
            licenseStatusLabel.font = licenseStatusLabel.font.deriveFont(Font.BOLD)
        } else {
            licenseStatusLabel.text = "FREE (No license - Limit: 10 daily queries)"
        }
    }

    override fun createComponent(): JComponent {
        updateLicenseStatus()
        updateProviderSettings()  // Show initial provider settings
        return panel
    }

    override fun isModified(): Boolean {
        val s = service<DocProSettingsState>()
        return providerBox.selectedItem != s.provider ||
                apiKeyField.text != s.apiKey ||
                openaiModelBox.selectedItem != s.openaiModel ||
                groqModelBox.selectedItem != s.groqModel ||
                bedrockModelBox.selectedItem != s.bedrockModel ||
                bedrockRegionField.text != s.bedrockRegion ||
                bedrockAccessKeyIdField.text != s.bedrockAccessKeyId ||
                String(bedrockSecretAccessKeyField.password) != s.bedrockSecretAccessKey
    }

    override fun apply() {
        val s = service<DocProSettingsState>()
        s.provider = providerBox.selectedItem as Provider
        s.apiKey = apiKeyField.text
        s.openaiModel = openaiModelBox.selectedItem as String
        s.groqModel = groqModelBox.selectedItem as String
        s.bedrockModel = bedrockModelBox.selectedItem as String
        s.bedrockRegion = bedrockRegionField.text
        s.bedrockAccessKeyId = bedrockAccessKeyIdField.text
        s.bedrockSecretAccessKey = String(bedrockSecretAccessKeyField.password)
    }

    override fun reset() {
        val s = service<DocProSettingsState>()
        providerBox.selectedItem = s.provider
        apiKeyField.text = s.apiKey
        openaiModelBox.selectedItem = s.openaiModel
        groqModelBox.selectedItem = s.groqModel
        bedrockModelBox.selectedItem = s.bedrockModel
        bedrockRegionField.text = s.bedrockRegion
        bedrockAccessKeyIdField.text = s.bedrockAccessKeyId
        bedrockSecretAccessKeyField.text = s.bedrockSecretAccessKey
        updateLicenseStatus()
        updateProviderSettings()  // Update displayed settings
    }

    override fun getDisplayName(): String = "IntelliDoc AI"
}

