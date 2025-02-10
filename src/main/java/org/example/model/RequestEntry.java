package org.example.model;

import burp.api.montoya.http.message.HttpRequestResponse;

public class RequestEntry {
    private final String id;
    private final HttpRequestResponse requestResponse;
    private final String url;
    private final String method;
    private final long timestamp;
    private String status;

    public RequestEntry(HttpRequestResponse requestResponse) {
        this.id = java.util.UUID.randomUUID().toString();
        this.requestResponse = requestResponse;
        this.url = requestResponse.request().url();
        this.method = requestResponse.request().method();
        this.timestamp = System.currentTimeMillis();
        this.status = requestResponse.response() != null ?
                String.valueOf(requestResponse.response().statusCode()) : "No Response";
    }

    // Getters
    public String getId() { return id; }
    public HttpRequestResponse getRequestResponse() { return requestResponse; }
    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
}