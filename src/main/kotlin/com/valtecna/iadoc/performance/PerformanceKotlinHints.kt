package com.valtecna.iadoc.performance

import com.intellij.psi.PsiElement

class PerformanceKotlinHints : PerformanceHints {

    override fun analyze(element: PsiElement): PerformanceIssue? {
        val className = element.javaClass.name
        val text = element.text

        if (!isFunctionOrProperty(className)) {
            return null
        }

        if (text.length > MAX_ELEMENT_SIZE) {
            return null
        }

        val textLower = text.lowercase()

        return checkNPlusOneQuery(textLower)
            ?: checkInefficientCollections(textLower)
            ?: checkMemoryLeakRisk(textLower)
            ?: checkBlockingMainThread(textLower)
            ?: checkLargeAllocation(textLower)
            ?: checkBoxingOverhead(textLower)
    }

    companion object {
        private const val MAX_ELEMENT_SIZE = 10000
    }

    private fun isFunctionOrProperty(className: String): Boolean {
        return className.contains("KtNamedFunction") ||
               className.contains("KtProperty") ||
               className.contains("KtParameter")
    }

    private fun checkNPlusOneQuery(text: String): PerformanceIssue? {
        val hasLoop = text.contains("for (") ||
                     text.contains("foreach") ||
                     text.contains(".foreach") ||
                     text.contains("while (")

        if (!hasLoop) return null

        val hasRepositoryCall = text.contains("repository.find") ||
                               text.contains("repository.get") ||
                               text.contains("dao.find") ||
                               text.contains("dao.get")

        val hasQueryExecution = text.contains("query.execute") ||
                               text.contains("transaction {") && text.contains(".find")

        if (hasRepositoryCall || hasQueryExecution) {
            return PerformanceIssue(
                type = PerformanceType.N_PLUS_ONE_QUERY,
                message = "N+1 Query detected - Use JOIN or batch loading",
                severity = PerformanceSeverity.CRITICAL
            )
        }

        return null
    }

    private fun checkInefficientCollections(text: String): PerformanceIssue? {
        val hasChainedOps = (text.contains(".filter {") && text.contains(".map {")) ||
                           (text.contains(".map {") && text.contains(".sortedby"))

        val hasSequenceConversion = text.contains(".assequence()")
        val hasToList = text.contains(".tolist()")

        val operationCount = text.split(".filter").size +
                           text.split(".map").size +
                           text.split(".sorted").size +
                           text.split(".distinct").size - 4

        if (hasChainedOps && operationCount > 2 && !hasSequenceConversion) {
            return PerformanceIssue(
                type = PerformanceType.INEFFICIENT_COLLECTION_OPS,
                message = "Use sequence for chained operations on large collections",
                severity = PerformanceSeverity.MEDIUM
            )
        }

        return null
    }

    private fun checkMemoryLeakRisk(text: String): PerformanceIssue? {
        val hasSubscribe = text.contains("subscribe") ||
                          text.contains("addobserver") ||
                          text.contains("addlistener")

        val hasUnsubscribe = text.contains("unsubscribe") ||
                            text.contains("removeobserver") ||
                            text.contains("removelistener") ||
                            text.contains("dispose")

        if (hasSubscribe && !hasUnsubscribe) {
            return PerformanceIssue(
                type = PerformanceType.MEMORY_LEAK_RISK,
                message = "Memory leak risk - Subscription not disposed",
                severity = PerformanceSeverity.HIGH
            )
        }

        val hasThreadLaunch = text.contains("thread {") ||
                             text.contains("globalscope.launch")

        val hasThreadJoin = text.contains(".join(") ||
                           text.contains(".cancel(")

        if (hasThreadLaunch && !hasThreadJoin && text.contains("globalscope")) {
            return PerformanceIssue(
                type = PerformanceType.MEMORY_LEAK_RISK,
                message = "Coroutine leak - Use structured concurrency instead of GlobalScope",
                severity = PerformanceSeverity.HIGH
            )
        }

        return null
    }

    private fun checkBlockingMainThread(text: String): PerformanceIssue? {
        val isUIContext = text.contains("dispatchers.main") ||
                         text.contains("withcontext(dispatchers.main)") ||
                         text.contains("runonuithread")

        if (!isUIContext) return null

        val hasBlockingOperation = text.contains("thread.sleep") ||
                                  text.contains("inputstream") ||
                                  text.contains("outputstream") ||
                                  text.contains("httpclient") ||
                                  text.contains("file.read") ||
                                  text.contains("file.write") ||
                                  text.contains("runblocking")

        if (hasBlockingOperation) {
            return PerformanceIssue(
                type = PerformanceType.BLOCKING_MAIN_THREAD,
                message = "Blocking operation on main thread - Use withContext(Dispatchers.IO)",
                severity = PerformanceSeverity.CRITICAL
            )
        }

        return null
    }

    private fun checkLargeAllocation(text: String): PerformanceIssue? {
        val allocationPattern = Regex("""(arraylistof|mutablelistof|hashsetof|hashmapof)\s*\(\s*(\d+)\s*\)""")
        val matches = allocationPattern.findAll(text)

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

        val arrayAllocation = Regex("""(intarray|longarray|doublearray)\s*\(\s*(\d+)\s*\)""")
        val arrayMatches = arrayAllocation.findAll(text)

        for (match in arrayMatches) {
            val size = match.groupValues[2].toIntOrNull() ?: continue
            if (size > 100000) {
                return PerformanceIssue(
                    type = PerformanceType.LARGE_ALLOCATION,
                    message = "Large array allocation ($size) - Consider lazy initialization",
                    severity = PerformanceSeverity.MEDIUM
                )
            }
        }

        return null
    }

    private fun checkBoxingOverhead(text: String): PerformanceIssue? {
        val hasBoxedList = text.contains("list<int>") ||
                          text.contains("list<long>") ||
                          text.contains("list<double>") ||
                          text.contains("arraylist<int>")

        val hasPrimitiveOperations = text.contains("for (") &&
                                     (text.contains("[") || text.contains(".get("))

        if (hasBoxedList && hasPrimitiveOperations) {
            return PerformanceIssue(
                type = PerformanceType.BOXING_OVERHEAD,
                message = "Boxing overhead - Use IntArray/LongArray for primitives",
                severity = PerformanceSeverity.LOW
            )
        }

        return null
    }
}
