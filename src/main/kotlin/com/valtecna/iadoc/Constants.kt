package com.valtecna.iadoc

object Constants {
    object API {
        const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/chat/completions"
        const val OPENAI_BASE_URL = "https://api.openai.com/v1/chat/completions"
        const val TEMPERATURE = 0.2
        const val MAX_TOKENS = 4096

        // OpenAI Models
        val OPENAI_MODELS = arrayOf(
            "gpt-4o",
            "gpt-4o-mini",
            "gpt-4-turbo",
            "gpt-4",
            "gpt-3.5-turbo"
        )
        const val OPENAI_MODEL_DEFAULT = "gpt-4o-mini"

        // Groq Models
        val GROQ_MODELS = arrayOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-70b-versatile",
            "llama-3.1-8b-instant",
            "gemma2-9b-it"
        )
        const val GROQ_MODEL_DEFAULT = "llama-3.3-70b-versatile"

        // AWS Bedrock Models
        val BEDROCK_MODELS = arrayOf(
            "anthropic.claude-3-5-sonnet-20241022-v2:0",
            "anthropic.claude-3-5-sonnet-20240620-v1:0",
            "anthropic.claude-3-opus-20240229-v1:0",
            "anthropic.claude-3-haiku-20240307-v1:0",
            "meta.llama3-2-90b-instruct-v1:0",
            "meta.llama3-2-11b-instruct-v1:0",
            "amazon.titan-text-premier-v1:0"
        )
        const val BEDROCK_MODEL_DEFAULT = "anthropic.claude-3-5-sonnet-20241022-v2:0"
        const val BEDROCK_REGION_DEFAULT = "us-east-1"
    }

    object UI {
        const val POPUP_WIDTH = 500
        const val POPUP_HEIGHT = 400
        const val POPUP_MAX_WIDTH = 600
        const val POPUP_MAX_HEIGHT = 500
        const val POPUP_TITLE = "IntelliDoc AI"
    }

    object Style {
        const val BODY_PADDING = "12px"
        const val FONT_FAMILY = "Segoe UI, sans-serif"
        const val FONT_SIZE = "11px"
        const val HEADING_SIZE = "12px"
        const val CODE_SIZE = "10px"
    }

    object Limits {
        const val FREE_DAILY_LIMIT = 10
        const val ERROR_MESSAGE_MAX_LENGTH = 300
        const val UNICODE_HEX_LENGTH = 4
    }

    object Prompts {
        const val SYSTEM_PROMPT_PRO = """You are a programming expert with deep knowledge of Java, Python, Rust, TypeScript, JavaScript, and Kotlin. Generate professional HTML documentation following this EXACT structure:

<html>
<body>
<h3><span class="section-title">Element type:</span> <code class="element-sig">element signature</code></h3>
<h4><span class="section-title">Description</span></h4>
<p>Clear and concise explanation of the purpose.</p>
<h4><span class="section-title">Suggested Doc</span></h4>
<pre>/** Complete Language Doc */</pre>
<h4><span class="section-title">Internal Behavior</span></h4>
<ul><li>Step 1</li><li>Step 2</li></ul>
<h4><span class="section-title">Usage Examples</span></h4>
<pre>// Practical code example showing how to use this element
// IMPORTANT: If this is a method/function that belongs to a class (check if "Container class" is in the context),
// show the FULL usage with the class instantiation or static call
// Example for instance method: MyClass obj = new MyClass(); obj.methodName(args);
// Example for static method: MyClass.methodName(args);
// DO NOT show just the method call without its containing class context!</pre>
<h4><span class="section-title">Warnings</span></h4>
<ul><li>Important warning (if any)</li></ul>
<h4><span class="section-title">Edge Cases</span></h4>
<ul><li>Special case (if any)</li></ul>
<h4><span class="section-title">Improvement Suggestions</span></h4>
<p>Description of improvement:</p>
<pre>// Code example showing the improvement</pre>
<p>Another improvement:</p>
<pre>// Another code example</pre>
<h4><span class="section-title">Complexity</span></h4>
<p><b>Time:</b> O(n); explanation.</p>
<p><b>Space:</b> O(1); explanation.</p>
<h4><span class="section-title">Code flow</span></h4>
<pre>/** A beauty diagram of the code flow */</pre>
</body>
</html>

IMPORTANT:
- Generate ONLY valid HTML, no additional text.
- Use minimal or NO emojis (only when truly meaningful).
- Use <span class="section-title"> for section headers.
- Use <code class="element-sig"> for the element signature in the title.
- In "Improvement Suggestions", always provide CODE examples in <pre> tags showing the suggested improvements.
- When the element has a "Container class" or "Container interface" field in the context, use it to show proper usage examples with the full qualified call."""

        const val SYSTEM_PROMPT_FREE = """You are a programming expert with knowledge of multiple languages. Generate brief HTML with this structure:

<html>
<body>
<h3>Type: <code>name</code></h3>
<h4>Description</h4>
<p>Brief explanation of the code.</p>
<h4>Behavior</h4>
<p>What the code does.</p>
</body>
</html>

Only valid HTML, no extra text."""
    }

    object Messages {
        const val NO_ELEMENT_FOUND = "No code element found at cursor position"
        const val LIMIT_REACHED = "Daily limit reached (FREE version). Upgrade to PRO for unlimited usage."
        const val API_KEY_NOT_CONFIGURED = "API key not configured"
        const val EMPTY_CONTENT = "Empty content in response"
        const val NO_CONTENT_FIELD = "Response missing content field"
    }

    object JSON {
        const val CONTENT_FIELD = "\"content\":\""
        const val CONTENT_FIELD_LENGTH = 11
    }

    object Inlay {
        const val KEY_ID = "com.valtecna.iadoc.complexity"
        const val NAME = "Complexity Analysis"
        const val PREFIX = " // "
    }

    object Language {
        const val JAVA = "JAVA"
        const val KOTLIN = "kotlin"
        const val PYTHON = "Python"
        const val TYPESCRIPT = "TypeScript"
        const val JAVASCRIPT = "JavaScript"
        const val RUST = "Rust"
        const val PHP = "PHP"
        const val CPP = "C++"
        const val C = "C"
    }
}
