package com.valtecna.iadoc.inlays

import com.intellij.codeInsight.hints.*
import com.intellij.lang.Language
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.performance.PerformanceHints
import com.valtecna.iadoc.performance.PerformanceJavaHints
import com.valtecna.iadoc.performance.PerformanceKotlinHints
import com.valtecna.iadoc.performance.PerformanceSeverity
import com.valtecna.iadoc.settings.DocProSettingsState
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class PerformanceInlayProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey("com.valtecna.iadoc.performance")
    override val name: String = "Performance Analysis"
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
        if (!settingsState.showPerformanceHints) {
            return null
        }

        return PerformanceCollector(editor, file.language.id)
    }

    private class PerformanceCollector(editor: Editor, private val languageId: String) : FactoryInlayHintsCollector(editor) {

        private val javaAnalyzer = PerformanceJavaHints()
        private val kotlinAnalyzer = PerformanceKotlinHints()

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            val analyzer = getAnalyzer(languageId) ?: return true

            try {
                val issue = analyzer.analyze(element) ?: return true

                val icon = when (issue.severity) {
                    PerformanceSeverity.CRITICAL -> "🔴"
                    PerformanceSeverity.HIGH -> "🟠"
                    PerformanceSeverity.MEDIUM -> "🟡"
                    PerformanceSeverity.LOW -> "🔵"
                }

                val text = "$icon Performance: ${issue.message}"
                val offset = element.textRange.startOffset

                sink.addBlockElement(
                    offset,
                    true,
                    true,
                    3,
                    factory.text(text)
                )
            } catch (e: Exception) {
            }

            return true
        }

        private fun getAnalyzer(languageId: String): PerformanceHints? {
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
