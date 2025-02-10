package org.example.http;

public class CustomProvider implements AIProvider {
    private String apiKey;

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String analyze(String request, String response, String analysisType) {
        // TODO: Implement custom API integration
        throw new UnsupportedOperationException("Custom integration not implemented yet");
    }

    @Override
    public boolean testConnection(String apiKey) {
        this.apiKey = apiKey;
        return false;
    }
}