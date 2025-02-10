package org.example.model;

import java.time.LocalDateTime;

public class ResultsTable {
    private final String requestId;
    private final String analysisType;
    private final String result;
    private final LocalDateTime analysisTime;
    private final String modelUsed;

    public ResultsTable(String requestId, String analysisType, String result, String modelUsed) {
        this.requestId = requestId;
        this.analysisType = analysisType;
        this.result = result;
        this.analysisTime = LocalDateTime.now();
        this.modelUsed = modelUsed;
    }

    // Getters
    public String getRequestId() { return requestId; }
    public String getAnalysisType() { return analysisType; }
    public String getResult() { return result; }
    public LocalDateTime getAnalysisTime() { return analysisTime; }
    public String getModelUsed() { return modelUsed; }
}