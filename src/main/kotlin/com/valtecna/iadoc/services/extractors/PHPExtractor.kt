package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.valtecna.iadoc.services.CodeElementInfo
import com.valtecna.iadoc.services.CodeElementType
import com.valtecna.iadoc.services.LanguageExtractor

class PHPExtractor : LanguageExtractor {

    override fun supports(element: PsiElement): Boolean {
        return element.language.id.equals("PHP", ignoreCase = true)
    }

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return null

        var current: PsiElement? = element
        while (current != null) {
            val elementType = current.node?.elementType?.toString() ?: ""

            when {
                elementType.contains("Function") || elementType.contains("METHOD") -> {
                    return extractFunction(current)
                }
                elementType.contains("Class") -> {
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
            it.contains("function")
        }?.trim() ?: "function unknown"

        val name = signature
            .substringAfter("function ")
            .substringBefore('(')
            .trim()

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = signature,
            body = text,
            language = "php"
        )
    }

    private fun extractClass(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull {
            it.contains("class")
        }?.trim() ?: "class Unknown"

        val name = signature
            .substringAfter("class ")
            .substringBefore('{')
            .substringBefore("extends")
            .substringBefore("implements")
            .trim()

        return CodeElementInfo(
            name = name,
            type = CodeElementType.CLASS,
            signature = signature,
            body = text,
            language = "php"
        )
    }
}
