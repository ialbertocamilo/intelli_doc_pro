package com.valtecna.iadoc.settings

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.llm.Provider
import java.awt.BorderLayout
import java.awt.event.ItemEvent
import javax.swing.*

class DocProConfigurable : Configurable {
    private val panel = JPanel(BorderLayout())
    private val providerBox = JComboBox(Provider.values())
    private val apiKeyField = JTextField()
    private val apiKeyRow = JPanel(BorderLayout())

    // Model configuration per provider
    private val openaiModelBox = JComboBox(Constants.API.OPENAI_MODELS)
    private val groqModelBox = JComboBox(Constants.API.GROQ_MODELS)
    private val anthropicModelBox = JComboBox(Constants.API.ANTHROPIC_MODELS)

    // Panels for provider-specific settings
    private val openaiSettingsPanel = JPanel()
    private val groqSettingsPanel = JPanel()
    private val anthropicSettingsPanel = JPanel()
    private val providerSettingsContainer = JPanel(BorderLayout())

    private val showComplexityHintsCheckbox = JCheckBox("Complexity hints", true)
    private val showSecurityHintsCheckbox = JCheckBox("Security hints", true)
    private val showPerformanceHintsCheckbox = JCheckBox("Performance hints", true)

    init {
        val form = JPanel()
        form.layout = BoxLayout(form, BoxLayout.Y_AXIS)

        // Hints configuration section (at the top, aligned left)
        val hintsPanel = JPanel(BorderLayout())
        val hintsCheckboxes = JPanel()
        hintsCheckboxes.layout = BoxLayout(hintsCheckboxes, BoxLayout.Y_AXIS)

        showComplexityHintsCheckbox.alignmentX = JCheckBox.LEFT_ALIGNMENT
        showSecurityHintsCheckbox.alignmentX = JCheckBox.LEFT_ALIGNMENT
        showPerformanceHintsCheckbox.alignmentX = JCheckBox.LEFT_ALIGNMENT

        hintsCheckboxes.add(showComplexityHintsCheckbox)
        hintsCheckboxes.add(Box.createVerticalStrut(5))
        hintsCheckboxes.add(showSecurityHintsCheckbox)
        hintsCheckboxes.add(Box.createVerticalStrut(5))
        hintsCheckboxes.add(showPerformanceHintsCheckbox)

        hintsPanel.add(hintsCheckboxes, BorderLayout.WEST)

        val providerRow = JPanel(BorderLayout())
        providerRow.add(JLabel("LLM Provider:"), BorderLayout.WEST)
        providerRow.add(providerBox, BorderLayout.CENTER)

        // API Key row (for all providers)
        apiKeyRow.add(JLabel("API Key:"), BorderLayout.WEST)
        apiKeyRow.add(apiKeyField, BorderLayout.CENTER)

        // Setup provider-specific panels
        setupOpenAISettings()
        setupGroqSettings()
        setupAnthropicSettings()

        providerBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                updateProviderSettings()
                loadApiKeyForProvider()
            }
        }

        form.add(hintsPanel)
        form.add(Box.createVerticalStrut(20))
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

    private fun setupAnthropicSettings() {
        anthropicSettingsPanel.layout = BoxLayout(anthropicSettingsPanel, BoxLayout.Y_AXIS)

        val modelRow = JPanel(BorderLayout())
        modelRow.add(JLabel("Anthropic Model:"), BorderLayout.WEST)
        modelRow.add(anthropicModelBox, BorderLayout.CENTER)

        anthropicSettingsPanel.add(modelRow)
    }

    private fun loadApiKeyForProvider() {
        val s = service<DocProSettingsState>()
        apiKeyField.text = when (providerBox.selectedItem as? Provider) {
            Provider.OpenAI -> s.openaiApiKey
            Provider.Groq -> s.groqApiKey
            Provider.Anthropic -> s.anthropicApiKey
            else -> ""
        }
    }

    private fun updateProviderSettings() {
        providerSettingsContainer.removeAll()

        when (providerBox.selectedItem as? Provider) {
            Provider.OpenAI -> providerSettingsContainer.add(openaiSettingsPanel, BorderLayout.NORTH)
            Provider.Groq -> providerSettingsContainer.add(groqSettingsPanel, BorderLayout.NORTH)
            Provider.Anthropic -> providerSettingsContainer.add(anthropicSettingsPanel, BorderLayout.NORTH)
            else -> {}
        }
        providerSettingsContainer.revalidate()
        providerSettingsContainer.repaint()
    }

    override fun createComponent(): JComponent {
        updateProviderSettings()
        return panel
    }

    override fun isModified(): Boolean {
        val s = service<DocProSettingsState>()
        val currentApiKey = when (providerBox.selectedItem as? Provider) {
            Provider.OpenAI -> s.openaiApiKey
            Provider.Groq -> s.groqApiKey
            Provider.Anthropic -> s.anthropicApiKey
            else -> ""
        }
        return providerBox.selectedItem != s.provider ||
                apiKeyField.text != currentApiKey ||
                openaiModelBox.selectedItem != s.openaiModel ||
                groqModelBox.selectedItem != s.groqModel ||
                anthropicModelBox.selectedItem != s.anthropicModel ||
                showComplexityHintsCheckbox.isSelected != s.showComplexityHints ||
                showSecurityHintsCheckbox.isSelected != s.showSecurityHints ||
                showPerformanceHintsCheckbox.isSelected != s.showPerformanceHints
    }

    override fun apply() {
        val s = service<DocProSettingsState>()
        val oldShowComplexityHints = s.showComplexityHints
        val oldShowSecurityHints = s.showSecurityHints
        val oldShowPerformanceHints = s.showPerformanceHints

        s.provider = providerBox.selectedItem as Provider

        when (providerBox.selectedItem as? Provider) {
            Provider.OpenAI -> s.openaiApiKey = apiKeyField.text
            Provider.Groq -> s.groqApiKey = apiKeyField.text
            Provider.Anthropic -> s.anthropicApiKey = apiKeyField.text
            else -> {}
        }

        s.openaiModel = openaiModelBox.selectedItem as String
        s.groqModel = groqModelBox.selectedItem as String
        s.anthropicModel = anthropicModelBox.selectedItem as String
        s.showComplexityHints = showComplexityHintsCheckbox.isSelected
        s.showSecurityHints = showSecurityHintsCheckbox.isSelected
        s.showPerformanceHints = showPerformanceHintsCheckbox.isSelected

        if (oldShowComplexityHints != s.showComplexityHints ||
            oldShowSecurityHints != s.showSecurityHints ||
            oldShowPerformanceHints != s.showPerformanceHints) {
            ApplicationManager.getApplication().invokeLater {
                ProjectManager.getInstance().openProjects.forEach { project ->
                    // Clear inlay hints by updating all editors
                    EditorFactory.getInstance().allEditors.forEach { editor ->
                        val document = editor.document
                        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                        if (psiFile != null) {
                            // Clear existing inlays
                            editor.inlayModel.getInlineElementsInRange(0, document.textLength).forEach { it.dispose() }
                            editor.inlayModel.getBlockElementsInRange(0, document.textLength).forEach { it.dispose() }
                        }
                    }

                    // Restart code analyzer to regenerate hints if enabled
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    override fun reset() {
        val s = service<DocProSettingsState>()
        providerBox.selectedItem = s.provider
        loadApiKeyForProvider()
        openaiModelBox.selectedItem = s.openaiModel
        groqModelBox.selectedItem = s.groqModel
        anthropicModelBox.selectedItem = s.anthropicModel
        showComplexityHintsCheckbox.isSelected = s.showComplexityHints
        showSecurityHintsCheckbox.isSelected = s.showSecurityHints
        showPerformanceHintsCheckbox.isSelected = s.showPerformanceHints
        updateProviderSettings()
    }

    override fun getDisplayName(): String = "CodeHint Professional"
}

