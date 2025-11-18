package com.valtecna.iadoc.inlays

import com.intellij.codeInsight.hints.*
import com.intellij.lang.Language
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNamedElement
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.analysis.ComplexityAnalyzer
import com.valtecna.iadoc.analysis.ComplexityResult
import com.valtecna.iadoc.analysis.UniversalComplexityAnalyzer
import com.valtecna.iadoc.settings.DocProSettingsState
import javax.swing.JPanel

@Suppress("UnstableApiUsage")
class ComplexityInlayProvider : InlayHintsProvider<NoSettings> {

    override val key: SettingsKey<NoSettings> = SettingsKey(Constants.Inlay.KEY_ID)
    override val name: String = Constants.Inlay.NAME
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
        // Check if complexity hints are enabled in settings
        val settingsState = service<DocProSettingsState>()
        if (!settingsState.showComplexityHints) {
            return null  // Don't show hints if disabled
        }

        return ComplexityCollector(editor, file.language.id)
    }

    private class ComplexityCollector(editor: Editor, private val languageId: String) : FactoryInlayHintsCollector(editor) {

        private val javaAnalyzer = ComplexityAnalyzer()
        private val universalAnalyzer = UniversalComplexityAnalyzer()

        override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
            // For Java - use the specialized Java analyzer
            if (element is PsiMethod) {
                try {
                    val result = javaAnalyzer.analyze(element)
                    val reason = getComplexityReason(result)
                    val text = "⚙️ Complexity: ${result.time} - $reason"
                    val offset = element.textRange.startOffset

                    sink.addBlockElement(
                        offset,
                        true,
                        true,
                        1,
                        factory.text(text)
                    )
                } catch (e: Exception) {
                    // Silently ignore errors
                }
                return true
            }

            // For all other languages (Kotlin, Python, TypeScript, JavaScript, Rust, PHP, C++, C)
            // Use generic text-based analysis
            if (isFunctionLikeElement(element)) {
                try {
                    val functionName = getFunctionName(element)
                    val functionText = element.text

                    val result = universalAnalyzer.analyze(functionText, functionName)

                    val reason = try {
                        getComplexityReason(result)
                    } catch (e: Exception) {
                        "detected"
                    }

                    val text = "⚙️ Complexity: ${result.time} - $reason"
                    val offset = element.textRange.startOffset

                    sink.addBlockElement(
                        offset,
                        true,
                        true,
                        1,
                        factory.text(text)
                    )
                } catch (e: Exception) {
                    try {
                        val offset = element.textRange.startOffset
                        sink.addBlockElement(
                            offset,
                            true,
                            true,
                            1,
                            factory.text("⚙️ Complexity: O(?)")
                        )
                    } catch (ignored: Exception) {
                        // Really ignore this one
                    }
                }
                return true
            }

            return true
        }

        private fun getComplexityReason(result: ComplexityResult): String {
            // Extract the most relevant detail from the complexity analysis
            val details = result.details

            return when (result.time) {
                "O(1)" -> "constant time"
                "O(log n)" -> "logarithmic pattern detected"
                "O(n)" -> {
                    val loops = details.find { it.startsWith("Loops:") }?.substringAfter(": ")?.toIntOrNull() ?: 0
                    val recursion = details.find { it.startsWith("Recursion:") }?.substringAfter(": ")?.toIntOrNull() ?: 0
                    when {
                        loops > 0 -> "single loop"
                        recursion > 0 -> "recursive call"
                        else -> "linear operation"
                    }
                }
                "O(n log n)" -> "sorting algorithm"
                "O(n²)" -> "nested loops (depth 2)"
                "O(n³)" -> "nested loops (depth 3+)"
                "O(2ⁿ)" -> "double recursion"
                "O(n!)" -> "factorial/permutation pattern"
                else -> "detected pattern"
            }
        }

        private fun isFunctionLikeElement(element: PsiElement): Boolean {
            val className = element.javaClass.name
            val elementType = element.toString()
            val simpleName = element.javaClass.simpleName

            // Debug: Print what we're seeing (will help diagnose Python issues)
            if (languageId == "Python") {
                println("Checking Python element: className=$className, type=$elementType, simpleName=$simpleName")
            }

            // Kotlin: KtNamedFunction and KtLambdaExpression
            if (className.contains("KtNamedFunction") ||
                simpleName == "KtNamedFunction" ||
                className.contains("KtLambdaExpression") ||
                simpleName == "KtLambdaExpression") return true

            // Python: ONLY PyFunction (not PyClass)
            if ((className.endsWith("PyFunction") ||
                simpleName == "PyFunction" ||
                className.contains(".psi.PyFunction") ||
                elementType.contains("PyFunction")) &&
                !className.contains("PyClass") &&
                !simpleName.contains("Class")) {
                return true
            }

            // JavaScript/TypeScript: ONLY functions (not classes/variables)
            if ((className.contains("JSFunction") ||
                className.contains("ES6Function") ||
                className.contains("TypeScriptFunction") ||
                elementType.contains("JSFunction") ||
                elementType.contains("ES6FunctionDeclaration") ||
                elementType.contains("TypeScriptFunction") ||
                className.contains("ArrowFunction") ||
                elementType.contains("ArrowFunction")) &&
                !className.contains("Class") &&
                !simpleName.contains("Class")) return true

            // Rust: ONLY RsFunction (not structs, traits, etc.)
            if ((className.contains("RsFunction") ||
                simpleName == "RsFunction" ||
                elementType.contains("RsFunction")) &&
                !className.contains("Struct") &&
                !className.contains("Trait") &&
                !simpleName.contains("Struct")) return true

            // PHP: ONLY functions/methods (not classes/variables)
            if ((className.contains("PhpFunction") ||
                className.endsWith("Method") ||
                elementType.contains("PhpFunction") ||
                (className.contains("Method") && !className.contains("MethodCall"))) &&
                !className.contains("PhpClass") &&
                !simpleName.contains("Class") &&
                !simpleName.contains("Variable")) return true

            // C/C++: ONLY function declarations (not classes/structs)
            if ((className.contains("FunctionDecl") ||
                className.contains("CppFunction") ||
                elementType.contains("FunctionDecl") ||
                elementType.contains("CppFunction")) &&
                !className.contains("Class") &&
                !className.contains("Struct")) return true

            return false
        }

        private fun getFunctionName(element: PsiElement): String {
            return when (element) {
                is PsiNamedElement -> element.name ?: ""
                else -> {
                    val text = element.text.take(100)  // First 100 chars
                    val namePattern = Regex("(def|function|fn|func|fun)\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
                    namePattern.find(text)?.groupValues?.getOrNull(2) ?: ""
                }
            }
        }
    }

    override fun isLanguageSupported(language: Language): Boolean {
        return when (language.id) {
            Constants.Language.JAVA -> true
            Constants.Language.KOTLIN -> true
            Constants.Language.PYTHON -> true
            Constants.Language.TYPESCRIPT -> true
            Constants.Language.JAVASCRIPT -> true
            Constants.Language.RUST -> true
            Constants.Language.PHP -> true
            Constants.Language.CPP -> true
            Constants.Language.C -> true
            else -> false
        }
    }
}
