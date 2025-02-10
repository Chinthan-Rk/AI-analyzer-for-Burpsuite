package org.example.http;

public interface AIProvider {
    String analyze(String request, String response, String analysisType);
    boolean testConnection(String apiKey);
    void setApiKey(String apiKey);
}