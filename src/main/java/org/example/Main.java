package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import org.example.gui.MainPanel;
import org.example.model.RequestEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main implements BurpExtension, ContextMenuItemsProvider {
    private MontoyaApi api;
    private MainPanel mainPanel;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;

        // Set extension name
        api.extension().setName("Burp AI Analyzer");

        // Initialize main panel
        mainPanel = new MainPanel(api);

        // Register our UI components
        api.userInterface().registerSuiteTab("AI Analyzer", mainPanel);

        // Register context menu
        api.userInterface().registerContextMenuItemsProvider(this);

        // Log that extension loaded
        api.logging().logToOutput("Burp AI Analyzer loaded");
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        JMenuItem sendToAI = new JMenuItem("Send to AI Analyzer");
        sendToAI.addActionListener(e -> {
            var messageEditor = event.messageEditorRequestResponse();
            // Debug log
            api.logging().logToOutput("Context menu clicked");

            // Try getting the request response directly from the event
            var httpReqRes = event.selectedRequestResponses();

            if (!httpReqRes.isEmpty()) {
                var requestResponse = httpReqRes.get(0);  // Get first selected request
                api.logging().logToOutput("Request URL: " + requestResponse.request().url());
                RequestEntry entry = new RequestEntry(requestResponse);
                mainPanel.addRequest(entry);
            } else {
                api.logging().logToOutput("No request selected");
            }

        });

        return List.of(sendToAI);
    }



}