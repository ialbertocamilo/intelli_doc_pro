package com.valtecna.iadoc.performance

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class PerformanceJavaHints : PerformanceHints {

    override fun analyze(element: PsiElement): PerformanceIssue? {
        if (element !is PsiMethod && element !is PsiField) {
            return null
        }

        val text = element.text
        if (text.length > MAX_ELEMENT_SIZE) {
            return null
        }

        return checkNPlusOneQuery(element)
            ?: checkInefficientCollections(element, text)
            ?: checkMemoryLeakRisk(element, text)
            ?: checkBlockingMainThread(element, text)
            ?: checkLargeAllocation(text)
            ?: checkBoxingOverhead(text)
    }

    companion object {
        private const val MAX_ELEMENT_SIZE = 10000
    }

    private fun checkNPlusOneQuery(element: PsiElement): PerformanceIssue? {
        if (element !is PsiMethod) return null

        val loops = PsiTreeUtil.findChildrenOfType(element, PsiForStatement::class.java)
            .plus(PsiTreeUtil.findChildrenOfType(element, PsiForeachStatement::class.java))
            .plus(PsiTreeUtil.findChildrenOfType(element, PsiWhileStatement::class.java))
            .take(20)

        for (loop in loops) {
            val loopText = loop.text.lowercase()

            val hasRepositoryCall = loopText.contains("repository.find") ||
                                   loopText.contains("repository.get") ||
                                   loopText.contains("dao.find") ||
                                   loopText.contains("dao.get") ||
                                   loopText.contains(".find(") ||
                                   loopText.contains(".get(")

            val hasQueryExecution = loopText.contains("query.execute") ||
                                   loopText.contains("statement.execute") ||
                                   loopText.contains("jdbctemplate.query")

            if (hasRepositoryCall || hasQueryExecution) {
                return PerformanceIssue(
                    type = PerformanceType.N_PLUS_ONE_QUERY,
                    message = "N+1 Query detected - Use JOIN or batch loading",
                    severity = PerformanceSeverity.CRITICAL
                )
            }
        }

        return null
    }

    private fun checkInefficientCollections(element: PsiElement, text: String): PerformanceIssue? {
        if (element !is PsiMethod) return null

        val textLower = text.lowercase()

        val hasChainedStreamOps = textLower.contains(".stream()") &&
                                 (textLower.contains(".filter(") && textLower.contains(".map(")) ||
                                 (textLower.contains(".map(") && textLower.contains(".collect("))

        val hasMultipleIntermediateOps = textLower.split(".filter(").size +
                                         textLower.split(".map(").size +
                                         textLower.split(".sorted").size > 4

        if (hasChainedStreamOps && hasMultipleIntermediateOps) {
            val hasParallelStream = textLower.contains(".parallelstream()")
            if (!hasParallelStream) {
                return PerformanceIssue(
                    type = PerformanceType.INEFFICIENT_COLLECTION_OPS,
                    message = "Consider parallel stream for large collections",
                    severity = PerformanceSeverity.MEDIUM
                )
            }
        }

        return null
    }

    private fun checkMemoryLeakRisk(element: PsiElement, text: String): PerformanceIssue? {
        if (element !is PsiMethod) return null

        val textLower = text.lowercase()

        val hasAddListener = textLower.contains("addlistener") ||
                            textLower.contains("addobserver") ||
                            textLower.contains("subscribe")

        val hasRemoveListener = textLower.contains("removelistener") ||
                               textLower.contains("removeobserver") ||
                               textLower.contains("unsubscribe") ||
                               textLower.contains("dispose")

        if (hasAddListener && !hasRemoveListener) {
            return PerformanceIssue(
                type = PerformanceType.MEMORY_LEAK_RISK,
                message = "Memory leak risk - Listener not removed",
                severity = PerformanceSeverity.HIGH
            )
        }

        val hasThreadStart = textLower.contains("thread(") && textLower.contains(".start()")
        val hasThreadJoin = textLower.contains(".join(") || textLower.contains(".interrupt(")

        if (hasThreadStart && !hasThreadJoin) {
            return PerformanceIssue(
                type = PerformanceType.MEMORY_LEAK_RISK,
                message = "Thread leak - Thread not properly terminated",
                severity = PerformanceSeverity.HIGH
            )
        }

        return null
    }

    private fun checkBlockingMainThread(element: PsiElement, text: String): PerformanceIssue? {
        if (element !is PsiMethod) return null

        val textLower = text.lowercase()

        val isEDTMethod = textLower.contains("invokeLater") ||
                         textLower.contains("invokeandwait") ||
                         textLower.contains("runwriteaction") ||
                         textLower.contains("actionperformed") ||
                         element.name.let { it == "actionPerformed" || it == "run" }

        if (!isEDTMethod) return null

        val hasBlockingOperation = textLower.contains("thread.sleep") ||
                                   textLower.contains("inputstream.read") ||
                                   textLower.contains("outputstream.write") ||
                                   textLower.contains("socket.") ||
                                   textLower.contains("httpclient") ||
                                   textLower.contains("files.read") ||
                                   textLower.contains("files.write")

        if (hasBlockingOperation) {
            return PerformanceIssue(
                type = PerformanceType.BLOCKING_MAIN_THREAD,
                message = "Blocking operation on main thread - Move to background",
                severity = PerformanceSeverity.CRITICAL
            )
        }

        return null
    }

    private fun checkLargeAllocation(text: String): PerformanceIssue? {
        val textLower = text.lowercase()

        val allocationPattern = Regex("""new\s+(arraylist|hashmap|hashset|linkedlist)\s*\(\s*(\d+)\s*\)""")
        val matches = allocationPattern.findAll(textLower)

        for (match in matches) {
            val size = match.groupValues[2].toIntOrNull() ?: continue
            if (size > 10000) {
                return PerformanceIssue(
                    type = PerformanceType.LARGE_ALLOCATION,
                    message = "Large allocation ($size) - Consider lazy initialization",
                    severity = PerformanceSeverity.MEDIUM
                )
            }
        }

        return null
    }

    private fun checkBoxingOverhead(text: String): PerformanceIssue? {
        val textLower = text.lowercase()

        val hasBoxedCollection = textLower.contains("list<integer>") ||
                                textLower.contains("list<long>") ||
                                textLower.contains("list<double>") ||
                                textLower.contains("arraylist<integer>") ||
                                textLower.contains("arraylist<long>")

        val hasPrimitiveOperations = textLower.contains("for (") &&
                                     (textLower.contains(".get(") || textLower.contains(".add("))

        if (hasBoxedCollection && hasPrimitiveOperations) {
            return PerformanceIssue(
                type = PerformanceType.BOXING_OVERHEAD,
                message = "Boxing overhead - Use primitive arrays for performance",
                severity = PerformanceSeverity.LOW
            )
        }

        return null
    }
}
