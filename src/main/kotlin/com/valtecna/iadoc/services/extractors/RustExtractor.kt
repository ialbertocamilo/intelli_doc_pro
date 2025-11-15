package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.valtecna.iadoc.services.CodeElementInfo
import com.valtecna.iadoc.services.CodeElementType
import com.valtecna.iadoc.services.LanguageExtractor

class RustExtractor : LanguageExtractor {

    override fun supports(element: PsiElement): Boolean {
        return element.language.id.equals("Rust", ignoreCase = true)
    }

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return null

        var current: PsiElement? = element
        while (current != null) {
            val elementType = current.node?.elementType?.toString() ?: ""

            when {
                elementType.contains("FUNCTION") -> {
                    return extractFunction(current)
                }
                elementType.contains("STRUCT") -> {
                    return extractStruct(current)
                }
                elementType.contains("IMPL") -> {
                    return extractImpl(current)
                }
            }
            current = current.parent
        }

        return null
    }

    private fun extractFunction(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull()?.trim() ?: "fn unknown"
        val name = extractName(signature, "fn ")

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = signature,
            body = text,
            language = "rust"
        )
    }

    private fun extractStruct(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull()?.trim() ?: "struct Unknown"
        val name = extractName(signature, "struct ")

        return CodeElementInfo(
            name = name,
            type = CodeElementType.STRUCT,
            signature = signature,
            body = text,
            language = "rust"
        )
    }

    private fun extractImpl(element: PsiElement): CodeElementInfo {
        val text = element.text
        val lines = text.lines()
        val signature = lines.firstOrNull()?.trim() ?: "impl Unknown"
        val name = extractName(signature, "impl ")

        return CodeElementInfo(
            name = name,
            type = CodeElementType.IMPL,
            signature = signature,
            body = text,
            language = "rust"
        )
    }

    private fun extractName(signature: String, prefix: String): String {
        return signature
            .substringAfter(prefix)
            .substringBefore('(')
            .substringBefore('<')
            .substringBefore('{')
            .trim()
    }
}
