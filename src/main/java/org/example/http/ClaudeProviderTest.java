package org.example.http;

public class ClaudeProviderTest {
    public static void main(String[] args) {
        // Create an instance of ClaudeProvider
        ClaudeProvider provider = new ClaudeProvider();

        // Replace this with your actual API key
        String apiKey = "remove-key-while-building";

        try {
            // Set the API key
            provider.setApiKey(apiKey);

            // Test connection first
            System.out.println("Testing connection...");
            boolean connectionSuccess = provider.testConnection(apiKey);
            if (!connectionSuccess) {
                System.out.println("Connection test failed!");
                return;
            }
            System.out.println("Connection test successful!");

            // Sample HTTP request and response for testing
            String sampleRequest = """
                    POST /login HTTP/1.1
                    Host: example.com
                    Content-Type: application/x-www-form-urlencoded
                    
                    username=admin&password=password123""";

            String sampleResponse = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Server: Apache/2.4.41
                    
                    {"status": "success", "token": "abc123"}""";

            // Test each analysis type
            System.out.println("\nTesting Vulnerability Scan...");
            String vulnResult = provider.analyze(sampleRequest, sampleResponse, "Vulnerability Scan");
            System.out.println("Vulnerability Scan Result:");
            System.out.println(vulnResult);

            System.out.println("\nTesting Security Headers Check...");
            String headerResult = provider.analyze(sampleRequest, sampleResponse, "Security Headers Check");
            System.out.println("Security Headers Check Result:");
            System.out.println(headerResult);

            System.out.println("\nTesting Custom Prompt...");
            String customResult = provider.analyze(sampleRequest, sampleResponse, "Custom Prompt");
            System.out.println("Custom Prompt Result:");
            System.out.println(customResult);

        } catch (RuntimeException e) {
            // Handle any runtime errors that might occur
            System.err.println("Error during analysis: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Caused by: " + e.getCause().getMessage());
            }
        } catch (Exception e) {
            // Handle any other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}