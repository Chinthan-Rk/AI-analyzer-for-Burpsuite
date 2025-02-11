```markdown
# AI Analyzer for Burp Suite

AI Analyzer is a Burp Suite extension that integrates AI capabilities into your web application security testing workflow. The extension analyzes HTTP requests and responses using AI models to provide insights about potential security issues.

## Current Features

The extension provides automated analysis of HTTP traffic using advanced AI models:

- **Real-time Analysis**: Analyze intercepted requests and responses directly within Burp Suite
- **Claude AI Integration**: 
  - Full support for Claude's API
  - Intelligent analysis of HTTP traffic
  - Customizable analysis prompts
- **Integrated User Interface**: 
  - Dedicated extension tab in Burp Suite
  - Easy-to-use request/response viewer
  - Results table showing analysis findings
- **Analysis Types**:
  - Vulnerability scanning
  - Security header analysis
  - Custom analysis with your own prompts

## Features Under Development

Please note that the following features are currently in development and not yet functional:

1. **AI Provider Integration**:
   - OpenAI integration is planned but not yet implemented
   - Custom AI provider support is in development
   - The architecture supports multiple providers, but only Claude is currently functional

2. **Analysis Capabilities**:
   - Real-time scanning during proxy intercept is planned
   - Batch analysis of multiple requests is under development
   - Export functionality for analysis results is coming soon

3. **User Interface Enhancements**:
   - Customizable scanning rules interface
   - Advanced filtering of results
   - Detailed vulnerability reporting templates

We are actively working on these features and will update the documentation as they become available. For now, the extension functions primarily with Claude's AI capabilities for individual request analysis.

## Installation

1. Download the latest JAR file from the [Releases](https://github.com/Chinthan-Rk/AI-analyzer-for-Burpsuite/releases) page
2. Open Burp Suite Professional
3. Navigate to the "Extender" tab
4. Click "Add" in the "Extensions" tab
5. Select "Java" as the extension type
6. Choose the downloaded JAR file
7. The extension will appear as "AI Analyzer" in Burp's interface

## Configuration

After installation:

1. Go to the "AI Analyzer" tab in Burp Suite
2. Select your AI provider (Currently only Claude is supported)
3. Enter your API key:
   - For Claude: Get your API key from [Anthropic](https://anthropic.com)
4. Choose your analysis type:
   - Vulnerability Scan
   - Security Headers Check
   - Custom Prompt

## Usage

The extension integrates into your normal Burp Suite workflow:

1. Analyzing Requests:
   - Capture a request in Burp's Proxy
   - Right-click and select "Send to AI Analyzer"
   - The request will appear in the AI Analyzer tab

2. Viewing Results:
   - Results appear in the response table
   - Each analysis shows:
     - Analysis type used
     - AI model response
     - Timestamp of analysis

## Development

Built using Java and the Burp Suite Extender API.

### Building from Source

Prerequisites:
- Java JDK 22
- Maven
- Burp Suite Professional

Build steps:
```bash
git clone https://github.com/Chinthan-Rk/AI-analyzer-for-Burpsuite.git
cd AI-analyzer-for-Burpsuite
mvn clean package
```

The compiled JAR will be in the `target` directory.

### Project Structure

```
src/
├── main/
│   └── java/
│       └── org/
│           └── example/
│               ├── gui/        # UI Components
│               │   ├── table/  # Request/Response Tables
│               │   └── viewer/ # Message Viewer
│               ├── http/       # AI Provider Integration
│               └── model/      # Data Models
```

## Issue Reporting

If you encounter any bugs or issues:
1. Check existing issues in the GitHub repository
2. If your issue isn't already reported, create a new issue
3. Provide detailed information:
   - Steps to reproduce the problem
   - Expected behavior
   - Actual behavior
   - Error messages if any
   - Your environment details (Burp Suite version, Java version)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
```
