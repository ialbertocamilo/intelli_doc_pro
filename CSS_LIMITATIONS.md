# CSS Limitations in Swing's StyleSheet

## Problem

When implementing syntax highlighting for code blocks in the IaDoc plugin, we encountered `NullPointerException` errors caused by unsupported CSS properties in Swing's `javax.swing.text.html.StyleSheet`.

## Error Details

```
java.lang.NullPointerException: Cannot invoke "javax.swing.text.html.CSS$CssValue.parseCssValue(String)" because "conv" is null
    at java.desktop/javax.swing.text.html.CSS.getInternalCSSValue(CSS.java:850)
    at java.desktop/javax.swing.text.html.CSS.addInternalCSSValue(CSS.java:835)
    at java.desktop/javax.swing.text.html.StyleSheet.addCSSAttribute(StyleSheet.java:524)
```

## Root Cause

Swing's `StyleSheet` (used by `JEditorPane` with `HTMLEditorKit`) only supports a **limited subset** of CSS 1.0 properties. When you use unsupported properties, it throws `NullPointerException` instead of gracefully ignoring them.

## Unsupported CSS Properties

The following properties **MUST BE AVOIDED**:

### ❌ Padding
```kotlin
// BROKEN - Causes NullPointerException
addRule("pre { padding: 8px; }")
addRule("code { padding: 2px 4px; }")
```

**Workaround**: Use `margin-top` and `margin-bottom` instead for spacing.

### ❌ Border-Radius
```kotlin
// BROKEN - Causes NullPointerException
addRule("pre { border-radius: 4px; }")
```

**Workaround**: No workaround. Rounded corners are not possible with StyleSheet.

### ❌ Specific Font Names
```kotlin
// BROKEN - Causes NullPointerException
addRule("pre { font-family: 'JetBrains Mono'; }")
addRule("code { font-family: 'Consolas'; }")
addRule("body { font-family: 'Segoe UI', sans-serif; }")
```

**Workaround**: Use generic font family names only: `monospace`, `sans-serif`, `serif`.

### ❌ CSS3 Properties
Any CSS3 properties (flexbox, grid, transforms, transitions, etc.) are not supported.

## Supported CSS Properties

The following properties **ARE SAFE TO USE**:

### ✅ Colors
```kotlin
addRule("pre { background-color: #2b2b2b; }")
addRule("code { color: #a9b7c6; }")
```

### ✅ Font Properties (Generic)
```kotlin
addRule("pre { font-family: monospace; }")
addRule("body { font-family: sans-serif; }")
addRule("h3 { font-size: 12px; }")
addRule("h4 { font-weight: bold; }")
```

### ✅ Margins
```kotlin
addRule("pre { margin-top: 8px; margin-bottom: 8px; }")
addRule("body { margin-left: 12px; margin-right: 12px; }")
```

### ✅ Basic Text Properties
```kotlin
addRule("h3 { text-align: center; }")
addRule("p { text-decoration: underline; }")
```

## Working Solution

Here's the **safe** CSS code that works without throwing exceptions:

```kotlin
val css = StyleSheet().apply {
    // Only use CSS properties that Swing's StyleSheet definitely supports
    addRule("pre { background-color: #2b2b2b; color: #a9b7c6; font-family: monospace; font-size: 11px; margin-top: 8px; margin-bottom: 8px; }")
    addRule("code { background-color: #2b2b2b; color: #a9b7c6; font-family: monospace; font-size: 11px; }")
    addRule("body { font-family: sans-serif; font-size: 11px; }")
    addRule("h3 { font-size: 12px; font-weight: bold; }")
    addRule("h4 { font-size: 11px; font-weight: bold; }")
}
```

## Better Alternative: EditorEx

For true syntax highlighting with full CSS/styling control, use IntelliJ's `EditorEx` instead of `JEditorPane`:

```kotlin
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory

fun createHighlightedEditor(code: String, fileType: FileType, project: Project): EditorEx {
    val editorFactory = EditorFactory.getInstance()
    val document = editorFactory.createDocument(code)
    val editor = editorFactory.createEditor(document, project) as EditorEx

    editor.isViewer = true
    editor.settings.apply {
        isLineNumbersShown = true
        isLineMarkerAreaShown = false
    }

    // Apply true syntax highlighting
    val highlighter = EditorHighlighterFactory.getInstance()
        .createEditorHighlighter(project, fileType)
    editor.highlighter = highlighter

    return editor
}
```

This approach gives you:
- ✅ True syntax highlighting matching IDE theme
- ✅ Full font control (JetBrains Mono, etc.)
- ✅ Proper padding, borders, and styling
- ✅ Theme integration (automatic light/dark mode)

## References

- [Swing StyleSheet Documentation](https://docs.oracle.com/javase/8/docs/api/javax/swing/text/html/StyleSheet.html)
- [IntelliJ Platform Editor Documentation](https://plugins.jetbrains.com/docs/intellij/editor-basics.html)
- File: `src/main/kotlin/com/valtecna/iadoc/ui/CodeHighlightedDocPanel.kt:42`

## Summary

**For Production Code with JEditorPane:**
- Use ONLY the safe CSS properties listed above
- Avoid `padding`, `border-radius`, specific font names, and all CSS3
- Test thoroughly after any CSS changes

**For Future Enhancements:**
- Migrate to `EditorEx` for full styling control and true syntax highlighting
- This will be implemented in Phase 2 of the syntax highlighting feature
