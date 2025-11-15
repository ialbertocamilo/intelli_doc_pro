package com.valtecna.iadoc.services

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import com.valtecna.iadoc.Constants
import com.valtecna.iadoc.cache.DocumentationCache
import com.valtecna.iadoc.llm.LLMService
import java.awt.Color

class HTMLGenerator(private val llm: LLMService) {

    private val cache = DocumentationCache.getInstance()

    fun generate(context: String, pro: Boolean): String {
        cache.get(context, pro)?.let { return it }

        val raw = llm.generateDocumentation(context, pro)
        val sanitized = sanitize(raw)

        cache.put(context, pro, sanitized)

        return sanitized
    }

    private fun toHex(color: Color): String {
        return String.format("#%02x%02x%02x", color.red, color.green, color.blue)
    }

    private fun sanitize(html: String): String {
        var s = html
        s = s.replace(Regex("\\n+"), "\n")
        s = s.replace(Regex("\\sstyle=\"[^\"]*\"", RegexOption.IGNORE_CASE), "")
        s = s.replace(Regex("<pre[^>]*>", RegexOption.IGNORE_CASE), "<pre>")
        s = s.replace(Regex("<!--[\\s\\S]*?-->", RegexOption.IGNORE_CASE), "")
        val bgColor = toHex(JBColor.background())
        val fgColor = toHex(JBColor.foreground())
        val codeBgColor = toHex(JBColor(Color(240, 240, 240), Color(50, 50, 50)))
        val preBgColor = toHex(JBColor(Color(245, 245, 245), Color(43, 43, 43)))
        val borderColor = toHex(JBColor(Color(221, 221, 221), Color(80, 80, 80)))
        val linkColor = toHex(JBColor(Color(33, 150, 243), Color(100, 181, 246)))
        val headingColor = toHex(JBColor(Color(30, 30, 30), Color(220, 220, 220)))

        val sectionTitleColor = toHex(JBColor(Color(70, 130, 180), Color(135, 206, 250)))  // Steel blue / Light sky blue

        val style = """
            <style>
                body {
                    padding: ${Constants.Style.BODY_PADDING};
                    font-family: ${Constants.Style.FONT_FAMILY};
                    font-size: ${Constants.Style.FONT_SIZE};
                    color: $fgColor;
                    background-color: $bgColor;
                    margin: 0;
                    max-width: 100%;
                    width: 100%;
                    box-sizing: border-box;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                * {
                    box-sizing: border-box;
                }
                h3 {
                    margin-top: 0;
                    margin-bottom: 8px;
                    font-size: ${Constants.Style.HEADING_SIZE};
                    font-weight: bold;
                    color: $headingColor;
                    border-bottom: 1px solid $borderColor;
                    padding-bottom: 5px;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                h4 {
                    margin-top: 10px;
                    margin-bottom: 5px;
                    font-size: ${Constants.Style.FONT_SIZE};
                    font-weight: bold;
                    color: $headingColor;
                }
                .section-title {
                    color: $sectionTitleColor;
                    font-weight: bold;
                }
                .element-sig {
                    font-family: monospace;
                    background-color: $codeBgColor;
                    padding: 2px 6px;
                    font-size: ${Constants.Style.CODE_SIZE};
                    border-radius: 3px;
                    border: 1px solid $borderColor;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                    display: inline-block;
                    max-width: 100%;
                }
                p {
                    margin-top: 0;
                    margin-bottom: 6px;
                    font-weight: normal;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                ul {
                    margin-top: 0;
                    margin-bottom: 6px;
                    padding-left: 18px;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                ol {
                    margin-top: 0;
                    margin-bottom: 6px;
                    padding-left: 18px;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                li {
                    margin-top: 2px;
                    margin-bottom: 2px;
                    font-weight: normal;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                code {
                    font-family: monospace;
                    background-color: $codeBgColor;
                    padding: 1px 3px;
                    font-size: ${Constants.Style.CODE_SIZE};
                    border-radius: 2px;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                pre {
                    background-color: $preBgColor;
                    padding: 8px;
                    border: 1px solid $borderColor;
                    border-radius: 4px;
                    margin-top: 0;
                    margin-bottom: 8px;
                    font-family: monospace;
                    font-size: ${Constants.Style.CODE_SIZE};
                    white-space: pre-wrap;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                    overflow-x: auto;
                    max-width: 100%;
                }
                a {
                    color: $linkColor;
                    text-decoration: none;
                    font-weight: normal;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                table {
                    margin-top: 0;
                    margin-bottom: 8px;
                    width: 100%;
                    table-layout: auto;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                td {
                    padding: 3px 5px;
                    text-align: left;
                    border-bottom: 1px solid $borderColor;
                    font-weight: normal;
                    font-size: ${Constants.Style.FONT_SIZE};
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                th {
                    padding: 3px 5px;
                    text-align: left;
                    border-bottom: 1px solid $borderColor;
                    font-weight: normal;
                    color: $headingColor;
                    font-size: ${Constants.Style.FONT_SIZE};
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                blockquote {
                    margin-top: 0;
                    margin-bottom: 8px;
                    padding: 5px 8px;
                    border-left: 2px solid $linkColor;
                    background-color: $codeBgColor;
                    font-weight: normal;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                strong, b {
                    font-weight: bold;
                    color: $headingColor;
                }
            </style>
        """.trimIndent()

        if (s.contains("<head", ignoreCase = true)) {
            s = s.replaceFirst(Regex("<head>", RegexOption.IGNORE_CASE), "<head>$style")
        } else if (s.contains("<html", ignoreCase = true)) {
            val idx = s.indexOf('>')
            if (idx != -1) {
                s = s.substring(0, idx + 1) + "<head>$style</head>" + s.substring(idx + 1)
            } else {
                s = "<html><head>$style</head>" + s + "</html>"
            }
        } else {
            s = "<html><head>$style</head><body>" + s + "</body></html>"
        }
        return s
    }
}
