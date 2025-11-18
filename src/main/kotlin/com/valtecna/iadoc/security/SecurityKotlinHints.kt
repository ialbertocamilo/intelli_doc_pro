package com.valtecna.iadoc.security

import com.intellij.psi.PsiElement

class SecurityKotlinHints : SecurityHints {

    override fun analyze(element: PsiElement): SecurityIssue? {
        val className = element.javaClass.name
        val text = element.text

        if (!isFunctionOrProperty(className)) {
            return null
        }

        if (text.length > MAX_ELEMENT_SIZE) {
            return null
        }

        val textLower = text.lowercase()

        return checkSQLInjection(textLower)
            ?: checkHardcodedCredentials(element, textLower)
            ?: checkPathTraversal(textLower)
            ?: checkInsecureRandom(textLower)
            ?: checkCommandInjection(textLower)
            ?: checkXSS(textLower)
    }

    companion object {
        private const val MAX_ELEMENT_SIZE = 10000
    }

    private fun isFunctionOrProperty(className: String): Boolean {
        return className.contains("KtNamedFunction") ||
               className.contains("KtProperty") ||
               className.contains("KtParameter")
    }

    private fun checkSQLInjection(text: String): SecurityIssue? {
        if (!containsSQLKeywords(text)) return null

        val hasStringConcatenation = text.contains("+") || text.contains("\${")
        val hasStringTemplate = text.contains("\"\"\"") && text.contains("\${")

        if (hasStringConcatenation || hasStringTemplate) {
            return SecurityIssue(
                type = SecurityType.SQL_INJECTION,
                message = "SQL Injection - Use parameterized queries or exposed/jooq DSL",
                severity = Severity.CRITICAL
            )
        }

        return null
    }

    private fun checkHardcodedCredentials(element: PsiElement, text: String): SecurityIssue? {
        val elementText = element.text
        val credentialPatterns = listOf(
            "password", "passwd", "pwd",
            "apikey", "api_key", "secret", "token",
            "credential", "auth", "private_key", "access_key"
        )

        val hasCredentialName = credentialPatterns.any { pattern ->
            elementText.lowercase().contains("val $pattern") ||
            elementText.lowercase().contains("var $pattern") ||
            elementText.lowercase().contains("const val $pattern")
        }

        if (!hasCredentialName) return null

        val hasLiteralAssignment = text.contains("= \"") || text.contains("= '")

        if (hasLiteralAssignment) {
            return SecurityIssue(
                type = SecurityType.HARDCODED_CREDENTIALS,
                message = "Hardcoded credential - Use environment variables or secure vault",
                severity = Severity.CRITICAL
            )
        }

        return null
    }

    private fun checkPathTraversal(text: String): SecurityIssue? {
        val hasFileOperation = text.contains("file(") ||
                              text.contains("path(") ||
                              text.contains("tofile")

        if (!hasFileOperation) return null

        val hasConcatenation = text.contains("+") || text.contains("\${")
        val hasValidation = text.contains("normalize") ||
                          text.contains("canonicalpath") ||
                          text.contains("startswith")

        if (hasConcatenation && !hasValidation) {
            return SecurityIssue(
                type = SecurityType.PATH_TRAVERSAL,
                message = "Path Traversal - Validate and normalize file paths",
                severity = Severity.HIGH
            )
        }

        return null
    }

    private fun checkInsecureRandom(text: String): SecurityIssue? {
        val hasRandomUsage = text.contains("math.random()") ||
                            text.contains("random()")

        if (!hasRandomUsage) return null

        val isSecurityContext = text.contains("token") ||
                              text.contains("session") ||
                              text.contains("password") ||
                              text.contains("salt") ||
                              text.contains("key") ||
                              text.contains("nonce")

        if (isSecurityContext) {
            return SecurityIssue(
                type = SecurityType.INSECURE_RANDOM,
                message = "Insecure Random - Use SecureRandom for cryptographic operations",
                severity = Severity.HIGH
            )
        }

        return null
    }

    private fun checkCommandInjection(text: String): SecurityIssue? {
        val hasCommandExecution = text.contains("runtime.getruntime().exec") ||
                                 text.contains("processbuilder") ||
                                 text.contains("\"sh\"") ||
                                 text.contains("\"bash\"")

        if (!hasCommandExecution) return null

        val hasConcatenation = text.contains("+") || text.contains("\${")

        if (hasConcatenation) {
            return SecurityIssue(
                type = SecurityType.COMMAND_INJECTION,
                message = "Command Injection - Validate and sanitize command parameters",
                severity = Severity.CRITICAL
            )
        }

        return null
    }

    private fun checkXSS(text: String): SecurityIssue? {
        val hasHTMLOutput = text.contains("response.writer") ||
                           text.contains("respondtext") ||
                           text.contains("html {")

        if (!hasHTMLOutput) return null

        val hasConcatenation = text.contains("+") || text.contains("\${")
        val hasUserInput = text.contains("request.parameter") ||
                          text.contains("call.receive") ||
                          text.contains("request.header")

        if (hasConcatenation && hasUserInput) {
            val hasEscaping = text.contains("escapehtml") ||
                            text.contains("encode") ||
                            text.contains("sanitize")

            if (!hasEscaping) {
                return SecurityIssue(
                    type = SecurityType.XSS,
                    message = "XSS vulnerability - Escape HTML output or use template engine",
                    severity = Severity.HIGH
                )
            }
        }

        return null
    }

    private fun containsSQLKeywords(text: String): Boolean {
        val keywords = listOf(
            "select ", "insert ", "update ", "delete ",
            "create ", "drop ", "alter ", "truncate "
        )
        return keywords.any { text.contains(it) }
    }
}
