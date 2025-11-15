package com.valtecna.iadoc.services

/**
 * Builds context strings for any programming language.
 * Replaces the Java-specific ContextBuilder.
 */
object UniversalContextBuilder {

    fun buildContext(info: CodeElementInfo): String {
        return when (info.type) {
            CodeElementType.FUNCTION,
            CodeElementType.METHOD -> buildFunctionContext(info)

            CodeElementType.CLASS,
            CodeElementType.STRUCT,
            CodeElementType.INTERFACE,
            CodeElementType.TRAIT,
            CodeElementType.IMPL -> buildTypeContext(info)

            CodeElementType.FIELD,
            CodeElementType.PROPERTY,
            CodeElementType.VARIABLE,
            CodeElementType.CONSTANT -> buildVariableContext(info)

            CodeElementType.ENUM -> buildEnumContext(info)
            CodeElementType.TYPE_ALIAS -> buildTypeAliasContext(info)
        }
    }

    private fun buildFunctionContext(info: CodeElementInfo): String {
        val parts = mutableListOf<String>()

        parts.add("Language: ${info.language}")
        parts.add("Type: ${info.type.name.lowercase().replace('_', ' ')}")
        parts.add("Name: ${info.name}")

        // Include containing class information if available
        if (info.containerClass != null) {
            parts.add("Container ${info.containerType ?: "class"}: ${info.containerClass}")
        }

        if (info.modifiers.isNotEmpty()) {
            parts.add("Modifiers: ${info.modifiers.joinToString(" ")}")
        }

        parts.add("Signature: ${info.signature}")

        if (info.parameters.isNotEmpty()) {
            val params = info.parameters.joinToString(", ") { p ->
                if (p.defaultValue != null) {
                    "${p.name}: ${p.type} = ${p.defaultValue}"
                } else {
                    "${p.name}: ${p.type}"
                }
            }
            parts.add("Parameters: $params")
        }

        info.returnType?.let {
            parts.add("Return type: $it")
        }

        info.documentation?.let {
            parts.add("Documentation: $it")
        }

        info.body?.let {
            parts.add("Implementation:\n$it")
        }

        return parts.joinToString("\n")
    }

    private fun buildTypeContext(info: CodeElementInfo): String {
        val parts = mutableListOf<String>()

        parts.add("Language: ${info.language}")
        parts.add("Type: ${info.type.name.lowercase()}")
        parts.add("Name: ${info.name}")

        if (info.modifiers.isNotEmpty()) {
            parts.add("Modifiers: ${info.modifiers.joinToString(" ")}")
        }

        parts.add("Signature: ${info.signature}")

        info.documentation?.let {
            parts.add("Documentation: $it")
        }

        info.body?.let {
            parts.add("Members:\n$it")
        }

        return parts.joinToString("\n")
    }

    private fun buildVariableContext(info: CodeElementInfo): String {
        val parts = mutableListOf<String>()

        parts.add("Language: ${info.language}")
        parts.add("Type: ${info.type.name.lowercase()}")
        parts.add("Name: ${info.name}")

        // Include containing class information if available
        if (info.containerClass != null) {
            parts.add("Container ${info.containerType ?: "class"}: ${info.containerClass}")
        }

        if (info.modifiers.isNotEmpty()) {
            parts.add("Modifiers: ${info.modifiers.joinToString(" ")}")
        }

        info.returnType?.let {
            parts.add("Data type: $it")
        }

        parts.add("Declaration: ${info.signature}")

        info.body?.let {
            parts.add("Initial value: $it")
        }

        info.documentation?.let {
            parts.add("Documentation: $it")
        }

        return parts.joinToString("\n")
    }

    private fun buildEnumContext(info: CodeElementInfo): String {
        val parts = mutableListOf<String>()

        parts.add("Language: ${info.language}")
        parts.add("Type: enum")
        parts.add("Name: ${info.name}")

        if (info.modifiers.isNotEmpty()) {
            parts.add("Modifiers: ${info.modifiers.joinToString(" ")}")
        }

        info.documentation?.let {
            parts.add("Documentation: $it")
        }

        info.body?.let {
            parts.add("Values:\n$it")
        }

        return parts.joinToString("\n")
    }

    private fun buildTypeAliasContext(info: CodeElementInfo): String {
        val parts = mutableListOf<String>()

        parts.add("Language: ${info.language}")
        parts.add("Type: type alias")
        parts.add("Name: ${info.name}")
        parts.add("Definition: ${info.signature}")

        info.documentation?.let {
            parts.add("Documentation: $it")
        }

        return parts.joinToString("\n")
    }
}
