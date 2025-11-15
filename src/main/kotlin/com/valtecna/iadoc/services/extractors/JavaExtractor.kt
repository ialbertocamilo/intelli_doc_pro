package com.valtecna.iadoc.services.extractors

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.*
import com.valtecna.iadoc.services.*

/**
 * Java-specific code element extractor.
 */
class JavaExtractor : LanguageExtractor {

    override fun extract(event: AnActionEvent): CodeElementInfo? {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val offset = editor.caretModel.offset

        val element = psiFile.findElementAt(offset) ?: return null
        val parent = findRelevantParent(element) ?: return null

        return when (parent) {
            is PsiMethod -> extractMethod(parent)
            is PsiField -> extractField(parent)
            is PsiClass -> extractClass(parent)
            else -> null
        }
    }

    override fun supports(element: PsiElement): Boolean {
        return element is PsiJavaFile ||
               element.containingFile is PsiJavaFile
    }

    private fun findRelevantParent(element: PsiElement): PsiElement? {
        var current: PsiElement? = element
        while (current != null) {
            if (current is PsiMethod || current is PsiField || current is PsiClass) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun extractMethod(method: PsiMethod): CodeElementInfo {
        val params = method.parameterList.parameters.map { param ->
            Parameter(
                name = param.name,
                type = param.type.presentableText,
                defaultValue = null
            )
        }

        // Get containing class information
        val containingClass = method.containingClass
        val containerClass = containingClass?.qualifiedName ?: containingClass?.name
        val containerType = when {
            containingClass == null -> null
            containingClass.isInterface -> "interface"
            containingClass.isEnum -> "enum"
            containingClass.isAnnotationType -> "annotation"
            else -> "class"
        }

        return CodeElementInfo(
            name = method.name,
            type = CodeElementType.METHOD,
            signature = buildMethodSignature(method),
            body = method.body?.text,
            parameters = params,
            returnType = method.returnType?.presentableText,
            modifiers = method.modifierList.text.split(" ").filter { it.isNotBlank() },
            documentation = method.docComment?.text,
            language = "Java",
            containerClass = containerClass,
            containerType = containerType
        )
    }

    private fun extractField(field: PsiField): CodeElementInfo {
        // Get containing class information
        val containingClass = field.containingClass
        val containerClass = containingClass?.qualifiedName ?: containingClass?.name
        val containerType = when {
            containingClass == null -> null
            containingClass.isInterface -> "interface"
            containingClass.isEnum -> "enum"
            containingClass.isAnnotationType -> "annotation"
            else -> "class"
        }

        return CodeElementInfo(
            name = field.name,
            type = CodeElementType.FIELD,
            signature = "${field.modifierList?.text ?: ""} ${field.type.presentableText} ${field.name}",
            body = field.initializer?.text,
            parameters = emptyList(),
            returnType = field.type.presentableText,
            modifiers = field.modifierList?.text?.split(" ")?.filter { it.isNotBlank() } ?: emptyList(),
            documentation = field.docComment?.text,
            language = "Java",
            containerClass = containerClass,
            containerType = containerType
        )
    }

    private fun extractClass(clazz: PsiClass): CodeElementInfo {
        val methods = clazz.methods.joinToString("\n") { method ->
            "  ${method.modifierList.text} ${method.returnType?.presentableText ?: "void"} ${method.name}(...)"
        }

        val fields = clazz.fields.joinToString("\n") { field ->
            "  ${field.modifierList?.text ?: ""} ${field.type.presentableText} ${field.name}"
        }

        return CodeElementInfo(
            name = clazz.name ?: "Anonymous",
            type = when {
                clazz.isInterface -> CodeElementType.INTERFACE
                clazz.isEnum -> CodeElementType.ENUM
                else -> CodeElementType.CLASS
            },
            signature = buildClassSignature(clazz),
            body = "Fields:\n$fields\n\nMethods:\n$methods",
            parameters = emptyList(),
            returnType = null,
            modifiers = clazz.modifierList?.text?.split(" ")?.filter { it.isNotBlank() } ?: emptyList(),
            documentation = clazz.docComment?.text,
            language = "Java"
        )
    }

    private fun buildMethodSignature(method: PsiMethod): String {
        val params = method.parameterList.parameters.joinToString(", ") {
            "${it.type.presentableText} ${it.name}"
        }
        return "${method.modifierList.text} ${method.returnType?.presentableText ?: "void"} ${method.name}($params)"
    }

    private fun buildClassSignature(clazz: PsiClass): String {
        val keyword = when {
            clazz.isInterface -> "interface"
            clazz.isEnum -> "enum"
            else -> "class"
        }
        return "${clazz.modifierList?.text ?: ""} $keyword ${clazz.name}"
    }
}
