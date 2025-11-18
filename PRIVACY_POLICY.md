# Privacy Policy - IntelliDoc Professional

**Effective Date**: January 18, 2025
**Last Updated**: January 18, 2025

---

## Introduction

Valtecna ("we," "our," or "us") respects your privacy. This Privacy Policy explains how IntelliDoc Professional handles your information.

---

## What Information We Collect

### Information We DO NOT Collect

IntelliDoc Professional is designed with privacy in mind:

- ❌ **Your Source Code**: Never transmitted to Valtecna servers
- ❌ **API Keys**: Stored only locally on your machine
- ❌ **Personal Identifiable Information**: We don't collect names, emails, addresses
- ❌ **Usage Analytics**: We don't track how you use the plugin
- ❌ **Telemetry Data**: No analytics or tracking

### Information Stored Locally

The following information is stored on **your local machine only**:

- ✅ LLM Provider selection (OpenAI, Groq, or Bedrock)
- ✅ API Keys (encrypted by IntelliJ Platform)
- ✅ Model preferences
- ✅ Hint visibility preferences
- ✅ License status (managed by JetBrains)

**Location**: `~/.config/JetBrains/[IDE]/options/DocProSettings.xml`

---

## How Your Data is Used

### LLM Provider Communication

When you generate documentation:

1. **Your code snippet** is sent directly from your IDE to your chosen LLM provider (OpenAI, Groq, or AWS)
2. **We (Valtecna) never see this data** - it goes directly to the provider
3. The provider processes your request and returns documentation
4. The response is displayed in your IDE

**Data Flow**:
```
Your IDE → [Your API Key] → LLM Provider (OpenAI/Groq/AWS)
          ← Documentation Response ←
```

**Valtecna is NOT in this flow.**

### Third-Party Services

You are responsible for reviewing the privacy policies of your chosen LLM provider:

- **OpenAI**: https://openai.com/policies/privacy-policy
- **Groq**: https://groq.com/privacy-policy
- **AWS Bedrock**: https://aws.amazon.com/privacy/

---

## License Verification

IntelliDoc Professional uses **JetBrains Marketplace licensing system**:

- ✅ License status is verified via JetBrains servers
- ✅ Your JetBrains Account email may be used for license validation
- ✅ Managed entirely by JetBrains, not Valtecna
- ✅ See JetBrains Privacy Policy: https://www.jetbrains.com/legal/docs/privacy/privacy/

---

## Data Security

### Local Storage Security

- API keys are stored using IntelliJ Platform's secure credential storage
- Settings files are protected by your operating system's file permissions
- No data is transmitted to Valtecna servers

### Network Security

- All communications with LLM providers use HTTPS/TLS encryption
- API keys are sent via secure headers
- We recommend using environment variables for sensitive credentials

---

## Your Rights

You have the right to:

- ✅ **Access**: View all settings stored locally
- ✅ **Delete**: Uninstall the plugin to remove all local data
- ✅ **Control**: Choose which LLM provider to use
- ✅ **Opt-out**: Disable any hint features you don't want

---

## Children's Privacy

IntelliDoc Professional is not directed to children under 13. We do not knowingly collect information from children.

---

## Changes to This Policy

We may update this Privacy Policy from time to time. Changes will be posted on this page with an updated "Last Updated" date.

---

## Data Retention

- **Local Settings**: Retained until you uninstall the plugin
- **API Keys**: Stored locally until you remove them
- **Generated Documentation**: Not stored by the plugin (temporary display only)

---

## International Users

IntelliDoc Professional can be used worldwide. Your data is processed locally on your machine. When using LLM providers, your data may be transmitted internationally according to the provider's data handling practices.

---

## GDPR Compliance (EU Users)

Under GDPR:
- We are NOT a data controller (we don't collect personal data)
- Your LLM provider may be a data processor
- You have rights under GDPR with respect to your LLM provider

---

## CCPA Compliance (California Users)

We do not sell personal information. We do not collect personal information for sale.

---

## Contact Information

For privacy-related questions:

**Valtecna**
Email: svg.z32@gmail.com
Location: Arequipa, Peru
Website: https://www.valtecna.com

---

## Third-Party Libraries

IntelliDoc Professional uses open-source libraries. Their privacy implications:

- **AWS SDK**: Facilitates AWS communication (your credentials)
- **Gson**: Local JSON processing (no external communication)
- **Jsoup**: Local HTML parsing (no external communication)

---

## Consent

By installing and using IntelliDoc Professional, you consent to this Privacy Policy.

---

**Summary in Plain English:**

- 🔒 Your code stays on your machine
- 🔑 API keys stored locally (not sent to us)
- 📡 Documentation requests go directly to your LLM provider
- 🚫 We don't track you or collect analytics
- ✅ You control everything

**We built this plugin to respect your privacy.**
