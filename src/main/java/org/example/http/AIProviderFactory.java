package org.example.http;

public class AIProviderFactory {
    public static AIProvider getProvider(String modelType) {
        return switch (modelType) {
            case "Claude" -> new ClaudeProvider();
            case "OpenAI" -> new OpenAIProvider();
            case "Custom" -> new CustomProvider();
            default -> throw new IllegalArgumentException("Unknown model type: " + modelType);
        };
    }
}