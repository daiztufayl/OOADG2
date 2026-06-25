import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ViewPatientHistoryFrame extends JFrame {

    private DefaultTableModel tableModel;
    private JTable patientTable;
    private JTextArea txtHistory;
    private JLabel statusLabel;

    public ViewPatientHistoryFrame() {
        setTitle("View Patient History");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("Patient Records", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // table on the left, history on the right
        String[] columns = {"ID", "Name", "Age", "Gender"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.setRowHeight(26);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        JScrollPane tableScroll = new JScrollPane(patientTable);

        // clicking a row shows that patient's medical history below
        patientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = patientTable.getSelectedRow();
                if (row != -1) {
                    Object historyVal = patientTable.getClientProperty("history_" + row);
                    if (historyVal != null) {
                        txtHistory.setText(historyVal.toString());
                    }
                }
            }
        });

        txtHistory = new JTextArea(6, 20);
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        txtHistory.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane historyScroll = new JScrollPane(txtHistory);

        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.add(new JLabel("Medical History (click a patient):"), BorderLayout.NORTH);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, historyPanel);
        splitPane.setResizeWeight(0.65);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose = new JButton("Close");

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPatients();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadPatients();
    }

    private void loadPatients() {
        tableModel.setRowCount(0);
        txtHistory.setText("");

        // clear any stored history properties
        for (int i = 0; i < 9999; i++) {
            patientTable.putClientProperty("history_" + i, null);
        }

        Connection conn = DBConnection.getDBConnection();
        if (conn == null) {
            statusLabel.setText("Could not connect to the database.");
            return;
        }

        String query = "SELECT record_id, patient_name, age, gender, medical_history FROM patientRecords ORDER BY patient_name";

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("record_id");
                String name = rs.getString("patient_name");
                int age = rs.getInt("age");
                String gender = rs.getString("gender");
                String history = rs.getString("medical_history");

                tableModel.addRow(new Object[]{id, name, age, gender});
                // store history per row index for the click listener
                patientTable.putClientProperty("history_" + count, history != null ? history : "No history recorded.");
                count++;
            }

            statusLabel.setText(count + " patient" + (count == 1 ? "" : "s") + " found.");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load patient records.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ViewPatientHistoryFrame().setVisible(true);
            }
        });
    }
}
