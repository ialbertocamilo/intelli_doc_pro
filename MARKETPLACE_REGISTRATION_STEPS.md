# IntelliDoc Professional - JetBrains Marketplace Registration

## ⚠️ Current Issue

**Error**: "Unknown product code provided PVALDOC"

**Cause**: The product code `PVALDOC` is not registered in JetBrains Marketplace system yet.

**Solution**: Follow the registration steps below to get your product code approved.

---

## 📝 Step-by-Step Registration Process

### 1. Create JetBrains Marketplace Account

1. Go to https://plugins.jetbrains.com/
2. Click "Sign In" (top right)
3. Sign in with your JetBrains Account (or create one)
4. Complete your vendor profile

### 2. Upload Your Plugin (Free Version First)

**Important**: Upload as a **FREE** plugin first, then apply for paid status later.

1. Go to https://plugins.jetbrains.com/author/me
2. Click "Add new plugin"
3. Fill in the form:
   - **Plugin Name**: IntelliDoc Professional
   - **Plugin ID**: com.valtecna.IaDoc (from your plugin.xml)
   - **Category**: Code tools / Documentation
   - **License**: Paid (after approval)

4. Upload your plugin JAR:
```bash
./gradlew buildPlugin
# JAR will be in: build/distributions/IaDoc-*.zip
```

5. Add description, screenshots, tags
6. Submit for review

### 3. Apply for Paid Plugin Status

**After your free plugin is approved:**

1. Contact JetBrains Marketplace team: marketplace@jetbrains.com

2. **Email Template**:
```
Subject: Application for Paid Plugin Status - IntelliDoc Professional

Hello JetBrains Marketplace Team,

I would like to apply for paid plugin status for my plugin:

Plugin Name: IntelliDoc Professional
Plugin ID: com.valtecna.IaDoc
Plugin URL: [Your plugin URL from marketplace]
Vendor: Valtecna

Desired Product Code: PVALDOC (or assign one if this is taken)

Monetization Model:
- Paid plugin with 7-day trial period
- Subscription-based pricing

Plugin Description:
AI-powered documentation generator with advanced code analysis features:
- Multi-language support (Java, Kotlin, Python, TypeScript, JavaScript, Rust, PHP, C++)
- Real-time complexity analysis with inlay hints
- Security vulnerability detection
- Performance optimization suggestions
- Multi-LLM support (OpenAI, Groq, AWS Bedrock)

Expected Pricing: $8/month or $80/year

Please let me know the next steps to enable monetization for this plugin.

Thank you,
[Your Name]
```

### 4. Wait for Approval

**Timeline**: Usually 3-7 business days

JetBrains will:
- Review your plugin
- Assign or confirm your product code
- Enable monetization in your marketplace dashboard
- Provide you with testing instructions

### 5. Configure Pricing

Once approved:

1. Log in to JetBrains Marketplace dashboard
2. Go to your plugin settings
3. Navigate to "Monetization" section
4. Configure:
   - Product code (provided by JetBrains)
   - Pricing tiers
   - Trial period (30 days standard)
   - Subscription options

### 6. Enable Product Descriptor

After JetBrains confirms your product code:

1. Uncomment the `product-descriptor` in `plugin.xml`:
```xml
<product-descriptor
    code="PVALDOC"  <!-- Or the code JetBrains assigned -->
    release-date="20250118"
    release-version="20251"
    optional="false"
    eap="false"/>
```

2. Update the code if JetBrains assigned a different one

3. Rebuild and reupload your plugin

---

## 🧪 Testing Before Going Live

### Local Development Testing

**While waiting for approval**, you can test the licensing logic:

1. **Keep product-descriptor commented out** (current state)
2. **Mock the license check** for testing:

