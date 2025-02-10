package org.example.gui.viewer;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;

import javax.swing.*;
import java.awt.*;

public class MessageViewer extends JPanel {
    private final HttpRequestEditor requestEditor;
    private final HttpResponseEditor responseEditor;

    public MessageViewer(MontoyaApi api) {
        setLayout(new BorderLayout());

        // Create editors
        requestEditor = api.userInterface().createHttpRequestEditor();
        responseEditor = api.userInterface().createHttpResponseEditor();

        // Create tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Request", requestEditor.uiComponent());
        tabbedPane.addTab("Response", responseEditor.uiComponent());

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void setMessage(HttpRequestResponse reqRes) {
        requestEditor.setRequest(reqRes.request());
        if (reqRes.response() != null) {
            responseEditor.setResponse(reqRes.response());
        } else {
            responseEditor.setResponse(null);
        }
    }

    public void clear() {
        requestEditor.setRequest(null);
        responseEditor.setResponse(null);
    }
}