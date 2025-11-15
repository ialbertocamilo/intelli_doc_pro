package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.valtecna.iadoc.services.*

/**
 * TypeScript/JavaScript-specific code element extractor.
 * Note: Requires JavaScript/TypeScript plugin (bundled with WebStorm/IntelliJ IDEA Ultimate).
 */
class TypeScriptExtractor : LanguageExtractor {

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val offset = editor.caretModel.offset

        val element = psiFile.findElementAt(offset) ?: return null

        return try {
            extractJSElement(element)
        } catch (e: Exception) {
            null
        }
    }

    override fun supports(element: PsiElement): Boolean {
        val fileName = element.containingFile?.name ?: return false
        return fileName.endsWith(".ts") ||
               fileName.endsWith(".tsx") ||
               fileName.endsWith(".js") ||
               fileName.endsWith(".jsx")
    }

    private fun extractJSElement(element: PsiElement): CodeElementInfo? {
        var current: PsiElement? = element

        while (current != null) {
            val className = current.javaClass.simpleName

            when {
                className.contains("JSFunction") ||
                className.contains("TypeScriptFunction") -> return extractFunction(current)

                className.contains("JSClass") ||
                className.contains("TypeScriptClass") -> return extractClass(current)

                className.contains("JSVariable") ||
                className.contains("TypeScriptVariable") -> return extractVariable(current)

                className.contains("JSProperty") -> return extractProperty(current)
            }
            current = current.parent
        }
        return null
    }

    private fun extractFunction(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "anonymous"
        val text = element.text
        val language = if (element.containingFile.name.endsWith(".ts") ||
                          element.containingFile.name.endsWith(".tsx"))
                       "TypeScript" else "JavaScript"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = text.lines().firstOrNull() ?: "",
            body = text,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = language
        )
    }

    private fun extractClass(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "AnonymousClass"
        val language = if (element.containingFile.name.endsWith(".ts") ||
                          element.containingFile.name.endsWith(".tsx"))
                       "TypeScript" else "JavaScript"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.CLASS,
            signature = "class $name",
            body = element.text,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = language
        )
    }

    private fun extractVariable(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "unknown"
        val language = if (element.containingFile.name.endsWith(".ts") ||
                          element.containingFile.name.endsWith(".tsx"))
                       "TypeScript" else "JavaScript"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.VARIABLE,
            signature = element.text,
            body = null,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = language
        )
    }

    private fun extractProperty(element: PsiElement): CodeElementInfo {
        val name = getElementName(element) ?: "unknown"
        val language = if (element.containingFile.name.endsWith(".ts") ||
                          element.containingFile.name.endsWith(".tsx"))
                       "TypeScript" else "JavaScript"

        return CodeElementInfo(
            name = name,
            type = CodeElementType.PROPERTY,
            signature = element.text,
            body = null,
            parameters = emptyList(),
            returnType = null,
            modifiers = emptyList(),
            documentation = null,
            language = language
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
}
