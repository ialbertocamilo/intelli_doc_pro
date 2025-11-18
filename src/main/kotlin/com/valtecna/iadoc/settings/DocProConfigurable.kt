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
    private val bedrockRegionField = JTextField()

    // Panels for provider-specific settings
    private val openaiSettingsPanel = JPanel()
    private val groqSettingsPanel = JPanel()
    private val bedrockSettingsPanel = JPanel()
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

        val licenseRow = JPanel(BorderLayout())
        licenseRow.add(licenseStatusLabel, BorderLayout.CENTER)

        val providerRow = JPanel(BorderLayout())
        providerRow.add(JLabel("LLM Provider:"), BorderLayout.WEST)
        providerRow.add(providerBox, BorderLayout.CENTER)

        // API Key row (for all providers: OpenAI, Groq, and Bedrock)
        apiKeyRow.add(JLabel("API Key:"), BorderLayout.WEST)
        apiKeyRow.add(apiKeyField, BorderLayout.CENTER)

        // Setup provider-specific panels
        setupOpenAISettings()
        setupGroqSettings()
        setupBedrockSettings()

        providerBox.addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED) {
                updateProviderSettings()
            }
        }

        form.add(hintsPanel)
        form.add(Box.createVerticalStrut(20))
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

        val regionRow = JPanel(BorderLayout())
        regionRow.add(JLabel("AWS Region:"), BorderLayout.WEST)
        regionRow.add(bedrockRegionField, BorderLayout.CENTER)

        val modelRow = JPanel(BorderLayout())
        modelRow.add(JLabel("Bedrock Model:"), BorderLayout.WEST)
        modelRow.add(bedrockModelBox, BorderLayout.CENTER)

        bedrockSettingsPanel.add(regionRow)
        bedrockSettingsPanel.add(Box.createVerticalStrut(5))
        bedrockSettingsPanel.add(modelRow)
    }

    private fun updateProviderSettings() {
        providerSettingsContainer.removeAll()

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
        when (LicenseChecker.isLicensed()) {
            true -> {
                licenseStatusLabel.text = "PRO (Valid license activated)"
                licenseStatusLabel.font = licenseStatusLabel.font.deriveFont(Font.BOLD)
            }
            false -> {
                licenseStatusLabel.text = "Trial - 7 days free trial available"
            }
            null -> {
                licenseStatusLabel.text = "Checking license status..."
            }
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
        s.apiKey = apiKeyField.text
        s.openaiModel = openaiModelBox.selectedItem as String
        s.groqModel = groqModelBox.selectedItem as String
        s.bedrockModel = bedrockModelBox.selectedItem as String
        s.bedrockRegion = bedrockRegionField.text
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
        apiKeyField.text = s.apiKey
        openaiModelBox.selectedItem = s.openaiModel
        groqModelBox.selectedItem = s.groqModel
        bedrockModelBox.selectedItem = s.bedrockModel
        bedrockRegionField.text = s.bedrockRegion
        showComplexityHintsCheckbox.isSelected = s.showComplexityHints
        showSecurityHintsCheckbox.isSelected = s.showSecurityHints
        showPerformanceHintsCheckbox.isSelected = s.showPerformanceHints
        updateLicenseStatus()
        updateProviderSettings()
    }

    override fun getDisplayName(): String = "IntelliDoc Professional"
}

