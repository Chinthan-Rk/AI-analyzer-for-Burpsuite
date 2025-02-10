package org.example.http;

public class OpenAIProvider implements AIProvider {
    private String apiKey;

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String analyze(String request, String response, String analysisType) {
        // TODO: Implement OpenAI API integration
        throw new UnsupportedOperationException("OpenAI integration not implemented yet");
    }

    @Override
    public boolean testConnection(String apiKey) {
        this.apiKey = apiKey;
        return false;
    }
}