package org.example.gui.table;

import org.example.model.RequestEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.ArrayList;

public class RequestTable extends JTable {
    private final List<RequestEntry> requests;
    private final DefaultTableModel model;

    public RequestTable() {
        this.requests = new ArrayList<>();

        // Create table model with non-editable cells
        this.model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Define columns
        model.setColumnIdentifiers(new String[]{"#", "Method", "URL", "Status", "Time"});
        setModel(model);

        // Set column widths
        getColumnModel().getColumn(0).setPreferredWidth(50);    // #
        getColumnModel().getColumn(1).setPreferredWidth(70);    // Method
        getColumnModel().getColumn(2).setPreferredWidth(350);   // URL
        getColumnModel().getColumn(3).setPreferredWidth(70);    // Status
        getColumnModel().getColumn(4).setPreferredWidth(150);   // Time

        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        getTableHeader().setReorderingAllowed(false);
    }

    public void addEntry(RequestEntry entry) {
        requests.add(entry);
        model.addRow(new Object[]{
                requests.size(),          // Row number
                entry.getMethod(),        // HTTP Method
                entry.getUrl(),           // URL
                entry.getStatus(),        // Status Code
                new java.util.Date(entry.getTimestamp())  // Timestamp
        });
    }

    public RequestEntry getSelectedEntry() {
        int selectedRow = getSelectedRow();
        return selectedRow != -1 ? requests.get(selectedRow) : null;
    }

    public void clear() {
        requests.clear();
        model.setRowCount(0);
    }
}