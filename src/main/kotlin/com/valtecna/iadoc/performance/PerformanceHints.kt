package com.valtecna.iadoc.performance

import com.intellij.psi.PsiElement

interface PerformanceHints {
    fun analyze(element: PsiElement): PerformanceIssue?
}

data class PerformanceIssue(
    val type: PerformanceType,
    val message: String,
    val severity: PerformanceSeverity
)

enum class PerformanceType {
    N_PLUS_ONE_QUERY,
    INEFFICIENT_COLLECTION_OPS,
    MEMORY_LEAK_RISK,
    BLOCKING_MAIN_THREAD,
    LARGE_ALLOCATION,
    BOXING_OVERHEAD
}

enum class PerformanceSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW
}
