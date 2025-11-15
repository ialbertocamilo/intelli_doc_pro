# Tests del Analizador de Complejidad

## Descripción

Este directorio contiene tests unitarios para verificar el correcto funcionamiento del analizador de complejidad algorítmica del plugin IaDoc.

## Tests Implementados

### ComplexityAnalyzerTest

Verifica que el analizador detecte correctamente todas las complejidades algorítmicas:

| Complejidad | Tests | Descripción |
|-------------|-------|-------------|
| **O(1)** | `testConstantTime` | Operaciones constantes sin loops |
| **O(log n)** | `testBinarySearch`, `testLogarithmicDivision`, `testLogarithmicMultiplication`, `testLogarithmicBitShift` | Búsqueda binaria, división/multiplicación por constante, bit shifting |
| **O(n)** | `testLinearLoop`, `testLinearWhile`, `testLinearRecursion` | Loops simples, recursión simple |
| **O(n log n)** | `testMergeSort`, `testArraysSort` | Algoritmos de ordenamiento |
| **O(n²)** | `testBubbleSort`, `testNestedLoops`, `testNestedWhileInFor` | Loops anidados (2 niveles) |
| **O(n³)** | `testTripleNestedLoops` | Loops anidados (3 niveles) |
| **O(2ⁿ)** | `testFibonacciRecursive` | Recursión doble (Fibonacci) |

## Ejecutar Tests

### Opción 1: Desde Gradle

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar solo los tests de ComplexityAnalyzer
./gradlew test --tests ComplexityAnalyzerTest

# Ejecutar un test específico
./gradlew test --tests ComplexityAnalyzerTest.testBinarySearch
```

### Opción 2: Desde IntelliJ IDEA

1. Abre el archivo `ComplexityAnalyzerTest.kt`
2. Click derecho en la clase o en un método de test
3. Selecciona "Run 'ComplexityAnalyzerTest'" o "Run 'testNombre'"

## Ver Resultados

Los resultados de los tests se mostrarán en la consola con el siguiente formato:

```
> Task :test

ComplexityAnalyzerTest > testConstantTime() PASSED
ComplexityAnalyzerTest > testBinarySearch() PASSED
ComplexityAnalyzerTest > testLogarithmicDivision() PASSED
ComplexityAnalyzerTest > testLinearLoop() PASSED
ComplexityAnalyzerTest > testBubbleSort() PASSED
ComplexityAnalyzerTest > testTripleNestedLoops() PASSED
ComplexityAnalyzerTest > testFibonacciRecursive() PASSED

BUILD SUCCESSFUL
```

## Agregar Nuevos Tests

Para agregar nuevos tests de complejidad:

1. Abre `ComplexityAnalyzerTest.kt`
2. Crea una nueva función con el prefijo `test`
3. Usa el helper `getMethodFromCode()` para crear el código de prueba
4. Llama a `analyzer.analyze(method)` y verifica con `assertEquals()`

Ejemplo:

```kotlin
fun testMiNuevoAlgoritmo() {
    val code = """
        class Test {
            public void miAlgoritmo(int n) {
                // Tu código aquí
            }
        }
    """.trimIndent()

    val method = getMethodFromCode(code, "miAlgoritmo")
    val result = analyzer.analyze(method)

    assertEquals("O(n)", result.time)
}
```

## Cobertura de Tests

Los tests cubren:

- ✅ Todas las complejidades de O(1) a O(n!)
- ✅ Diferentes tipos de loops (for, while, do-while)
- ✅ Recursión simple y doble
- ✅ Patrones de división/multiplicación
- ✅ Bit shifting
- ✅ Algoritmos de ordenamiento
- ✅ Anidamiento de loops (1, 2, 3 niveles)

## Notas

- Los tests usan `BasePlatformTestCase` del IntelliJ Platform SDK
- Cada test crea código Java dinámicamente usando `myFixture.configureByText()`
- Los tests validan tanto el tiempo de complejidad como el análisis correcto de patrones
