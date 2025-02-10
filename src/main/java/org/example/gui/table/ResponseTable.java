package org.example.gui.table;

import org.example.model.ResultsTable;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class ResponseTable extends JTable {
    private final List<ResultsTable> results;
    private final DefaultTableModel model;
    private final JTextArea detailsArea;

    public ResponseTable() {
        this.results = new ArrayList<>();

        // Create table model with non-editable cells
        this.model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Define columns with more meaningful headers
        model.setColumnIdentifiers(new String[]{
                "#",
                "Scan Type",
                "Severity",
                "Finding Summary",
                "Time"
        });
        setModel(model);

        // Configure table appearance and behavior
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoCreateRowSorter(true);

        // Set column widths
        getColumnModel().getColumn(0).setPreferredWidth(50);     // #
        getColumnModel().getColumn(1).setPreferredWidth(120);    // Scan Type
        getColumnModel().getColumn(2).setPreferredWidth(80);     // Severity
        getColumnModel().getColumn(3).setPreferredWidth(400);    // Finding Summary
        getColumnModel().getColumn(4).setPreferredWidth(150);    // Time

        // Create details area for full analysis
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setLineWrap(true);

        // Add selection listener to update details area
        getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsArea();
            }
        });

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        setRowSorter(sorter);
    }

    public void addResult(ResultsTable result) {
        results.add(result);

        // Parse the analysis result to determine severity and summary
        String severity = determineSeverity(result.getResult());
        String summary = extractSummary(result.getResult());

        // Add the row to the table
        model.addRow(new Object[]{
                results.size(),              // Row number
                result.getAnalysisType(),    // Analysis Type
                severity,                    // Severity
                summary,                     // Summary
                result.getAnalysisTime()     // Time
        });
    }

    private String determineSeverity(String analysisText) {
        // Determine severity based on keywords in the analysis
        analysisText = analysisText.toLowerCase();
        if (analysisText.contains("critical") ||
                analysisText.contains("severe") ||
                analysisText.contains("high risk")) {
            return "Critical";
        } else if (analysisText.contains("high") ||
                analysisText.contains("significant")) {
            return "High";
        } else if (analysisText.contains("medium") ||
                analysisText.contains("moderate")) {
            return "Medium";
        } else if (analysisText.contains("low") ||
                analysisText.contains("minor")) {
            return "Low";
        }
        return "Info";
    }

    private String extractSummary(String analysisText) {
        // Extract first meaningful line or finding as summary
        String[] lines = analysisText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() &&
                    !line.startsWith("#") &&
                    !line.startsWith("Analysis") &&
                    line.length() > 10) {
                // Truncate if too long
                if (line.length() > 100) {
                    return line.substring(0, 97) + "...";
                }
                return line;
            }
        }
        return "No summary available";
    }

    private void updateDetailsArea() {
        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            selectedRow = convertRowIndexToModel(selectedRow);
            ResultsTable result = results.get(selectedRow);
            detailsArea.setText(formatAnalysisText(result.getResult()));
            detailsArea.setCaretPosition(0);
        } else {
            detailsArea.setText("");
        }
    }

    private String formatAnalysisText(String text) {
        // Add some basic formatting to make the analysis more readable
        StringBuilder formatted = new StringBuilder();
        String[] lines = text.split("\n");
        boolean inList = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                formatted.append("\n");
                continue;
            }

            // Handle headers
            if (line.matches("\\d+\\..*")) {
                formatted.append("\n").append(line).append("\n");
                inList = true;
            } else if (line.startsWith("-")) {
                if (!inList) formatted.append("\n");
                formatted.append("  ").append(line).append("\n");
                inList = true;
            } else {
                if (inList) formatted.append("\n");
                formatted.append(line).append("\n");
                inList = false;
            }
        }

        return formatted.toString();
    }

    public JComponent getDetailsComponent() {
        return new JScrollPane(detailsArea);
    }

    public ResultsTable getSelectedResult() {
        int selectedRow = getSelectedRow();
        return selectedRow != -1 ? results.get(convertRowIndexToModel(selectedRow)) : null;
    }

    public void clear() {
        results.clear();
        model.setRowCount(0);
        detailsArea.setText("");
    }
}