# AWS Bedrock Models Reference

## Available Models

The IaDoc plugin now supports configurable AWS Bedrock models and regions through the settings UI.

### Claude Models (Anthropic)

#### Claude 3.5 Sonnet v2 ⭐ (Default)
- **Model ID**: `anthropic.claude-3-5-sonnet-20241022-v2:0`
- **Context Window**: 200K tokens
- **Best For**: General documentation, complex code analysis
- **Pricing**: $3/M input tokens, $15/M output tokens

#### Claude 3.5 Sonnet
- **Model ID**: `anthropic.claude-3-5-sonnet-20240620-v1:0`
- **Context Window**: 200K tokens
- **Best For**: Similar to v2, previous version

#### Claude 3 Opus
- **Model ID**: `anthropic.claude-3-opus-20240229-v1:0`
- **Context Window**: 200K tokens
- **Best For**: Highest quality, most detailed documentation
- **Pricing**: $15/M input tokens, $75/M output tokens
- **Note**: Most expensive but highest quality

#### Claude 3 Haiku
- **Model ID**: `anthropic.claude-3-haiku-20240307-v1:0`
- **Context Window**: 200K tokens
- **Best For**: Fast, cost-effective documentation
- **Pricing**: $0.25/M input tokens, $1.25/M output tokens
- **Note**: Fastest and cheapest

### Llama Models (Meta)

#### Llama 3.2 90B Instruct
- **Model ID**: `meta.llama3-2-90b-instruct-v1:0`
- **Context Window**: 128K tokens
- **Best For**: Open-source alternative, good for code
- **Pricing**: Lower than Claude models

#### Llama 3.2 11B Instruct
- **Model ID**: `meta.llama3-2-11b-instruct-v1:0`
- **Context Window**: 128K tokens
- **Best For**: Lightweight, fast responses
- **Pricing**: Very cost-effective

### Amazon Titan Models

#### Titan Text Premier
- **Model ID**: `amazon.titan-text-premier-v1:0`
- **Context Window**: 32K tokens
- **Best For**: AWS-native solution
- **Pricing**: Competitive with other models

---

## AWS Regions

### us-east-1 (N. Virginia) ⭐ Recommended
- **Most models available**
- Lowest latency for US East Coast
- Best for general use

### us-west-2 (Oregon)
- Good availability
- Lower latency for US West Coast

### eu-central-1 (Frankfurt)
- European data residency
- GDPR compliance

### ap-southeast-1 (Singapore)
- Asia-Pacific coverage
- Good for APAC users

### ap-northeast-1 (Tokyo)
- Japan region
- Good for East Asia users

---

## Configuration in Plugin Settings

### Settings Location
1. Open IntelliJ IDEA Settings/Preferences
2. Navigate to **Tools** → **Documentation PRO**
3. Select **Bedrock** as LLM Provider

### Configuration Fields

#### 1. API Key (Optional)
- **Leave empty** to use AWS credentials chain (recommended)
- **Format**: `ACCESS_KEY:SECRET_KEY:REGION` for explicit credentials
- **Example**: `AKIAIOSFODNN7EXAMPLE:wJalrXUtnFEMI/K7MDENG/bPxRfiCY:us-east-1`

#### 2. Bedrock Region
- **Default**: `us-east-1`
- **Options**: See regions list above
- **Example**: `us-west-2`, `eu-central-1`

#### 3. Bedrock Model
- **Default**: `anthropic.claude-3-5-sonnet-20241022-v2:0`
- **Options**: See models list above
- **Example**: `anthropic.claude-3-haiku-20240307-v1:0` for faster/cheaper

---

## Model Selection Guide

### For Best Quality
**Claude 3 Opus**
```
Model: anthropic.claude-3-opus-20240229-v1:0
Region: us-east-1
```
- Most detailed documentation
- Best code understanding
- Highest cost

### For Balanced Performance (Recommended)
**Claude 3.5 Sonnet v2**
```
Model: anthropic.claude-3-5-sonnet-20241022-v2:0
Region: us-east-1
```
- Excellent quality
- Reasonable cost
- Good speed

