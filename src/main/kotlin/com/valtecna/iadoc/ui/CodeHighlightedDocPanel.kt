package com.valtecna.iadoc.ui

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * A panel that displays HTML documentation with syntax-highlighted code blocks.
 * Parses <pre> tags and replaces them with IntelliJ EditorEx components for true syntax highlighting.
 */
class CodeHighlightedDocPanel(
    private val htmlContent: String,
    private val language: String,
    private val project: Project?
) : JPanel(BorderLayout()) {

    private val createdEditors = mutableListOf<EditorEx>()

    init {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        mainPanel.border = JBUI.Borders.empty(12)

        // Parse HTML and replace <pre> tags with syntax-highlighted editors
        val doc = Jsoup.parse(htmlContent)

        for (element in doc.body().children()) {
            when (element.tagName()) {
                "pre" -> {
                    val code = element.text()
                    val editor = createHighlightedEditor(code, language)
                    if (editor != null) {
                        val editorPanel = JPanel(BorderLayout())
                        editorPanel.add(editor.component, BorderLayout.CENTER)
                        editorPanel.maximumSize = Dimension(Int.MAX_VALUE, editor.component.preferredSize.height)
                        mainPanel.add(editorPanel)
                        mainPanel.add(Box.createVerticalStrut(8))
                    } else {
                        // Fallback to JTextArea if EditorEx creation fails
                        val textArea = JTextArea(code).apply {
                            isEditable = false
                            font = font.deriveFont(11f)
                            lineWrap = true
                            wrapStyleWord = true
                        }
                        val scrollPane = JScrollPane(textArea).apply {
                            maximumSize = Dimension(Int.MAX_VALUE, 150)
                        }
                        mainPanel.add(scrollPane)
                        mainPanel.add(Box.createVerticalStrut(8))
                    }
                }
                else -> {
                    // For non-code elements, use JEditorPane with HTML for better rendering
                    val htmlPane = JEditorPane("text/html", "<html><body style='font-family: sans-serif; font-size: 11px;'>${element.outerHtml()}</body></html>").apply {
                        isEditable = false
                        background = null
                        border = null
                        // Make links clickable if any
                        addHyperlinkListener { e ->
                            if (e.eventType == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                                java.awt.Desktop.getDesktop().browse(e.url.toURI())
                            }
                        }
                    }
                    htmlPane.maximumSize = Dimension(Int.MAX_VALUE, htmlPane.preferredSize.height)
                    mainPanel.add(htmlPane)
                    mainPanel.add(Box.createVerticalStrut(4))
                }
            }
        }

        val scrollPane = JBScrollPane(mainPanel).apply {
            preferredSize = Dimension(600, 400)
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }

        add(scrollPane, BorderLayout.CENTER)

        // Add resize listener to update editor widths
        addComponentListener(object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent?) {
                updateEditorWidths()
            }
        })
    }

    /**
     * Updates all embedded editors to match the current panel width
     */
    private fun updateEditorWidths() {
        val availableWidth = width - 40 // Account for padding and scrollbar
        for (editor in createdEditors) {
            val lineCount = editor.document.lineCount
            val lineHeight = editor.lineHeight
            val preferredHeight = (lineCount * lineHeight) + 10
            editor.component.preferredSize = Dimension(availableWidth, preferredHeight)
            editor.component.revalidate()
        }
    }

    /**
     * Creates a read-only editor with syntax highlighting for a code snippet.
     */
    private fun createHighlightedEditor(code: String, language: String): EditorEx? {
        if (project == null) return null

        val fileType = getFileTypeForLanguage(language) ?: return null
        val editorFactory = EditorFactory.getInstance()

        val document = editorFactory.createDocument(code)
        val editor = editorFactory.createEditor(document, project) as? EditorEx ?: return null

        createdEditors.add(editor)

        editor.settings.apply {
            isLineNumbersShown = false
            isLineMarkerAreaShown = false
            isFoldingOutlineShown = false
            isRightMarginShown = false
            isCaretRowShown = false
            additionalLinesCount = 0
            additionalColumnsCount = 0
            isAdditionalPageAtBottom = false
            isUseSoftWraps = true  // Enable soft wrap for long lines
        }

        editor.isViewer = true

        // Apply syntax highlighting matching the current editor scheme
        val highlighter = EditorHighlighterFactory.getInstance()
            .createEditorHighlighter(project, fileType)
        editor.highlighter = highlighter

        // Set component size based on content
        val component = editor.component
        val lineCount = document.lineCount
        val lineHeight = editor.lineHeight
        val preferredHeight = (lineCount * lineHeight) + 10
        val initialWidth = if (width > 0) width - 40 else 560  // Use current width or default
        component.preferredSize = Dimension(initialWidth, preferredHeight)
        component.minimumSize = Dimension(300, preferredHeight)

        return editor
    }

    /**
     * Maps language names to IntelliJ FileTypes for syntax highlighting.
     */
    private fun getFileTypeForLanguage(language: String): FileType? {
        val fileTypeManager = FileTypeManager.getInstance()
        return when (language.lowercase()) {
            "java" -> fileTypeManager.getFileTypeByExtension("java")
            "kotlin" -> fileTypeManager.getFileTypeByExtension("kt")
            "python" -> fileTypeManager.getFileTypeByExtension("py")
            "javascript", "js" -> fileTypeManager.getFileTypeByExtension("js")
            "typescript", "ts" -> fileTypeManager.getFileTypeByExtension("ts")
            "rust", "rs" -> fileTypeManager.getFileTypeByExtension("rs")
            else -> fileTypeManager.getFileTypeByExtension(language)
        }
    }

    /**
     * Cleanup method to release editor resources.
     * Call this when the panel is no longer needed.
     */
    fun dispose() {
        val editorFactory = EditorFactory.getInstance()
        for (editor in createdEditors) {
            editorFactory.releaseEditor(editor)
        }
        createdEditors.clear()
    }
}
