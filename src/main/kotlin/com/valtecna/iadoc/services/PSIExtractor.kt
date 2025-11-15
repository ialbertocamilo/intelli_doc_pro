package com.valtecna.iadoc.services

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

data class MethodInfo(
    val name: String,
    val returnType: String,
    val parameters: List<Pair<String, String>>, // type, name
    val body: String,
    val doc: String,
    val related: List<String>,
    val imports: List<String>
)

data class ClassInfo(
    val name: String,
    val methods: Int,
    val fields: Int,
    val doc: String,
    val related: List<String>,
    val imports: List<String>
)

data class FieldInfo(
    val name: String,
    val type: String,
    val modifiers: String,
    val initializer: String?,
    val doc: String,
    val containingClass: String,
    val related: List<String>,
    val imports: List<String>
)

class PSIExtractor {
    fun extract(event: AnActionEvent): Any? {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return null
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset)

        val javaElement = when {
            PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) != null ->
                PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
            PsiTreeUtil.getParentOfType(element, PsiField::class.java) != null ->
                PsiTreeUtil.getParentOfType(element, PsiField::class.java)
            PsiTreeUtil.getParentOfType(element, PsiClass::class.java) != null ->
                PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
            else -> null
        } ?: return null

        val imports = collectImports(psiFile)
        return when (javaElement) {
            is PsiMethod -> toMethodInfo(javaElement, imports)
            is PsiField -> toFieldInfo(javaElement, imports)
            is PsiClass -> toClassInfo(javaElement, imports)
            else -> null
        }
    }

    private fun collectImports(file: PsiFile): List<String> {
        return if (file is PsiJavaFile) {
            file.importList?.allImportStatements?.mapNotNull { it.importReference?.qualifiedName } ?: emptyList()
        } else emptyList()
    }

    private fun toMethodInfo(method: PsiMethod, imports: List<String>): MethodInfo {
        val params = method.parameterList.parameters.map { it.type.presentableText to (it.name ?: "p") }
        val body = method.body?.text ?: ""
        val doc = method.docComment?.text ?: collectInlineComments(method)
        val related = buildSet<String> {
            method.containingClass?.qualifiedName?.let { add(it) }
            method.returnType?.presentableText?.let { add(it) }
            params.forEach { add(it.first) }
        }.toList()
        return MethodInfo(
            name = method.name,
            returnType = method.returnType?.presentableText ?: "void",
            parameters = params,
            body = body,
            doc = doc,
            related = related,
            imports = imports
        )
    }

    private fun toClassInfo(psiClass: PsiClass, imports: List<String>): ClassInfo {
        val doc = psiClass.docComment?.text ?: collectInlineComments(psiClass)
        val related = buildSet<String> {
            psiClass.superClass?.qualifiedName?.let { add(it) }
            psiClass.interfaces.forEach { it.qualifiedName?.let { q -> add(q) } }
        }.toList()
        return ClassInfo(
            name = psiClass.name ?: "",
            methods = psiClass.methods.size,
            fields = psiClass.fields.size,
            doc = doc,
            related = related,
            imports = imports
        )
    }

    private fun toFieldInfo(field: PsiField, imports: List<String>): FieldInfo {
        val doc = field.docComment?.text ?: collectInlineComments(field)
        val related = buildSet<String> {
            field.containingClass?.qualifiedName?.let { add(it) }
            add(field.type.presentableText)
        }.toList()
        return FieldInfo(
            name = field.name,
            type = field.type.presentableText,
            modifiers = field.modifierList?.text ?: "",
            initializer = field.initializer?.text,
            doc = doc,
            containingClass = field.containingClass?.name ?: "",
            related = related,
            imports = imports
        )
    }

    private fun collectInlineComments(owner: PsiElement): String {
        val comments = PsiTreeUtil.collectElementsOfType(owner, PsiComment::class.java)
        return comments.joinToString("\n") { it.text }
    }
}
