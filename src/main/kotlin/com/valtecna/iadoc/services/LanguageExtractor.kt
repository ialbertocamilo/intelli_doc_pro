package com.valtecna.iadoc.services

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement

/**
 * Interface for language-specific code element extractors.
 * Each supported language should implement this interface.
 */
interface LanguageExtractor {
    /**
     * Extracts code element information from the cursor position.
     * @param event The action event containing editor and PSI context
     * @return CodeElementInfo if an element was found, null otherwise
     */
    fun extract(event: AnActionEvent): CodeElementInfo?

    /**
     * Checks if this extractor supports the given PSI element.
     * @param element The PSI element to check
     * @return true if this extractor can handle the element
     */
    fun supports(element: PsiElement): Boolean
}

/**
 * Generic code element information.
 * This replaces the language-specific MethodInfo, FieldInfo, ClassInfo.
 */
data class CodeElementInfo(
    val name: String,
    val type: CodeElementType,
    val signature: String,
    val body: String? = null,
    val parameters: List<Parameter> = emptyList(),
    val returnType: String? = null,
    val modifiers: List<String> = emptyList(),
    val documentation: String? = null,
    val language: String,
    val containerClass: String? = null,  // The class/struct that contains this element (for methods/fields)
    val containerType: String? = null    // Type of container (class/interface/struct/etc)
)

enum class CodeElementType {
    FUNCTION,
    METHOD,
    CLASS,
    STRUCT,
    INTERFACE,
    TRAIT,
    FIELD,
    PROPERTY,
    VARIABLE,
    CONSTANT,
    ENUM,
    TYPE_ALIAS,
    IMPL
}

data class Parameter(
    val name: String,
    val type: String,
    val defaultValue: String? = null
)
