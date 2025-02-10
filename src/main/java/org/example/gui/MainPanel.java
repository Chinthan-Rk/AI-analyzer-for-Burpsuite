package org.example.gui;

import burp.api.montoya.MontoyaApi;
import org.example.gui.table.RequestTable;
import org.example.gui.table.ResponseTable;
import org.example.gui.viewer.MessageViewer;
import org.example.http.AIProvider;
import org.example.http.AIProviderFactory;
import org.example.http.ClaudeProvider;
import org.example.model.RequestEntry;
import org.example.model.ResultsTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class MainPanel extends JPanel {
    // Core components
    private final MontoyaApi api;
    private final RequestTable requestTable;
    private final ResponseTable responseTable;
    private final MessageViewer messageViewer;

    // Configuration controls
    private final JComboBox<String> modelSelector;
    private final JComboBox<String> analysisType;
    private final JPasswordField apiKeyField;
    private final JButton scanButton;
    private final JButton clearButton;

    // Status indicators
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    public MainPanel(MontoyaApi api) {
        this.api = api;
        setLayout(new BorderLayout());

        // Initialize the core components
        requestTable = new RequestTable();
        responseTable = new ResponseTable();
        messageViewer = new MessageViewer(api);

        // Create the control components with modern styling
        modelSelector = new JComboBox<>(new String[]{
                "Claude",
                "OpenAI",
                "Custom"
        });
        modelSelector.setToolTipText("Select the AI model to use for analysis");

        analysisType = new JComboBox<>(new String[]{
                "Vulnerability Scan",
                "Security Headers Check",
                "Custom Prompt"
        });
        analysisType.setToolTipText("Choose the type of security analysis to perform");

        apiKeyField = new JPasswordField(30);
        apiKeyField.setToolTipText("Enter your API key for the selected model");

        scanButton = new JButton("Analyze Selected Request");
        scanButton.setToolTipText("Start analysis of the selected request");

        clearButton = new JButton("Clear Results");
        clearButton.setToolTipText("Clear all analysis results");

        // Create status components
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        // Set up the layout
        setupLayout();
        setupListeners();
    }

    private void setupLayout() {
        // Create the top configuration panel
        JPanel topPanel = createConfigPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create the main content panel with tables and details
        JSplitPane mainContent = createMainContent();
        add(mainContent, BorderLayout.CENTER);

        // Create the status panel
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createConfigPanel() {
        // Create a panel with padding and modern layout
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add components with proper spacing and alignment
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("AI Model:"), gbc);

        gbc.gridx = 1;
        panel.add(modelSelector, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("Analysis Type:"), gbc);

        gbc.gridx = 3;
        panel.add(analysisType, gbc);

        gbc.gridx = 4;
        panel.add(new JLabel("API Key:"), gbc);

        gbc.gridx = 5;
        panel.add(apiKeyField, gbc);

        gbc.gridx = 6;
        panel.add(scanButton, gbc);

        gbc.gridx = 7;
        panel.add(clearButton, gbc);

        return panel;
    }

    private JSplitPane createMainContent() {
        // Create the top split pane for request and response tables
        JPanel requestPanel = new JPanel(new BorderLayout());
        requestPanel.setBorder(BorderFactory.createTitledBorder("Captured Requests"));
        requestPanel.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Analysis Results"));

        // Create a split pane for the response table and its details
        JSplitPane responseDetailSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        responseDetailSplit.setTopComponent(new JScrollPane(responseTable));
        responseDetailSplit.setBottomComponent(responseTable.getDetailsComponent());
        responseDetailSplit.setResizeWeight(0.5);

        responsePanel.add(responseDetailSplit, BorderLayout.CENTER);

        // Create the main split pane
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setTopComponent(requestPanel);
        mainSplit.setBottomComponent(responsePanel);
        mainSplit.setResizeWeight(0.3);

        // Add the message viewer in another split pane
        JSplitPane contentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentSplit.setLeftComponent(mainSplit);
        contentSplit.setRightComponent(messageViewer);
        contentSplit.setResizeWeight(0.7);

        return contentSplit;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(progressBar, BorderLayout.EAST);
        return panel;
    }

    private void setupListeners() {
        // Add request table selection listener
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                RequestEntry selected = requestTable.getSelectedEntry();
                if (selected != null) {
                    messageViewer.setMessage(selected.getRequestResponse());
                    scanButton.setEnabled(true);
                } else {
                    scanButton.setEnabled(false);
                }
            }
        });

        // Add scan button listener
        scanButton.addActionListener(e -> performScan());

        // Add clear button listener
        clearButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to clear all analysis results?",
                    "Clear Results",
                    JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                responseTable.clear();
            }
        });

        // Add model selector listener
        modelSelector.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String model = (String) e.getItem();
                apiKeyField.setEnabled(!model.equals("Custom"));
            }
        });
    }

    private void showProcessingSummary(String summary) {
        // Create a custom dialog with a modern look
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Request Processing Summary", true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create a text area with the summary
        JTextArea summaryArea = new JTextArea(summary);
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Dialog", Font.PLAIN, 12));
        summaryArea.setBackground(new Color(250, 250, 250));
        summaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        // Add a scroll pane in case the content is long
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        // Create an information panel at the top
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(240, 240, 240));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add an info icon and label
        JLabel infoLabel = new JLabel("The following modifications were made to protect sensitive data:");
        infoLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        // Create a button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add a "Don't show again" checkbox
        JCheckBox dontShowCheckbox = new JCheckBox("Don't show this dialog again");
        buttonPanel.add(dontShowCheckbox);

        // Add an OK button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);

        // Add all components to the dialog
        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Show the dialog
        dialog.setVisible(true);
    }

    private void performScan() {
        RequestEntry selectedRequest = requestTable.getSelectedEntry();
        if (selectedRequest == null) {
            showError("Please select a request to analyze");
            return;
        }

        String apiKey = new String(apiKeyField.getPassword());
        if (apiKey.trim().isEmpty()) {
            showError("Please enter an API key");
            return;
        }

        String selectedModel = (String) modelSelector.getSelectedItem();
        String selectedAnalysisType = (String) analysisType.getSelectedItem();

        // Start the analysis in a background thread
        SwingWorker<String, Void> worker = new SwingWorker<>() {

            private String processingSummary;  // To store the processing summary

            @Override
            protected String doInBackground() {
                updateStatus("Analyzing request...", true);
                try {
                    AIProvider provider = AIProviderFactory.getProvider(selectedModel);
                    provider.setApiKey(apiKey);
                    // Since ClaudeProvider's analyze method now prints the summary to console,
                    // we need to capture it. This could be done by modifying ClaudeProvider
                    // to return both the analysis result and the processing summary.
                    String result = provider.analyze(
                            selectedRequest.getRequestResponse().request().toString(),
                            selectedRequest.getRequestResponse().response().toString(),
                            (String) analysisType.getSelectedItem()
                    );

                    // Store the processing summary for later use
                    // You'll need to modify your AIProvider interface and implementations
                    // to provide access to the processing summary
                    if (provider instanceof ClaudeProvider) {
                        processingSummary = ((ClaudeProvider) provider).getLastProcessingSummary();
                    }

                    return result;
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (!result.startsWith("Error:")) {

                        // Show the processing summary dialog
                        if (processingSummary != null) {
                            showProcessingSummary(processingSummary);
                        }

                        // Add the result to the response table
                        ResultsTable resultEntry = new ResultsTable(
                                selectedRequest.getId(),
                                (String) analysisType.getSelectedItem(),
                                result,
                                (String) modelSelector.getSelectedItem()
                        );
                        responseTable.addResult(resultEntry);
                        updateStatus("Analysis complete", false);
                    } else {
                        showError(result.substring(7));
                        updateStatus("Analysis failed", false);
                    }
                } catch (Exception e) {
                    showError("Error during analysis: " + e.getMessage());
                    updateStatus("Analysis failed", false);
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void updateStatus(String status, boolean inProgress) {
        statusLabel.setText(status);
        progressBar.setVisible(inProgress);
    }

    public void addRequest(RequestEntry request) {
        requestTable.addEntry(request);
    }
}