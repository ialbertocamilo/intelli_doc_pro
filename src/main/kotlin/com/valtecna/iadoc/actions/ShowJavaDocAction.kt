package com.valtecna.iadoc.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import com.intellij.openapi.components.service
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.llm.Provider
import com.valtecna.iadoc.llm.OpenAILLMService
import com.valtecna.iadoc.llm.GroqLLMService
import com.valtecna.iadoc.llm.BedrockLLMService
import com.valtecna.iadoc.services.HTMLGenerator
import com.valtecna.iadoc.services.ExtractorRegistry
import com.valtecna.iadoc.services.UniversalContextBuilder
import com.valtecna.iadoc.settings.DocProSettingsState
import com.valtecna.iadoc.license.LicenseChecker
import com.valtecna.iadoc.ui.CodeHighlightedDocPanel

class ShowJavaDocAction : AnAction(Constants.UI.POPUP_TITLE) {

    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.project ?: return

        // Use the new multi-language extractor
        val info = ExtractorRegistry.extract(e)

        if (info == null) {
            HintManager.getInstance().showInformationHint(
                editor,
                Constants.Messages.NO_ELEMENT_FOUND
            )
            return
        }

        val settings = service<DocProSettingsState>()
        val isPro = LicenseChecker.isPro()

        // Use the universal context builder
        val context = UniversalContextBuilder.buildContext(info)

        val llm = when (settings.provider) {
            Provider.OpenAI -> OpenAILLMService(
                apiKey = settings.apiKey,
                model = settings.openaiModel
            )
            Provider.Groq -> GroqLLMService(
                apiKey = settings.apiKey,
                model = settings.groqModel
            )
            Provider.Bedrock -> BedrockLLMService(
                apiKey = settings.apiKey,
                model = settings.bedrockModel,
                region = settings.bedrockRegion
            )
        }

        // Run the LLM call in a background thread to avoid freezing the UI
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generating AI Documentation...", true) {
            var htmlDoc: String? = null
            var error: String? = null

            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Calling IntelliDoc Professional..."
                try {
                    htmlDoc = HTMLGenerator(llm).generate(context, isPro)
                } catch (e: Exception) {
                    error = e.message ?: "Unknown error occurred"
                }
            }

            override fun onSuccess() {
                if (htmlDoc != null) {
                    val docPanel = CodeHighlightedDocPanel(htmlDoc!!, info.language, project)

                    val popup = JBPopupFactory.getInstance()
                        .createComponentPopupBuilder(docPanel, null)
                        .setTitle(Constants.UI.POPUP_TITLE)
                        .setResizable(true)
                        .setMovable(true)
                        .setRequestFocus(true)
                        .createPopup()

                    val visualPosition = editor.caretModel.visualPosition
                    val point = editor.visualPositionToXY(visualPosition)
                    popup.show(RelativePoint(editor.contentComponent, Point(point.x, point.y + editor.lineHeight)))
                } else if (error != null) {
                    ApplicationManager.getApplication().invokeLater {
                        HintManager.getInstance().showErrorHint(editor, "Error: $error")
                    }
                }
            }

            override fun onThrowable(error: Throwable) {
                ApplicationManager.getApplication().invokeLater {
                    HintManager.getInstance().showErrorHint(editor, "Error: ${error.message}")
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}

