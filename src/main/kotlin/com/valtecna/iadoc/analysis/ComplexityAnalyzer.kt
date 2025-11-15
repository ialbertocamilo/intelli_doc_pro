package com.valtecna.iadoc.analysis

import com.intellij.psi.PsiMethod

class ComplexityAnalyzer {

    fun analyze(method: PsiMethod): ComplexityResult {
        val visitor = ComplexityVisitor(method)
        method.accept(visitor)

        val time = inferTimeComplexity(visitor)
        val space = inferSpaceComplexity(visitor)

        val details = buildList {
            add("Loops: ${visitor.loopCount}")
            add("Max depth: ${visitor.maxNestedDepth}")
            add("Streams: ${visitor.streamOps}")
            add("Recursion: ${visitor.recursiveCalls}")
            add("Double recursion: ${visitor.doubleRecursiveCalls}")
            add("Collections: ${visitor.newCollections}")
        }

        return ComplexityResult(time, space, details)
    }

    private fun inferTimeComplexity(v: ComplexityVisitor): String {
        // ORDEN DE PRIORIDAD (de mayor a menor complejidad):

        // 1. Factorial O(n!)
        if (v.containsFactorialPatterns) {
            return "O(n!)"
        }

        // 2. Exponencial O(2ⁿ)
        if (v.doubleRecursiveCalls > 0 || v.recursiveCalls >= 2) {
            return "O(2ⁿ)"
        }

        // 3. Cúbico O(n³) - 3 o más loops anidados
        if (v.maxNestedDepth >= 3) {
            return "O(n³)"
        }

        // 4. Cuadrático O(n²) - 2 loops anidados
        // IMPORTANTE: Verificar ANTES de patrones logarítmicos
        if (v.maxNestedDepth == 2) {
            return "O(n²)"
        }

        // 5. Linearítmico O(n log n) - Algoritmos de ordenamiento
        if (v.containsSortingPatterns) {
            return "O(n log n)"
        }

        // 6. Lineal O(n) - Loop simple o recursión simple
        // Verificar ANTES de logarítmico para evitar falsos positivos
        if (v.maxNestedDepth == 1 && !v.hasBinarySearchPattern && !v.containsLogarithmicPatterns) {
            return "O(n)"
        }

        // 7. Logarítmico O(log n) - División/multiplicación por constante
        // Solo si NO hay loops anidados (maxNestedDepth < 2)
        if ((v.containsLogarithmicPatterns || v.hasBinarySearchPattern) && v.maxNestedDepth <= 1) {
            return "O(log n)"
        }

        // 8. Recursión simple sin loops
        if (v.recursiveCalls > 0 && v.loopCount == 0) {
            return "O(n)"
        }

        // 9. Streams simples
        if (v.streamOps >= 1 && v.loopCount == 0) {
            return "O(n)"
        }

        // 10. Constante O(1) - Sin loops, streams ni recursión
        if (v.loopCount == 0 && v.streamOps == 0 && v.recursiveCalls == 0) {
            return "O(1)"
        }

        return "O(?)"
    }

    private fun inferSpaceComplexity(v: ComplexityVisitor): String {
        return when {
            v.newCollections > 0 -> "O(n)"
            else -> "O(1)"
        }
    }
}
