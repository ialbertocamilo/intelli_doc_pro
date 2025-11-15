package com.valtecna.iadoc.analysis

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiJavaFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Tests para verificar el correcto funcionamiento del analizador de complejidad algorítmica.
 *
 * Verifica que detecte correctamente:
 * - O(1) - Constante
 * - O(log n) - Logarítmico
 * - O(n) - Lineal
 * - O(n log n) - Linearítmico
 * - O(n²) - Cuadrático
 * - O(n³) - Cúbico
 * - O(2ⁿ) - Exponencial
 * - O(n!) - Factorial
 */
class ComplexityAnalyzerTest : BasePlatformTestCase() {

    private lateinit var analyzer: ComplexityAnalyzer

    override fun setUp() {
        super.setUp()
        analyzer = ComplexityAnalyzer()
    }

    // ==================== O(1) Tests ====================

    fun testConstantTime() {
        val code = """
            class Test {
                public int constant(int x) {
                    return x * 2 + 5;
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "constant")
        val result = analyzer.analyze(method)

        assertEquals("O(1)", result.time)
    }

    // ==================== O(log n) Tests ====================

    fun testBinarySearch() {
        val code = """
            class Test {
                public int binarySearch(int[] arr, int target) {
                    int left = 0;
                    int right = arr.length - 1;

                    while (left <= right) {
                        int mid = left + (right - left) / 2;

                        if (arr[mid] == target) {
                            return mid;
                        } else if (arr[mid] < target) {
                            left = mid + 1;
                        } else {
                            right = mid - 1;
                        }
                    }
                    return -1;
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "binarySearch")
        val result = analyzer.analyze(method)

        assertEquals("O(log n)", result.time)
    }

    fun testLogarithmicDivision() {
        val code = """
            class Test {
                public void logarithmic(int n) {
                    while (n > 0) {
                        n /= 2;
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "logarithmic")
        val result = analyzer.analyze(method)

        assertEquals("O(log n)", result.time)
    }

    fun testLogarithmicMultiplication() {
        val code = """
            class Test {
                public void logarithmic(int n) {
                    for (int i = 1; i < n; i *= 2) {
                        System.out.println(i);
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "logarithmic")
        val result = analyzer.analyze(method)

        assertEquals("O(log n)", result.time)
    }

    fun testLogarithmicBitShift() {
        val code = """
            class Test {
                public void bitShift(int n) {
                    while (n > 0) {
                        n >>= 1;
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "bitShift")
        val result = analyzer.analyze(method)

        assertEquals("O(log n)", result.time)
    }

    // ==================== O(n) Tests ====================

    fun testLinearLoop() {
        val code = """
            class Test {
                public void linear(int[] arr) {
                    for (int i = 0; i < arr.length; i++) {
                        System.out.println(arr[i]);
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "linear")
        val result = analyzer.analyze(method)

        assertEquals("O(n)", result.time)
    }

    fun testLinearWhile() {
        val code = """
            class Test {
                public void linear(int n) {
                    int i = 0;
                    while (i < n) {
                        i++;
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "linear")
        val result = analyzer.analyze(method)

        assertEquals("O(n)", result.time)
    }

    fun testLinearRecursion() {
        val code = """
            class Test {
                public int factorial(int n) {
                    if (n <= 1) return 1;
                    return n * factorial(n - 1);
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "factorial")
        val result = analyzer.analyze(method)

        // Recursión simple es O(n)
        assertEquals("O(n)", result.time)
    }

    // ==================== O(n log n) Tests ====================

    fun testMergeSort() {
        val code = """
            class Test {
                public void mergeSort(int[] arr) {
                    if (arr.length <= 1) return;

                    int mid = arr.length / 2;
                    int[] left = new int[mid];
                    int[] right = new int[arr.length - mid];

                    System.arraycopy(arr, 0, left, 0, mid);
                    System.arraycopy(arr, mid, right, 0, arr.length - mid);

                    mergeSort(left);
                    mergeSort(right);
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "mergeSort")
        val result = analyzer.analyze(method)

        assertEquals("O(n log n)", result.time)
    }

    fun testArraysSort() {
        val code = """
            class Test {
                public void sortArray(int[] arr) {
                    Arrays.sort(arr);
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "sortArray")
        val result = analyzer.analyze(method)

        assertEquals("O(n log n)", result.time)
    }

    // ==================== O(n²) Tests ====================

    fun testBubbleSort() {
        val code = """
            class Test {
                public void bubbleSort(int[] arr) {
                    for (int i = 0; i < arr.length; i++) {
                        for (int j = 0; j < arr.length; j++) {
                            if (arr[i] > arr[j]) {
                                int temp = arr[i];
                                arr[i] = arr[j];
                                arr[j] = temp;
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "bubbleSort")
        val result = analyzer.analyze(method)

        assertEquals("O(n²)", result.time)
    }

    fun testNestedLoops() {
        val code = """
            class Test {
                public boolean hasDuplicate(int[] arr) {
                    for (int i = 0; i < arr.length; i++) {
                        for (int j = i + 1; j < arr.length; j++) {
                            if (arr[i] == arr[j]) return true;
                        }
                    }
                    return false;
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "hasDuplicate")
        val result = analyzer.analyze(method)

        assertEquals("O(n²)", result.time)
    }

    fun testNestedWhileInFor() {
        val code = """
            class Test {
                public void nested(int n) {
                    for (int i = 0; i < n; i++) {
                        int j = 0;
                        while (j < n) {
                            j++;
                        }
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "nested")
        val result = analyzer.analyze(method)

        assertEquals("O(n²)", result.time)
    }

    // ==================== O(n³) Tests ====================

    fun testTripleNestedLoops() {
        val code = """
            class Test {
                public void cubic(int n) {
                    for (int i = 0; i < n; i++) {
                        for (int j = 0; j < n; j++) {
                            for (int k = 0; k < n; k++) {
                                System.out.println(i + j + k);
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "cubic")
        val result = analyzer.analyze(method)

        assertEquals("O(n³)", result.time)
    }

    // ==================== O(2ⁿ) Tests ====================

    fun testFibonacciRecursive() {
        val code = """
            class Test {
                public int fibonacci(int n) {
                    if (n <= 1) return n;
                    return fibonacci(n - 1) + fibonacci(n - 2);
                }
            }
        """.trimIndent()

        val method = getMethodFromCode(code, "fibonacci")
        val result = analyzer.analyze(method)

        assertEquals("O(2ⁿ)", result.time)
    }

    // ==================== Helper Methods ====================

    /**
     * Crea un método PSI a partir de código Java.
     */
    private fun getMethodFromCode(code: String, methodName: String): PsiMethod {
        val psiFile = myFixture.configureByText("Test.java", code) as PsiJavaFile
        val psiClass = psiFile.classes[0]
        return psiClass.findMethodsByName(methodName, false)[0]
    }
}
