package com.valtecna.iadoc.analysis

/**
 * Universal complexity analyzer that works with any programming language
 * by analyzing the code text instead of relying on language-specific PSI.
 */
class UniversalComplexityAnalyzer {

    fun analyze(functionText: String, functionName: String = ""): ComplexityResult {
        val metrics = analyzeText(functionText, functionName)
        val time = inferTimeComplexity(metrics)
        val space = inferSpaceComplexity(metrics)

        val details = buildList {
            add("Loops: ${metrics.loopCount}")
            add("Max depth: ${metrics.maxNestedDepth}")
            add("Recursion: ${metrics.recursiveCalls}")
        }

        return ComplexityResult(time, space, details)
    }

    private fun analyzeText(text: String, functionName: String): ComplexityMetrics {
        val lines = text.lines()
        var loopCount = 0
        var currentDepth = 0
        var maxDepth = 0
        var recursiveCalls = 0
        var doubleRecursiveCalls = 0
        var hasLogarithmicPattern = false
        var hasSortingPattern = false
        var hasFactorialPattern = false

        // Track nesting depth
        val depthStack = mutableListOf<LoopType>()

        for (line in lines) {
            val trimmed = line.trim()

            // Skip comments and empty lines
            if (trimmed.isEmpty() ||
                trimmed.startsWith("//") ||
                trimmed.startsWith("#") ||
                trimmed.startsWith("/*") ||
                trimmed.startsWith("*")) {
                continue
            }

            // Detect loops (for, while, forEach, map, etc.)
            if (isLoopStatement(trimmed)) {
                loopCount++
                currentDepth++
                depthStack.add(LoopType.LOOP)
                maxDepth = maxOf(maxDepth, currentDepth)

                // Check for logarithmic patterns in loop
                if (hasLogarithmicUpdate(trimmed)) {
                    hasLogarithmicPattern = true
                }
            }

            // Check for loop closing braces/keywords
            if (isLoopClosing(trimmed)) {
                if (depthStack.isNotEmpty()) {
                    depthStack.removeAt(depthStack.size - 1)
                    currentDepth = maxOf(0, currentDepth - 1)
                }
            }

            // Detect recursion
            if (functionName.isNotEmpty() && trimmed.contains(functionName) && trimmed.contains("(")) {
                recursiveCalls++
                // Count how many times the function is called in one line (double recursion)
                val callCount = trimmed.split(functionName).size - 1
                if (callCount >= 2) {
                    doubleRecursiveCalls++
                }
            }

            // Detect sorting patterns
            if (hasSortingKeyword(trimmed)) {
                hasSortingPattern = true
            }

            // Detect factorial/permutation patterns
            if (hasFactorialKeyword(trimmed)) {
                hasFactorialPattern = true
            }

            // Detect logarithmic patterns in body
            if (hasLogarithmicOperation(trimmed)) {
                hasLogarithmicPattern = true
            }
        }

        return ComplexityMetrics(
            loopCount = loopCount,
            maxNestedDepth = maxDepth,
            recursiveCalls = recursiveCalls,
            doubleRecursiveCalls = doubleRecursiveCalls,
            hasLogarithmicPattern = hasLogarithmicPattern,
            hasSortingPattern = hasSortingPattern,
            hasFactorialPattern = hasFactorialPattern
        )
    }

    private fun isLoopStatement(line: String): Boolean {
        return line.matches(Regex(".*\\b(for|while|forEach|map|filter|reduce|loop|each|times)\\b.*")) ||
               line.matches(Regex(".*\\b(for)\\s+.*\\bin\\b.*")) ||  // Python: for x in ...
               line.matches(Regex(".*\\.forEach.*")) ||
               line.matches(Regex(".*\\.map.*")) ||
               line.matches(Regex(".*\\.filter.*"))
    }

    private fun isLoopClosing(line: String): Boolean {
        return line == "}" ||
               line.startsWith("}") ||
               line == "end" ||
               line.matches(Regex("^end\\b.*")) // Ruby, Lua
    }

    private fun hasLogarithmicUpdate(line: String): Boolean {
        // Detect i /= 2, i *= 2, i >>= 1, i <<= 1, etc.
        return line.matches(Regex(".*[a-zA-Z_]\\w*\\s*[\\/\\*]?=\\s*[a-zA-Z_]\\w*\\s*[\\/\\*]\\s*[2-9].*")) ||
               line.matches(Regex(".*[a-zA-Z_]\\w*\\s*[><]=.*")) ||
               line.contains("/=") || line.contains("*=") ||
               line.contains(">>=") || line.contains("<<=")
    }

    private fun hasLogarithmicOperation(line: String): Boolean {
        return line.matches(Regex(".*\\s*=\\s*.*[\\/]\\s*2.*")) ||  // x = n / 2
               line.matches(Regex(".*\\s*=\\s*.*\\*\\s*2.*")) ||    // x = n * 2
               line.contains("Math.log") ||
               line.contains("math.log") ||
               line.contains("log(") ||
               line.contains(">>") || line.contains("<<")  // Bit shifts
    }

    private fun hasSortingKeyword(line: String): Boolean {
        return line.matches(Regex(".*\\b(sort|sorted|quicksort|mergesort|heapsort|timsort)\\b.*"))
    }

    private fun hasFactorialKeyword(line: String): Boolean {
        return line.matches(Regex(".*\\b(permut|permutation|factorial)\\b.*"))
    }

    private fun inferTimeComplexity(m: ComplexityMetrics): String {
        // Priority order (from highest to lowest complexity):

        // 1. Factorial O(n!)
        if (m.hasFactorialPattern) {
            return "O(n!)"
        }

        // 2. Exponential O(2ⁿ)
        if (m.doubleRecursiveCalls > 0 || m.recursiveCalls >= 2) {
            return "O(2ⁿ)"
        }

        // 3. Cubic O(n³) - 3+ nested loops
        if (m.maxNestedDepth >= 3) {
            return "O(n³)"
        }

        // 4. Quadratic O(n²) - 2 nested loops
        if (m.maxNestedDepth == 2) {
            return "O(n²)"
        }

        // 5. Linearithmic O(n log n) - Sorting algorithms
        if (m.hasSortingPattern) {
            return "O(n log n)"
        }

        // 6. Linear O(n) - Single loop or simple recursion
        if (m.maxNestedDepth == 1 && !m.hasLogarithmicPattern) {
            return "O(n)"
        }

        // 7. Logarithmic O(log n) - Division/multiplication by constant
        if (m.hasLogarithmicPattern && m.maxNestedDepth <= 1) {
            return "O(log n)"
        }

        // 8. Simple recursion without loops
        if (m.recursiveCalls > 0 && m.loopCount == 0) {
            return "O(n)"
        }

        // 9. Constant O(1) - No loops or recursion
        if (m.loopCount == 0 && m.recursiveCalls == 0) {
            return "O(1)"
        }

        return "O(?)"
    }

    private fun inferSpaceComplexity(m: ComplexityMetrics): String {
        // Simple heuristic: recursion uses stack space
        return when {
            m.recursiveCalls > 0 -> "O(n)"
            else -> "O(1)"
        }
    }

    private data class ComplexityMetrics(
        val loopCount: Int,
        val maxNestedDepth: Int,
        val recursiveCalls: Int,
        val doubleRecursiveCalls: Int,
        val hasLogarithmicPattern: Boolean,
        val hasSortingPattern: Boolean,
        val hasFactorialPattern: Boolean
    )

    private enum class LoopType {
        LOOP
    }
}
