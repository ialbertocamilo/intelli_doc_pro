package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.valtecna.iadoc.services.CodeElementInfo
import com.valtecna.iadoc.services.CodeElementType
import com.valtecna.iadoc.services.LanguageExtractor
import org.jetbrains.kotlin.psi.*

class KotlinExtractor : LanguageExtractor {

    override fun supports(element: PsiElement): Boolean {
        return element.language.id == "kotlin"
    }

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return null

        val function = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
        if (function != null) {
            return extractFunction(function)
        }

        val property = PsiTreeUtil.getParentOfType(element, KtProperty::class.java)
        if (property != null) {
            return extractProperty(property)
        }

        val klass = PsiTreeUtil.getParentOfType(element, KtClass::class.java)
        if (klass != null) {
            return extractClass(klass)
        }

        return null
    }

    private fun extractFunction(function: KtNamedFunction): CodeElementInfo {
        val name = function.name ?: "anonymous"
        val params = function.valueParameters.joinToString(", ") {
            "${it.name}: ${it.typeReference?.text ?: "Any"}"
        }
        val returnType = function.typeReference?.text ?: "Unit"
        val body = function.bodyExpression?.text ?: ""

        // Get containing class information
        val containingClass = PsiTreeUtil.getParentOfType(function, KtClass::class.java)
        val containerClass = containingClass?.fqName?.asString() ?: containingClass?.name
        val containerType = when {
            containingClass == null -> null
            containingClass.isInterface() -> "interface"
            containingClass.isEnum() -> "enum"
            containingClass.isData() -> "data class"
            else -> "class"
        }

        return CodeElementInfo(
            name = name,
            type = CodeElementType.FUNCTION,
            signature = "fun $name($params): $returnType",
            body = body,
            language = "kotlin",
            containerClass = containerClass,
            containerType = containerType
        )
    }

    private fun extractProperty(property: KtProperty): CodeElementInfo {
        val name = property.name ?: "property"
        val type = property.typeReference?.text ?: "Any"
        val initializer = property.initializer?.text ?: ""

        // Get containing class information
        val containingClass = PsiTreeUtil.getParentOfType(property, KtClass::class.java)
        val containerClass = containingClass?.fqName?.asString() ?: containingClass?.name
        val containerType = when {
            containingClass == null -> null
            containingClass.isInterface() -> "interface"
            containingClass.isEnum() -> "enum"
            containingClass.isData() -> "data class"
            else -> "class"
        }

        return CodeElementInfo(
            name = name,
            type = CodeElementType.PROPERTY,
            signature = "val/var $name: $type",
            body = initializer,
            language = "kotlin",
            containerClass = containerClass,
            containerType = containerType
        )
    }

    private fun extractClass(klass: KtClass): CodeElementInfo {
        val name = klass.name ?: "AnonymousClass"
        val modifiers = klass.modifierList?.text ?: ""
        val body = klass.getBody()?.text ?: ""

        return CodeElementInfo(
            name = name,
            type = CodeElementType.CLASS,
            signature = "$modifiers class $name",
            body = body,
            language = "kotlin"
        )
    }
}
