package org.example.http;

import okhttp3.*;
import com.google.gson.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClaudeProvider implements AIProvider {
    // Core API configuration
    private static final String API_ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private final OkHttpClient client;
    private String apiKey;
    private String lastProcessingSummary;  // Add this field


    // Headers that might contain sensitive data that we should handle carefully
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "cookie",
            "authorization",
            "proxy-authorization",
            "x-csrf-token"
    );

    // Inner class to track what modifications we make to the request/response
    private static class ProcessingMetadata {
        List<String> redactedHeaders = new ArrayList<>();
        boolean bodyTruncated = false;
        Map<String, String> headerValues = new HashMap<>();
        long originalSize = 0;
        long processedSize = 0;
        boolean isSecurityHeaderCheck = false;  // New flag to track analysis type
    }

    public ClaudeProvider() {
        // Create an OkHttpClient with custom timeout settings
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)     // Time to establish the connection
                .writeTimeout(30, TimeUnit.SECONDS)       // Time to write data to the server
                .readTimeout(60, TimeUnit.SECONDS)        // Time to read the response
                .retryOnConnectionFailure(true)          // Automatically retry on connection failures
                .build();

    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getLastProcessingSummary() {
        return lastProcessingSummary;
    }

    private String handleApiResponse(Response apiResponse) throws IOException {
        String responseBody = apiResponse.body().string();

        // First, log the raw response for debugging
        System.out.println("Raw API Response: " + responseBody);

        try {
            if (!apiResponse.isSuccessful()) {
                // Handle error response
                try {
                    JsonObject errorJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    String errorMessage = "API call failed: " + apiResponse.code();
                    if (errorJson.has("error")) {
                        JsonObject error = errorJson.getAsJsonObject("error");
                        errorMessage += " - " + error.get("message").getAsString();
                    }
                    throw new RuntimeException(errorMessage);
                } catch (JsonSyntaxException e) {
                    // If we can't parse the error as JSON, return the raw error
                    throw new RuntimeException("API call failed: " + apiResponse.code() + " - " + responseBody);
                }
            }

            // Parse successful response
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            // Extract the text content from the response
            if (jsonResponse.has("content")) {
                JsonArray content = jsonResponse.getAsJsonArray("content");
                if (content != null && content.size() > 0) {
                    JsonObject firstContent = content.get(0).getAsJsonObject();
                    if (firstContent.has("text")) {
                        return firstContent.get("text").getAsString();
                    }
                }
            }

            // If we can't parse the expected structure, return a meaningful error
            throw new RuntimeException("Unexpected response format from API: " + responseBody);

        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to parse API response: " + responseBody, e);
        }
    }

    private String describeHeaderContent(String headerName, String value) {
        switch (headerName.toLowerCase()) {
            case "cookie":
                int cookieCount = value.split(";").length;
                return cookieCount + " cookies present";
            case "authorization":
                return "Bearer token or credentials present";
            case "proxy-authorization":
                return "Proxy credentials present";
            default:
                return "Sensitive data";
        }
    }

    @Override
    public String analyze(String request, String response, String analysisType) {
        // First, we process the request and response to handle sensitive data
        ProcessingMetadata requestMeta = new ProcessingMetadata();
        ProcessingMetadata responseMeta = new ProcessingMetadata();

        String processedRequest = processHttpMessage(request, requestMeta,analysisType);
        String processedResponse = processHttpMessage(response, responseMeta,analysisType);

        // Generate a summary of what we modified for transparency
        this.lastProcessingSummary = createProcessingSummary(requestMeta, responseMeta);

        // Generate a summary of what we modified for transparency
        String processingNotice = createProcessingSummary(requestMeta, responseMeta);
        System.out.println("=== Processing Summary ===");
        System.out.println(processingNotice);
        System.out.println("========================");

        // Build our prompt with the processed data and metadata
        String prompt = buildPrompt(processedRequest, processedResponse, analysisType, requestMeta, responseMeta);

        // Prepare the request to the Claude API
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", "claude-3-opus-20240229");
        jsonBody.addProperty("max_tokens", 1000);

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);
        jsonBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
        );

        Request apiRequest = new Request.Builder()
                .url(API_ENDPOINT)
                .post(body)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", API_VERSION)
                .addHeader("content-type", "application/json")
                .build();


        try {
            // Make the API call and handle the response using our dedicated method
            Response apiResponse = client.newCall(apiRequest).execute();
            return handleApiResponse(apiResponse);
        } catch (IOException e) {
            throw new RuntimeException("Failed to communicate with Claude API: " + e.getMessage(), e);
        }
    }

    private String processHttpMessage(String message, ProcessingMetadata metadata, String analysisType) {
        metadata.isSecurityHeaderCheck = "Security Headers Check".equals(analysisType);

        if (message == null || message.trim().isEmpty()) {
            System.out.println("Warning: Empty message received");
            return "";
        }

        metadata.originalSize = message.length();
        StringBuilder processed = new StringBuilder();
        String[] lines = message.split("\n");
        boolean isHeaders = true;

        // Process the first line (request/status line)
        if (lines.length > 0) {
            processed.append(lines[0]).append("\n");
        }

        // Process headers and body
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                isHeaders = false;
                processed.append("\n");
                continue;
            }

            if (isHeaders) {
                // Special handling for Security Headers Check
                if (metadata.isSecurityHeaderCheck) {
                    // For security header checks, preserve the original header but add analysis
                    for (String sensitive : SENSITIVE_HEADERS) {
                        if (line.toLowerCase().startsWith(sensitive + ":")) {
                            metadata.headerValues.put(sensitive, line.substring(line.indexOf(":") + 1).trim());
                            metadata.redactedHeaders.add(sensitive);
                            // Keep the original header but mark it as sensitive
                            processed.append(line).append(" [Contains sensitive data - included for security analysis]\n");
                            break;
                        }
                    }
                    // If not a sensitive header, just add it normally
                    processed.append(line).append("\n");
                } else {
                    // Original header processing for other analysis types
                    boolean headerRedacted = false;
                    for (String sensitive : SENSITIVE_HEADERS) {
                        if (line.toLowerCase().startsWith(sensitive + ":")) {
                            String headerValue = line.substring(line.indexOf(":") + 1).trim();
                            metadata.headerValues.put(sensitive, headerValue);
                            metadata.redactedHeaders.add(sensitive);

                            // Special handling for cookies
                            if (sensitive.equals("cookie")) {
                                processed.append(processCookieHeader(headerValue)).append("\n");
                            } else {
                                // For other sensitive headers, provide meaningful context
                                String contentType = describeHeaderContent(sensitive, headerValue);
                                processed.append(sensitive).append(": [REDACTED - ").append(contentType).append("]\n");
                            }
                            headerRedacted = true;
                            break;
                        }
                    }
                    if (!headerRedacted) {
                        processed.append(line).append("\n");
                    }
                }
            } else {
                // Body processing remains the same
                if (processed.length() < 50000) {
                    processed.append(line).append("\n");
                } else if (!metadata.bodyTruncated) {
                    metadata.bodyTruncated = true;
                    processed.append("\n[TRUNCATED - Request body exceeds size limit. ");
                    processed.append("Original size: ").append(message.length()).append(" bytes]\n");
                }
            }
        }

        metadata.processedSize = processed.length();
        return processed.toString();
    }


    private String processCookieHeader(String cookieValue) {
        StringBuilder cookieInfo = new StringBuilder("Cookie: [");
        String[] cookies = cookieValue.split(";");
        List<String> cookieDescriptions = new ArrayList<>();

        for (String cookie : cookies) {
            cookie = cookie.trim();
            String[] parts = cookie.split("=", 2);
            if (parts.length > 0) {
                String name = parts[0].trim();
                String value = parts.length > 1 ? parts[1].trim() : "";

                // Analyze cookie characteristics
                boolean isSession = name.toLowerCase().contains("sess");
                boolean isAuth = name.toLowerCase().contains("auth") ||
                        name.toLowerCase().contains("token") ||
                        name.toLowerCase().contains("jwt");

                // Create meaningful description without exposing values
                String description = name + "=" + analyzeCookieValue(value, isSession, isAuth);
                cookieDescriptions.add(description);
            }
        }

        cookieInfo.append(String.join("; ", cookieDescriptions)).append("]");
        return cookieInfo.toString();
    }

    private String analyzeCookieValue(String value, boolean isSession, boolean isAuth) {
        if (value.isEmpty()) return "empty";

        StringBuilder analysis = new StringBuilder();

        if (isAuth) {
            analysis.append("AUTH_TOKEN(");
            if (value.startsWith("eyJ")) {
                analysis.append("JWT format, ");
            }
            analysis.append(value.length()).append(" chars)");
        } else if (isSession) {
            analysis.append("SESSION_ID(").append(value.length()).append(" chars)");
        } else {
            // Analyze the value type without exposing it
            if (value.matches("\\d+")) {
                analysis.append("NUMERIC(").append(value.length()).append(" digits)");
            } else if (value.matches("[0-9a-fA-F]+")) {
                analysis.append("HEX(").append(value.length()).append(" chars)");
            } else {
                analysis.append("STRING(").append(value.length()).append(" chars)");
            }
        }

        return analysis.toString();
    }

    private String createProcessingSummary(ProcessingMetadata requestMeta, ProcessingMetadata responseMeta) {
        StringBuilder summary = new StringBuilder("Processing Summary:\n\n");

        // Summarize request modifications
        summary.append("Request modifications:\n");
        if (!requestMeta.redactedHeaders.isEmpty()) {
            summary.append("- Redacted headers: ").append(String.join(", ", requestMeta.redactedHeaders)).append("\n");
            for (String header : requestMeta.redactedHeaders) {
                String contentDesc = describeHeaderContent(header, requestMeta.headerValues.get(header));
                summary.append("  • ").append(header).append(": ").append(contentDesc).append("\n");
            }
        } else {
            summary.append("- No headers were redacted\n");
        }
        if (requestMeta.bodyTruncated) {
            summary.append("- Request body truncated (Original size: ")
                    .append(requestMeta.originalSize).append(" bytes)\n");
        }

        // Summarize response modifications
        summary.append("\nResponse modifications:\n");
        if (!responseMeta.redactedHeaders.isEmpty()) {
            summary.append("- Redacted headers: ").append(String.join(", ", responseMeta.redactedHeaders)).append("\n");
            for (String header : responseMeta.redactedHeaders) {
                String contentDesc = describeHeaderContent(header, responseMeta.headerValues.get(header));
                summary.append("  • ").append(header).append(": ").append(contentDesc).append("\n");
            }
        } else {
            summary.append("- No headers were redacted\n");
        }
        if (responseMeta.bodyTruncated) {
            summary.append("- Response body truncated (Original size: ")
                    .append(responseMeta.originalSize).append(" bytes)\n");
        }

        return summary.toString();
    }

    private String buildPrompt(String request, String response, String analysisType,
                               ProcessingMetadata requestMeta, ProcessingMetadata responseMeta) {
        // Create a context section explaining what was modified
        StringBuilder contextInfo = new StringBuilder();
        contextInfo.append("Note about the data being analyzed:\n");
        if (!requestMeta.redactedHeaders.isEmpty() || !responseMeta.redactedHeaders.isEmpty()) {
            contextInfo.append("For security purposes, some sensitive headers have been redacted but should still be ");
            contextInfo.append("considered in the security analysis. The presence of these headers might indicate ");
            contextInfo.append("important security mechanisms that should be evaluated.\n\n");
        }

        return switch (analysisType) {
            case "Vulnerability Scan" -> String.format("""
                    %s
                    Analyze this HTTP request and response for security vulnerabilities.
                    Focus on critical security issues and provide actionable recommendations.
                    
                    REQUEST:
                    %s
                    
                    RESPONSE:
                    %s
                    
                    Analyze for:
                    1. Input validation issues
                    2. Authentication/Authorization flaws (consider redacted auth headers in analysis)
                    3. Information disclosure
                    4. Security misconfigurations
                    5. Session management issues (consider cookie usage patterns even if redacted)
                    
                    For each finding, provide:
                    - Severity (Critical/High/Medium/Low)
                    - Description
                    - Potential impact
                    - Specific remediation steps
                    """, contextInfo, request, response);

            case "Security Headers Check" -> String.format("""
                    %s
                    Analyze the security headers in this HTTP exchange:
                    
                    REQUEST:
                    %s
                    
                    RESPONSE:
                    %s
                    
                    Provide:
                    1. Analysis of present security headers
                    2. Missing critical security headers
                    3. Header-specific recommendations
                    4. Security implications of the current configuration
                    5. Best practices for header implementation
                    """, contextInfo, request, response);

            case "Custom Prompt" -> String.format("""
                    %s
                    Analyze this HTTP exchange for security issues:
                    
                    REQUEST:
                    %s
                    
                    RESPONSE:
                    %s
                    
                    Provide a focused security analysis of the most critical findings,
                    considering both visible and redacted security mechanisms.
                    """, contextInfo, request, response);

            default -> throw new IllegalArgumentException("Unknown analysis type: " + analysisType);
        };
    }

    @Override
    public boolean testConnection(String apiKey) {
        this.apiKey = apiKey;
        try {
            String testResult = analyze(
                    "GET / HTTP/1.1\nHost: test.com",
                    "HTTP/1.1 200 OK\nServer: test",
                    "Security Headers Check"
            );
            return testResult != null && !testResult.isEmpty();
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}