```kotlin
// In LicenseChecker.kt - ADD THIS TEMPORARY CODE FOR TESTING
object LicenseChecker {
    private const val PLUGIN_PRODUCT_CODE = "PVALDOC"
    private const val KEY_PREFIX = "key:"
    private const val STAMP_PREFIX = "stamp:"

    // TEMPORARY: Set to true to test "Pro" features during development
    private const val MOCK_LICENSE_FOR_TESTING = true

    fun isLicensed(): Boolean? {
        // REMOVE THIS BLOCK BEFORE PRODUCTION
        if (MOCK_LICENSE_FOR_TESTING) {
            return true  // Simulates valid license
        }

        val facade = LicensingFacade.getInstance() ?: return null
        val confirmationStamp = facade.getConfirmationStamp(PLUGIN_PRODUCT_CODE) ?: return false

        return when {
            confirmationStamp.startsWith(KEY_PREFIX) -> true
            confirmationStamp.startsWith(STAMP_PREFIX) -> true
            else -> false
        }
    }

    fun isPro(): Boolean = isLicensed() == true
    fun isFree(): Boolean = isLicensed() != true
}
```

⚠️ **IMPORTANT**: Remove `MOCK_LICENSE_FOR_TESTING` before releasing to production!

### Testing After Approval

Once JetBrains approves your product code:

1. Enable product-descriptor in plugin.xml
2. Set `MOCK_LICENSE_FOR_TESTING = false`
3. Test with real JetBrains Account license
4. Test trial activation
5. Test license expiration
6. Test purchase flow

---

## 🚀 Alternative: Start with Freemium Model

If you want to launch faster, consider **Freemium** instead of fully paid:

### Freemium Benefits:
- ✅ No approval delay (launches immediately)
- ✅ Easier user acquisition
- ✅ Users can try before buying
- ✅ Better for building community

### Freemium Configuration:

1. Change `product-descriptor`:
```xml
<product-descriptor
    code="PVALDOC"
    release-date="20250118"
    release-version="20251"
    optional="true"  <!-- Changed to true for freemium -->
    eap="false"/>
```

2. Define free vs paid features:
```kotlin
// Free Features (No license required):
- Basic AI documentation
- Complexity hints (limited)

// Pro Features (License required):
- Advanced complexity analysis
- Security hints
- Performance hints
- Multi-LLM support
- Priority support
```

3. Update settings message:
```kotlin
false -> {
    licenseStatusLabel.text = "Free tier - Upgrade to Pro for advanced features"
}
```

---

## 📊 Timeline Comparison

| Approach | Time to Launch | Complexity | Recommendation |
|----------|---------------|------------|----------------|
| **Free Plugin** | Immediate | Low | ✅ Start here |
| **Freemium** | 1-2 weeks | Medium | ✅ Good for growth |
| **Fully Paid with Trial** | 3-7 weeks | High | Later, once established |

---

## 🎯 Recommended Launch Strategy

### Phase 1: Free Launch (Week 1)
1. Comment out product-descriptor
2. Launch as free plugin on marketplace
3. Gather users and feedback
4. Build community

### Phase 2: Apply for Paid Status (Week 2-3)
1. Apply for paid plugin status
2. Wait for JetBrains approval
3. Get product code assigned

### Phase 3: Enable Monetization (Week 4+)
1. Uncomment product-descriptor
2. Configure pricing
3. Test trial flow
4. Launch paid version
5. Notify existing users about upgrade options

---

## 📞 Contact Information

- **Marketplace Support**: marketplace@jetbrains.com
- **General Support**: https://intellij-support.jetbrains.com/hc/en-us
- **Plugin Portal**: https://plugins.jetbrains.com/

---

## ✅ Current Status

- [x] Code implementation complete
- [x] Product descriptor configured (commented for development)
- [ ] Marketplace account created
- [ ] Plugin uploaded (free version)
- [ ] Applied for paid status
- [ ] Product code approved by JetBrains
- [ ] Pricing configured
- [ ] Trial flow tested
- [ ] Production launch

---

## 🔑 Key Takeaway

**You cannot test the paid licensing features until JetBrains approves your product code.**

**Next Steps:**
1. Keep product-descriptor commented during development
2. Upload plugin as FREE to marketplace first
3. Apply for paid status
4. Wait for approval
5. Enable product-descriptor with approved code
6. Test and launch

This is the standard process all paid IntelliJ plugins must follow.
