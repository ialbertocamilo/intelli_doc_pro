package com.valtecna.iadoc.services

object ContextBuilder {
    fun buildForMethod(info: MethodInfo): String {
        val params = if (info.parameters.isEmpty()) "" else info.parameters.joinToString("\n") { "  - ${it.second}: ${it.first}" }
        val related = if (info.related.isEmpty()) "" else info.related.joinToString("\n") { "  - $it" }
        val imports = if (info.imports.isEmpty()) "" else info.imports.joinToString(", ")
        return buildString {
            appendLine("METHOD_NAME: ${info.name}")
            appendLine("RETURNS: ${info.returnType}")
            appendLine("PARAMETERS:")
            if (params.isNotEmpty()) appendLine(params) else appendLine("  - none")
            appendLine("BODY:")
            appendLine(info.body)
            appendLine("EXISTING_DOC:")
            appendLine(info.doc)
            appendLine("RELATED:")
            if (related.isNotEmpty()) appendLine(related) else appendLine("  - none")
            appendLine("IMPORTS: $imports")
            appendLine("GOAL:")
            appendLine("Explain in detail what this code does.")
        }
    }

    fun buildForClass(info: ClassInfo): String {
        val related = if (info.related.isEmpty()) "" else info.related.joinToString("\n") { "  - $it" }
        val imports = if (info.imports.isEmpty()) "" else info.imports.joinToString(", ")
        return buildString {
            appendLine("CLASS_NAME: ${info.name}")
            appendLine("METHODS: ${info.methods}")
            appendLine("FIELDS: ${info.fields}")
            appendLine("EXISTING_DOC:")
            appendLine(info.doc)
            appendLine("RELATED:")
            if (related.isNotEmpty()) appendLine(related) else appendLine("  - none")
            appendLine("IMPORTS: $imports")
            appendLine("GOAL:")
            appendLine("Explain in detail what this class does.")
        }
    }

    fun buildForField(info: FieldInfo): String {
        val related = if (info.related.isEmpty()) "" else info.related.joinToString("\n") { "  - $it" }
        val imports = if (info.imports.isEmpty()) "" else info.imports.joinToString(", ")
        return buildString {
            appendLine("FIELD_NAME: ${info.name}")
            appendLine("TYPE: ${info.type}")
            appendLine("MODIFIERS: ${info.modifiers}")
            appendLine("CLASS: ${info.containingClass}")
            appendLine("INITIALIZER:")
            appendLine(info.initializer ?: "")
            appendLine("EXISTING_DOC:")
            appendLine(info.doc)
            appendLine("RELATED:")
            if (related.isNotEmpty()) appendLine(related) else appendLine("  - none")
            appendLine("IMPORTS: $imports")
            appendLine("GOAL:")
            appendLine("Explain in detail what this field represents and how it is used.")
        }
    }
}
