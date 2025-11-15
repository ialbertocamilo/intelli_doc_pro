package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.valtecna.iadoc.services.CodeElementInfo
import com.valtecna.iadoc.services.CodeElementType
import com.valtecna.iadoc.services.LanguageExtractor

class CppExtractor : LanguageExtractor {

    override fun supports(element: PsiElement): Boolean {
        val langId = element.language.id
        return langId.equals("C++", ignoreCase = true) ||
               langId.equals("ObjectiveC", ignoreCase = true) ||
               langId.equals("C", ignoreCase = true)
    }

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return null

        var current: PsiElement? = element
        while (current != null) {
            val elementType = current.node?.elementType?.toString() ?: ""

            when {
                elementType.contains("FUNCTION") || elementType.contains("METHOD") -> {
                    return extractFunction(current)
                }
                elementType.contains("CLASS") || elementType.contains("STRUCT") -> {
                    return extractClass(current)
                }
            }
            current = current.parent
        }

        return null
    }

    private fun extractFunction(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull {
            !it.trim().startsWith("//") && it.contains('(')
        }?.trim() ?: "unknown function"

        val name = extractFunctionName(signature)

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = signature,
            body = text,
            language = "cpp"
        )
    }

    private fun extractClass(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull {
            it.contains("class") || it.contains("struct")
        }?.trim() ?: "class Unknown"

        val name = signature
            .substringAfter("class ")
            .substringAfter("struct ")
            .substringBefore(':')
            .substringBefore('{')
            .trim()

        return CodeElementInfo(
            name = name,
            type = if (signature.contains("struct")) CodeElementType.STRUCT else CodeElementType.CLASS,
            signature = signature,
            body = text,
            language = "cpp"
        )
    }

    private fun extractFunctionName(signature: String): String {
        val beforeParen = signature.substringBefore('(').trim()
        val parts = beforeParen.split(Regex("\\s+"))
        return parts.lastOrNull()?.trim() ?: "unknown"
    }
}
