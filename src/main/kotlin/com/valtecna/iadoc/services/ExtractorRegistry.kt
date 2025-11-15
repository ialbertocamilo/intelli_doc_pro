package com.valtecna.iadoc.services

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.valtecna.iadoc.services.extractors.*

/**
 * Registry that manages all language-specific extractors.
 * Uses a chain of responsibility pattern to find the right extractor.
 */
object ExtractorRegistry {

    private val extractors: List<LanguageExtractor> = listOf(
        JavaExtractor(),
        KotlinExtractor(),
        PythonExtractor(),
        TypeScriptExtractor(),
        RustExtractor(),
        PHPExtractor(),
        CppExtractor()
    )

    /**
     * Extracts code element information using the appropriate language extractor.
     * @param event The action event containing editor and PSI context
     * @return CodeElementInfo if an element was found, null otherwise
     */
    fun extract(event: AnActionEvent): CodeElementInfo? {
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return null
        val element = psiFile.findElementAt(
            event.getData(CommonDataKeys.EDITOR)?.caretModel?.offset ?: 0
        ) ?: return null

        println("ExtractorRegistry: File language=${psiFile.language.id}, fileName=${psiFile.name}")
        println("ExtractorRegistry: Element=${element.javaClass.simpleName}")

        // Find the first extractor that supports this element
        for (extractor in extractors) {
            val supports = extractor.supports(element)
            println("ExtractorRegistry: ${extractor.javaClass.simpleName} supports=$supports")
            if (supports) {
                val result = extractor.extract(event)
                println("ExtractorRegistry: Extracted info: $result")
                return result
            }
        }

        println("ExtractorRegistry: No extractor found for this element")
        return null
    }

    /**
     * Registers a custom language extractor at runtime.
     * Useful for plugins that want to add support for additional languages.
     */
    fun registerExtractor(extractor: LanguageExtractor) {
        (extractors as? MutableList)?.add(extractor)
    }
}
