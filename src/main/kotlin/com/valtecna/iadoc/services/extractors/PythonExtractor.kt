package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.valtecna.iadoc.services.*

/**
 * Python-specific code element extractor.
 * Note: Requires PyCharm or Python plugin to be installed.
 */
class PythonExtractor : LanguageExtractor {

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val offset = editor.caretModel.offset

        val element = psiFile.findElementAt(offset) ?: return null

        // Use reflection to support Python PSI elements when plugin is available
        return try {
            val result = extractPythonElement(element)
            if (result == null) {
                // Fallback: extract based on text patterns
                extractByTextPattern(element, psiFile)
            } else {
                result
            }
        } catch (e: Exception) {
            println("PythonExtractor: Exception during extraction: ${e.message}")
            e.printStackTrace()
            // Try text-based extraction as fallback
            extractByTextPattern(element, psiFile)
        }
    }

    override fun supports(element: PsiElement): Boolean {
        val file = element.containingFile
        return file?.name?.endsWith(".py") == true ||
               file?.fileType?.name == "Python" ||
               file?.language?.id == "Python"
    }

    private fun extractPythonElement(element: PsiElement): CodeElementInfo? {
        var current: PsiElement? = element

        println("PythonExtractor: Starting extraction from element: ${element.javaClass.simpleName}")

        while (current != null) {
            val className = current.javaClass.simpleName
            val fullClassName = current.javaClass.name

            println("PythonExtractor: Checking element - simpleName=$className, fullName=$fullClassName")

            when {
                className.contains("PyFunction") || fullClassName.contains("PyFunction") -> {
                    println("PythonExtractor: Found PyFunction!")
                    return extractFunction(current)
                }
                className.contains("PyClass") || fullClassName.contains("PyClass") -> {
                    println("PythonExtractor: Found PyClass!")
                    return extractClass(current)
                }
                className.contains("PyTargetExpression") || fullClassName.contains("PyTargetExpression") -> {
                    println("PythonExtractor: Found PyTargetExpression!")
                    return extractVariable(current)
                }
            }
            current = current.parent
        }
        println("PythonExtractor: No Python element found")
        return null
    }

    private fun extractFunction(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "unknown"
        val text = element.text

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = text.lines().firstOrNull() ?: "",
            body = text,
            parameters = emptyList(), // Could be enhanced with reflection
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = "Python"
        )
    }

    private fun extractClass(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "unknown"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.CLASS,
            signature = "class $name:",
            body = element.text,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = "Python"
        )
    }

    private fun extractVariable(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "unknown"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.VARIABLE,
            signature = element.text,
            body = null,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = "Python"
        )
    }

    private fun getElementName(element: PsiElement): String? {
        return try {
            val nameMethod = element.javaClass.getMethod("getName")
            nameMethod.invoke(element) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fallback extraction based on text patterns when PSI parsing fails.
     * This works even without the Python plugin.
     */
    private fun extractByTextPattern(element: PsiElement, psiFile: PsiElement): CodeElementInfo? {
        println("PythonExtractor: Using text-based fallback extraction")

        // Get the line where cursor is positioned
        val text = psiFile.text
        val offset = element.textRange.startOffset

        // Find the start of the current line
        var lineStart = offset
        while (lineStart > 0 && text[lineStart - 1] != '\n') {
            lineStart--
        }

        // Find the end of the function/class definition
        var lineEnd = offset
        while (lineEnd < text.length && text[lineEnd] != '\n') {
            lineEnd++
        }

        // Get the current line
        val currentLine = text.substring(lineStart, lineEnd).trim()

        println("PythonExtractor: Current line: $currentLine")

        // Try to match function definition
        val functionPattern = Regex("""def\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\((.*?)\)""")
        val functionMatch = functionPattern.find(currentLine)

        if (functionMatch != null) {
            val functionName = functionMatch.groupValues[1]
            val params = functionMatch.groupValues[2]

            // Extract the full function body
            val functionBody = extractFunctionBody(text, lineStart)

            println("PythonExtractor: Found function: $functionName")

            return CodeElementInfo(
                name = functionName,
                type = CodeElementType.FUNCTION,
                signature = currentLine,
                body = functionBody,
                parameters = emptyList(),
                returnType = null,
                modifiers = emptyList(),
                documentation = null,
                language = "Python"
            )
        }

        // Try to match class definition
        val classPattern = Regex("""class\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*[:(]""")
        val classMatch = classPattern.find(currentLine)

        if (classMatch != null) {
            val className = classMatch.groupValues[1]
            println("PythonExtractor: Found class: $className")

            return CodeElementInfo(
                name = className,
                type = CodeElementType.CLASS,
                signature = currentLine,
                body = extractClassBody(text, lineStart),
                parameters = emptyList(),
                returnType = null,
                modifiers = emptyList(),
                documentation = null,
                language = "Python"
            )
        }

        println("PythonExtractor: No function or class found in current line")
        return null
    }

    private fun extractFunctionBody(text: String, startOffset: Int): String {
        // Simple extraction: get until next unindented line or end of file
        val lines = text.substring(startOffset).lines()
        val result = mutableListOf<String>()
        var baseIndent: Int? = null

        for (line in lines) {
            if (line.isBlank()) continue

            val indent = line.takeWhile { it.isWhitespace() }.length

            if (baseIndent == null && line.trim().startsWith("def ")) {
                baseIndent = indent
                result.add(line)
            } else if (baseIndent != null) {
                if (indent <= baseIndent && line.trim().isNotEmpty() && !line.trim().startsWith("#")) {
                    // End of function
                    break
                }
                result.add(line)
            }
        }

        return result.joinToString("\n")
    }

    private fun extractClassBody(text: String, startOffset: Int): String {
        // Similar to function body extraction but for classes
        val lines = text.substring(startOffset).lines()
        val result = mutableListOf<String>()
        var baseIndent: Int? = null

        for (line in lines) {
            if (line.isBlank()) continue

            val indent = line.takeWhile { it.isWhitespace() }.length

            if (baseIndent == null && line.trim().startsWith("class ")) {
                baseIndent = indent
                result.add(line)
            } else if (baseIndent != null) {
                if (indent <= baseIndent && line.trim().isNotEmpty() && !line.trim().startsWith("#")) {
                    break
                }
                result.add(line)
            }
        }

        return result.joinToString("\n").take(500) // Limit to 500 chars for preview
    }
}
