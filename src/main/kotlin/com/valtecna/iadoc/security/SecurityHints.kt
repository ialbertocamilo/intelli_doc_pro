package com.valtecna.iadoc.security

import com.intellij.psi.PsiElement

interface SecurityHints {
    fun analyze(element: PsiElement): SecurityIssue?
}

data class SecurityIssue(
    val type: SecurityType,
    val message: String,
    val severity: Severity
)

enum class SecurityType {
    SQL_INJECTION,
    XSS,
    HARDCODED_CREDENTIALS,
    PATH_TRAVERSAL,
    INSECURE_RANDOM,
    COMMAND_INJECTION,
    XXXX_EXTERNAL_ENTITY
}

enum class Severity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}
