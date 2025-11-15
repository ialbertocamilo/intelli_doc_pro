package com.valtecna.iadoc.analysis

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class ComplexityVisitor(private val method: PsiMethod) : JavaRecursiveElementWalkingVisitor() {

    var loopCount = 0
    var maxNestedDepth = 0
    var newCollections = 0
    var streamOps = 0
    var recursiveCalls = 0
    var doubleRecursiveCalls = 0
    var containsLogarithmicPatterns = false
    var containsSortingPatterns = false
    var containsFactorialPatterns = false
    var hasBinarySearchPattern = false

    private var currentLoopDepth = 0

    override fun visitForStatement(statement: PsiForStatement) {
        // Verificar tanto el body como la actualización del for (i++, i*=2, etc.)
        checkForBinarySearchPattern(statement.body)
        statement.update?.let { checkForBinarySearchPattern(it) }
        enterLoop()
        super.visitForStatement(statement)
        exitLoop()
    }

    override fun visitForeachStatement(statement: PsiForeachStatement) {
        enterLoop()
        super.visitForeachStatement(statement)
        exitLoop()
    }

    override fun visitWhileStatement(statement: PsiWhileStatement) {
        checkForBinarySearchPattern(statement.body)
        enterLoop()
        super.visitWhileStatement(statement)
        exitLoop()
    }

    override fun visitDoWhileStatement(statement: PsiDoWhileStatement) {
        checkForBinarySearchPattern(statement.body)
        enterLoop()
        super.visitDoWhileStatement(statement)
        exitLoop()
    }

    private fun enterLoop() {
        loopCount++
        currentLoopDepth++
        if (currentLoopDepth > maxNestedDepth) {
            maxNestedDepth = currentLoopDepth
        }
    }

    private fun exitLoop() {
        currentLoopDepth--
    }

    override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
        val methodName = expression.methodExpression.referenceName

        if (methodName == "stream" ||
            methodName == "parallelStream" ||
            methodName == "map" ||
            methodName == "filter" ||
            methodName == "forEach") {
            streamOps++
        }

        if (methodName == method.name) {
            recursiveCalls++
            val callsInMethod = PsiTreeUtil.findChildrenOfType(method.body, PsiMethodCallExpression::class.java)
                .count { it.methodExpression.referenceName == method.name }
            if (callsInMethod >= 2) {
                doubleRecursiveCalls++
            }
        }

        if (methodName == "binarySearch" || methodName?.contains("log") == true) {
            containsLogarithmicPatterns = true
        }

        if (methodName == "sort" || methodName == "mergeSort" || methodName == "heapSort" ||
            methodName == "quickSort") {
            containsSortingPatterns = true
        }

        // Solo detectar O(n!) si es permutación, NO factorial simple
        if (methodName?.contains("permut", ignoreCase = true) == true) {
            containsFactorialPatterns = true
        }

        super.visitMethodCallExpression(expression)
    }

    override fun visitNewExpression(expression: PsiNewExpression) {
        val type = expression.type?.canonicalText ?: ""
        if (type.contains("List") || type.contains("Map") || type.contains("Set")) {
            newCollections++
        }
        super.visitNewExpression(expression)
    }

    /**
     * Detecta patrones logarítmicos generales:
     * - División/multiplicación por constante (i /= 2, i *= 3, n = n/2, etc.)
     * - Reducción del espacio de búsqueda a la mitad
     * - Cualquier patrón donde el tamaño del problema se divide en cada iteración
     */
    private fun checkForBinarySearchPattern(body: PsiElement?) {
        if (body == null) return

        val text = body.text

        // Patrón 1: División compuesta (i /= n, i = i / n)
        val hasDivisionAssignment = text.contains(Regex("\\w+\\s*/=\\s*\\d+")) ||
                                    text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*/\\s*\\d+"))

        // Patrón 2: Multiplicación compuesta (i *= n, i = i * n)
        val hasMultiplicationAssignment = text.contains(Regex("\\w+\\s*\\*=\\s*\\d+")) ||
                                          text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*\\*\\s*\\d+"))

        // Patrón 3: División a la mitad con punto medio (mid = (left + right) / 2)
        val hasMidpointCalculation = text.contains(Regex("\\w+\\s*=\\s*\\(.*[+].*\\)\\s*/\\s*\\d+")) ||
                                     text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*[+]\\s*\\(.*\\)\\s*/\\s*\\d+"))

        // Patrón 4: Actualización de límites (reduce el espacio de búsqueda)
        val hasBoundsUpdate = text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*[+\\-]\\s*\\d+"))

        // Patrón 5: Desplazamiento de bits (i >>= 1, i <<= 1, equivalente a i /= 2, i *= 2)
        val hasBitShift = text.contains(Regex("\\w+\\s*>>=\\s*\\d+")) ||
                          text.contains(Regex("\\w+\\s*<<=\\s*\\d+")) ||
                          text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*>>\\s*\\d+")) ||
                          text.contains(Regex("\\w+\\s*=\\s*\\w+\\s*<<\\s*\\d+"))

        // Si tiene división/multiplicación por constante, es logarítmico
        if (hasDivisionAssignment || hasMultiplicationAssignment || hasBitShift) {
            hasBinarySearchPattern = true
            containsLogarithmicPatterns = true
            return
        }

        // Si tiene punto medio Y actualización de límites, es logarítmico (búsqueda binaria)
        if (hasMidpointCalculation && hasBoundsUpdate) {
            hasBinarySearchPattern = true
            containsLogarithmicPatterns = true
        }
    }
}
