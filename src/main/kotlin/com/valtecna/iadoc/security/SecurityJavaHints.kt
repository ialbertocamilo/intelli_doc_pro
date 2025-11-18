package com.valtecna.iadoc.security

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class SecurityJavaHints : SecurityHints {

    override fun analyze(element: PsiElement): SecurityIssue? {
        if (element !is PsiMethod && element !is PsiField && element !is PsiVariable) {
            return null
        }

        val text = element.text
        if (text.length > MAX_ELEMENT_SIZE) {
            return null
        }

        return checkSQLInjection(element)
            ?: checkHardcodedCredentials(element)
            ?: checkPathTraversal(element)
            ?: checkInsecureRandom(element)
            ?: checkCommandInjection(element)
            ?: checkXSS(element)
    }

    companion object {
        private const val MAX_ELEMENT_SIZE = 10000
    }

    private fun checkSQLInjection(element: PsiElement): SecurityIssue? {
        val text = element.text.lowercase()

        if (!containsSQLKeywords(text)) return null

        val hasStringConcatenation = text.contains("+") || text.contains("concat(")
        val hasStringFormat = text.contains("string.format") || text.contains("\${")

        if (hasStringConcatenation || hasStringFormat) {
            if (element is PsiMethod) {
                val literals = PsiTreeUtil.findChildrenOfType(element, PsiLiteralExpression::class.java)
                    .take(50)

                val hasSQLInLiteral = literals.any { literal ->
                    val value = literal.value?.toString()?.lowercase() ?: ""
                    containsSQLKeywords(value)
                }

                if (hasSQLInLiteral) {
                    return SecurityIssue(
                        type = SecurityType.SQL_INJECTION,
                        message = "SQL Injection - Use PreparedStatement instead of string concatenation",
                        severity = Severity.CRITICAL
                    )
                }
            }
        }

        return null
    }

    private fun checkHardcodedCredentials(element: PsiElement): SecurityIssue? {
        if (element !is PsiField && element !is PsiVariable) return null

        val name = when (element) {
            is PsiField -> element.name
            is PsiVariable -> element.name
            else -> null
        }?.lowercase() ?: return null

        val credentialPatterns = listOf(
            "password", "passwd", "pwd",
            "apikey", "api_key", "secret", "token",
            "credential", "auth", "private_key", "access_key"
        )

        if (!credentialPatterns.any { name.contains(it) }) return null

        val initializer = when (element) {
            is PsiField -> element.initializer
            is PsiVariable -> element.initializer
            else -> null
        }

        val isHardcoded = initializer is PsiLiteralExpression &&
                initializer.value is String &&
                (initializer.value as? String)?.isNotEmpty() == true

        if (isHardcoded) {
            return SecurityIssue(
                type = SecurityType.HARDCODED_CREDENTIALS,
                message = "Hardcoded credential - Use environment variables or secure vault",
                severity = Severity.CRITICAL
            )
        }

        return null
    }

    private fun checkPathTraversal(element: PsiElement): SecurityIssue? {
        val text = element.text

        val hasFileOperation = text.contains("File(") ||
                              text.contains("Files.") ||
                              text.contains("Paths.get")

        if (!hasFileOperation) return null

        val hasConcatenation = text.contains("+")
        val hasUserInput = text.contains("request.getParameter") ||
                          text.contains("Scanner") ||
                          text.contains("BufferedReader")

        if (hasConcatenation && element is PsiMethod) {
            val hasValidation = text.contains("normalize") ||
                              text.contains("getCanonicalPath") ||
                              text.contains("startsWith")

            if (!hasValidation) {
                return SecurityIssue(
                    type = SecurityType.PATH_TRAVERSAL,
                    message = "Path Traversal - Validate and normalize file paths",
                    severity = Severity.HIGH
                )
            }
        }

        return null
    }

    private fun checkInsecureRandom(element: PsiElement): SecurityIssue? {
        val text = element.text

        val hasRandomUsage = text.contains("Math.random()") ||
                            text.contains("new Random()")

        if (!hasRandomUsage) return null

        val isSecurityContext = text.lowercase().let { t ->
            t.contains("token") || t.contains("session") ||
            t.contains("password") || t.contains("salt") ||
            t.contains("key") || t.contains("nonce")
        }

        if (isSecurityContext) {
            return SecurityIssue(
                type = SecurityType.INSECURE_RANDOM,
                message = "Insecure Random - Use SecureRandom for cryptographic operations",
                severity = Severity.HIGH
            )
        }

        return null
    }

    private fun checkCommandInjection(element: PsiElement): SecurityIssue? {
        val text = element.text

        val hasCommandExecution = text.contains("Runtime.getRuntime().exec") ||
                                 text.contains("ProcessBuilder")

        if (!hasCommandExecution) return null

        val hasConcatenation = text.contains("+")

        if (hasConcatenation && element is PsiMethod) {
            return SecurityIssue(
                type = SecurityType.COMMAND_INJECTION,
                message = "Command Injection - Validate and sanitize command parameters",
                severity = Severity.CRITICAL
            )
        }

        return null
    }

    private fun checkXSS(element: PsiElement): SecurityIssue? {
        val text = element.text.lowercase()

        val hasHTMLOutput = text.contains("response.getwriter") ||
                           text.contains("printwriter") ||
                           text.contains("servletoutputstream") ||
                           text.contains(".html")

        if (!hasHTMLOutput) return null

        val hasConcatenation = text.contains("+")
        val hasUserInput = text.contains("request.getparameter") ||
                          text.contains("request.getheader")

        if (hasConcatenation && hasUserInput && element is PsiMethod) {
            val hasEscaping = text.contains("htmlutils") ||
                            text.contains("escapehtml") ||
                            text.contains("encode")

            if (!hasEscaping) {
                return SecurityIssue(
                    type = SecurityType.XSS,
                    message = "XSS vulnerability - Escape HTML output",
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
