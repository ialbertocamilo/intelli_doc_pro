# IntelliDoc Professional

**AI-powered code documentation and analysis for IntelliJ IDEA**

[![JetBrains Marketplace](https://img.shields.io/badge/JetBrains-Marketplace-blue.svg)](https://plugins.jetbrains.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0-green.svg)](https://github.com/valtecna/intellidoc-professional)

---

## 🚀 Features

### 📝 AI-Powered Documentation
- **Instant Documentation**: Right-click any function, class, or method to generate comprehensive documentation
- **Multi-LLM Support**: Choose between OpenAI, Groq, or AWS Bedrock
- **Context-Aware**: Analyzes dependencies, parameters, return types, and usage patterns
- **Usage Examples**: Automatically generates code examples with best practices

### 🔍 Real-Time Code Analysis

#### ⚙️ Complexity Analysis (9 languages)
- Detects nested loops, conditionals, and complex logic
- Shows Big-O time complexity in editor
- Supports: Java, Kotlin, Python, TypeScript, JavaScript, Rust, PHP, C++, Go

#### 🔐 Security Analysis (Java, Kotlin)
- SQL Injection detection
- XSS vulnerability warnings
- Hardcoded credentials detection
- Insecure deserialization checks
- Command injection warnings

#### ⚡ Performance Analysis (Java, Kotlin)
- N+1 Query detection
- Inefficient collection operations (Kotlin sequences)
- Memory leak risks (listeners, threads, GlobalScope)
- Main thread blocking warnings
- Large allocation detection
- Boxing overhead identification

### 🎨 Syntax-Highlighted Output
- Beautiful HTML documentation with code highlighting
- Resizable, movable popup windows
- Markdown support for formatted output

### 🌍 Multi-Language Support
Java • Kotlin • Python • TypeScript • JavaScript • Rust • PHP • C++ • Go

---

## 📦 Installation

### From JetBrains Marketplace (Recommended)

1. Open **Settings/Preferences** → **Plugins**
2. Search for **"IntelliDoc Professional"**
3. Click **Install**
4. Restart IDE
5. Configure your LLM provider in **Settings → Tools → IntelliDoc Professional**

### Manual Installation

1. Download the latest release from [Releases](https://github.com/valtecna/intellidoc-professional/releases)
2. Open **Settings/Preferences** → **Plugins** → ⚙️ → **Install Plugin from Disk**
3. Select the downloaded `.zip` file
4. Restart IDE

---

## ⚙️ Configuration

### 1. Open Settings
**Settings/Preferences → Tools → IntelliDoc Professional**

### 2. Choose LLM Provider

#### OpenAI
- **API Key**: Your OpenAI API key ([Get one here](https://platform.openai.com/api-keys))
- **Model**: `gpt-4o`, `gpt-4-turbo`, or `gpt-3.5-turbo`

#### Groq
- **API Key**: Your Groq API key ([Get one here](https://console.groq.com/keys))
- **Model**: `llama-3.3-70b-versatile`, `mixtral-8x7b-32768`, etc.

#### AWS Bedrock
- **API Key**: Format `ACCESS_KEY_ID:SECRET_ACCESS_KEY`
- **Region**: e.g., `us-east-1`, `us-west-2`
- **Model**: `anthropic.claude-3-5-sonnet-20241022-v2:0`

See [BEDROCK_API_KEY_FORMAT.md](BEDROCK_API_KEY_FORMAT.md) for detailed Bedrock setup.

### 3. Configure Hints (Optional)

Toggle real-time code analysis features:
- ☑️ Complexity hints
- ☑️ Security hints
- ☑️ Performance hints

---

## 🎯 Usage

### Generate Documentation

**Method 1: Context Menu**
1. Place cursor on any function/class/method
2. Right-click → **"View IntelliDoc Professional"**
3. Documentation appears in popup

**Method 2: Keyboard Shortcut**
- Windows/Linux: `Ctrl + Shift + J`
- macOS: `Cmd + Shift + J`

### View Real-Time Hints

Code analysis hints appear automatically above functions:

```java
⚙️ Complexity: O(n²) - nested loops (depth 2)

🔴 Security: SQL Injection - Use PreparedStatement

🔴 Performance: N+1 Query detected - Use JOIN or batch loading

public List<Order> getUserOrders(List<User> users) {
    // Your code here
}
```

---

## 💰 Pricing

- **Trial**: 7 days free (full features)
- **Paid**: $8/month or $80/year via JetBrains Marketplace
- **Enterprise**: Contact for volume licensing

---

## 📚 Documentation

- [Installation Guide](docs/installation.md)
- [Configuration Guide](docs/configuration.md)
- [Bedrock Setup](BEDROCK_API_KEY_FORMAT.md)
- [Performance Hints Examples](PERFORMANCE_HINTS_EXAMPLES.md)
- [Marketplace Registration](MARKETPLACE_REGISTRATION_STEPS.md)
- [Monetization Guide](MONETIZATION_IMPLEMENTATION_GUIDE.md)

---

## 🔐 Privacy & Security

- **API Keys**: Stored locally, never transmitted to Valtecna
- **Code Analysis**: Performed locally in your IDE
- **LLM Requests**: Sent directly to your chosen provider (OpenAI/Groq/AWS)
- **No Telemetry**: Your code never leaves your control

---

## 🛠️ Development

### Requirements
- Java 21+
- IntelliJ IDEA 2025.1+
- Gradle 8.10+

### Build from Source

```bash
git clone https://github.com/valtecna/intellidoc-professional.git
cd intellidoc-professional
./gradlew buildPlugin
```

Build output: `build/distributions/IntelliDoc-*.zip`

### Run in Development Mode

```bash
./gradlew runIde
```

---

## 📝 License

**Proprietary Software** - All Rights Reserved

Copyright (c) 2025 Valtecna

This software is proprietary and confidential. Unauthorized copying, distribution, or use is strictly prohibited. See [LICENSE](LICENSE) for full terms.

### Third-Party Components

This software uses the following open-source libraries:
- AWS SDK for Java (Apache 2.0)
- Gson (Apache 2.0)
- Jsoup (MIT)
- IntelliJ Platform SDK (Apache 2.0)

See [NOTICE](NOTICE) for complete attribution.

---

## 🤝 Support

- **Documentation**: [docs/](docs/)
- **Issues**: [GitHub Issues](https://github.com/valtecna/intellidoc-professional/issues)
- **Email**: svg.z32@gmail.com
- **Location**: Arequipa, Peru
- **Marketplace**: [JetBrains Plugin Page](https://plugins.jetbrains.com/)

---

## 🎓 Examples

### Java Documentation Example

**Input:**
```java
public List<User> findActiveUsers(String role, int limit) {
    return userRepository.findByRoleAndActive(role, true)
        .stream()
        .limit(limit)
        .collect(Collectors.toList());
}
```

**Output:**
```markdown
## 📖 Method: findActiveUsers

**Purpose**: Retrieves active users filtered by role with a specified limit.

**Parameters**:
- `role` (String): The user role to filter by
- `limit` (int): Maximum number of users to return

**Returns**: `List<User>` - List of active users matching the role

**Time Complexity**: O(n) where n is the total number of users

**Usage Example**:
```java
List<User> admins = findActiveUsers("ADMIN", 10);
admins.forEach(user -> System.out.println(user.getName()));
```

**Edge Cases**:
- Returns empty list if no users match criteria
- Limit of 0 returns empty list
- Null role throws NullPointerException

**Recommendations**:
- Add null check for role parameter
- Consider pagination for large datasets
- Cache results if called frequently
```

---

## 📊 Supported Models

### OpenAI
- GPT-4o (Recommended)
- GPT-4 Turbo
- GPT-3.5 Turbo

### Groq
- Llama 3.3 70B (Recommended)
- Mixtral 8x7B
- Llama 3.1 8B

### AWS Bedrock
- Claude 3.5 Sonnet v2 (Recommended)
- Claude 3.5 Sonnet v1
- Claude 3 Sonnet
- Claude 3 Haiku

---

## 🚦 Roadmap

See [ADDITIONAL_HINTS_ROADMAP.md](ADDITIONAL_HINTS_ROADMAP.md) for upcoming features.

### Phase 1 (✅ Completed)
- ✅ Complexity Analysis (9 languages)
- ✅ Security Analysis (Java, Kotlin)
- ✅ Performance Analysis (Java, Kotlin)

### Phase 2 (🔜 Planned)
- 🔜 Resource Management Hints
- 🔜 Null Safety Hints
- 🔜 More language support

---

## 🌟 Why IntelliDoc Professional?

| Feature | IntelliDoc Pro | Traditional Docs | IDE Built-in |
|---------|---------------|------------------|--------------|
| AI-Generated Docs | ✅ | ❌ | ❌ |
| Real-time Hints | ✅ | ❌ | 🟡 Limited |
| Multi-LLM Support | ✅ | N/A | ❌ |
| Security Analysis | ✅ | ❌ | 🟡 Basic |
| Performance Tips | ✅ | ❌ | ❌ |
| 9 Languages | ✅ | Varies | ✅ |
| Contextual Examples | ✅ | ❌ | ❌ |

---

## 📞 Contact

**Valtecna**
Website: https://www.valtecna.com
Email: svg.z32@gmail.com
Location: Arequipa, Peru
GitHub: https://github.com/valtecna/intellidoc-professional

---

**Made with ❤️ in Arequipa, Peru 🇵🇪**
