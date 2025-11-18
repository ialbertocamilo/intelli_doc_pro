package com.valtecna.iadoc.inlays

import com.intellij.codeInsight.hints.*
import com.intellij.lang.Language
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.security.SecurityHints
import com.valtecna.iadoc.security.SecurityJavaHints
import com.valtecna.iadoc.security.SecurityKotlinHints
import com.valtecna.iadoc.security.Severity
import com.valtecna.iadoc.settings.DocProSettingsState
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class SecurityInlayProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey("com.valtecna.iadoc.security")
    override val name: String = "Security Analysis"
    override val previewText: String? = null

    override fun createSettings(): NoSettings = NoSettings()

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JPanel = JPanel()
        }
    }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector? {
        val settingsState = service<DocProSettingsState>()
        if (!settingsState.showSecurityHints) {
            return null
        }

        return SecurityCollector(editor, file.language.id)
    }

    private class SecurityCollector(editor: Editor, private val languageId: String) : FactoryInlayHintsCollector(editor) {

        private val javaAnalyzer = SecurityJavaHints()
        private val kotlinAnalyzer = SecurityKotlinHints()

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            val analyzer = getAnalyzer(languageId) ?: return true

            try {
                val issue = analyzer.analyze(element) ?: return true

                val icon = when (issue.severity) {
                    Severity.CRITICAL -> "🔴"
                    Severity.HIGH -> "🟠"
                    Severity.MEDIUM -> "🟡"
                    Severity.LOW -> "🔵"
                }

                val text = "$icon Security: ${issue.message}"
                val offset = element.textRange.startOffset

                sink.addBlockElement(
                    offset,
                    true,
                    true,
                    2,
                    factory.text(text)
                )
            } catch (e: Exception) {
            }

            return true
        }

        private fun getAnalyzer(languageId: String): SecurityHints? {
            return when (languageId) {
                Constants.Language.JAVA -> javaAnalyzer
                Constants.Language.KOTLIN -> kotlinAnalyzer
                else -> null
            }
        }
    }

    override fun isLanguageSupported(language: Language): Boolean {
        return when (language.id) {
            Constants.Language.JAVA -> true
            Constants.Language.KOTLIN -> true
            else -> false
        }
    }
}