### For Speed & Cost
**Claude 3 Haiku**
```
Model: anthropic.claude-3-haiku-20240307-v1:0
Region: us-east-1
```
- Fast responses
- Low cost
- Still good quality

### For Open Source
**Llama 3.2 90B**
```
Model: meta.llama3-2-90b-instruct-v1:0
Region: us-east-1
```
- No vendor lock-in
- Good code capabilities
- Lower cost

---

## Cost Comparison (per 1000 documentation requests)

Assuming average 500 input tokens + 1500 output tokens per request:

| Model | Input Cost | Output Cost | Total |
|-------|-----------|-------------|-------|
| **Claude 3 Opus** | $7.50 | $112.50 | **$120.00** |
| **Claude 3.5 Sonnet v2** | $1.50 | $22.50 | **$24.00** |
| **Claude 3 Haiku** | $0.13 | $1.88 | **$2.01** |
| **Llama 3.2 90B** | ~$0.50 | ~$2.00 | **~$2.50** |

---

## Model Availability by Region

| Model | us-east-1 | us-west-2 | eu-central-1 | ap-southeast-1 | ap-northeast-1 |
|-------|-----------|-----------|--------------|----------------|----------------|
| Claude 3.5 Sonnet v2 | ✅ | ✅ | ✅ | ✅ | ✅ |
| Claude 3 Opus | ✅ | ✅ | ✅ | ✅ | ✅ |
| Claude 3 Haiku | ✅ | ✅ | ✅ | ✅ | ✅ |
| Llama 3.2 90B | ✅ | ✅ | ✅ | ❌ | ❌ |
| Llama 3.2 11B | ✅ | ✅ | ✅ | ❌ | ❌ |
| Titan Text Premier | ✅ | ✅ | ✅ | ✅ | ✅ |

**Note**: Model availability may change. Check AWS Bedrock console for latest information.

---

## Examples

### Example 1: High Quality Documentation
```
LLM Provider: Bedrock
API Key: (leave empty for AWS credentials chain)
Bedrock Region: us-east-1
Bedrock Model: anthropic.claude-3-opus-20240229-v1:0
```

### Example 2: Fast & Cost-Effective
```
LLM Provider: Bedrock
API Key: (leave empty for AWS credentials chain)
Bedrock Region: us-east-1
Bedrock Model: anthropic.claude-3-haiku-20240307-v1:0
```

### Example 3: European Data Residency
```
LLM Provider: Bedrock
API Key: (leave empty for AWS credentials chain)
Bedrock Region: eu-central-1
Bedrock Model: anthropic.claude-3-5-sonnet-20241022-v2:0
```

### Example 4: Explicit Credentials
```
LLM Provider: Bedrock
API Key: AKIAIOSFODNN7EXAMPLE:wJalrXUtnFEMI/K7MDENG/bPxRfiCY:us-west-2
Bedrock Region: us-west-2
Bedrock Model: anthropic.claude-3-5-sonnet-20241022-v2:0
```

---

## Troubleshooting

### "Model not found" Error
- Check if model is available in your selected region
- Verify model ID is correct (no typos)
- Ensure you've requested access to the model in AWS Bedrock console

### "Access Denied" Error
- Verify your AWS credentials have `bedrock:InvokeModel` permission
- Check IAM policy includes the specific model ARN
- Ensure model access is enabled in Bedrock console

### High Costs
- Switch to Claude 3 Haiku for 10-12x cost reduction
- Use Llama models for open-source alternative
- Monitor usage in AWS Cost Explorer

---

## References

- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [Claude on Bedrock](https://docs.anthropic.com/claude/docs/claude-on-amazon-bedrock)
- [Bedrock Pricing](https://aws.amazon.com/bedrock/pricing/)
- [Model Access Guide](https://docs.aws.amazon.com/bedrock/latest/userguide/model-access.html)

---

**Last Updated**: 2025-11-14
**Plugin Version**: 1.1.0
