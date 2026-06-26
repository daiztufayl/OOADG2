package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import SystemController.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

// Allows a doctor to view and manage their weekly schedule.
// Called from DoctorDashboard with the logged-in doctor's ID.
public class ManageScheduleFrame extends JFrame {

    private final int doctorId;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // day names matching the DB (day 1=Monday ... day 7=Sunday)
    private static final String[] DAY_NAMES = {
            "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public ManageScheduleFrame(int doctorId) {
        super("Manage Schedule & Availability");
        this.doctorId = doctorId;

        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("My Weekly Schedule", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // table showing current schedule slots
        String[] columns = { "Slot ID", "Day", "Time", "Available" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only, use buttons to change availability
            }
        };

        JTable scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(26);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // buttons: toggle availability of selected slot, refresh, close
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnToggle = new JButton("Toggle Availability");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose = new JButton("Close");

        // toggles the availability of the selected row in the schedule
        btnToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = scheduleTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(null,
                            "Please select a slot to toggle.",
                            "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int slotId = (int) tableModel.getValueAt(row, 0);
                String currentVal = tableModel.getValueAt(row, 3).toString();
                boolean newVal = currentVal.equals("No"); // flip the value

                toggleAvailability(slotId, newVal);
                loadSchedule(); // refresh table after change
            }
        });

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSchedule();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnToggle);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadSchedule(); // populate on open
    }

    // loads this doctor's schedule slots from the schedule + timeslots tables
    private void loadSchedule() {
        tableModel.setRowCount(0);

        Connection conn = SystemController.getDBConnection();
        if (conn == null) {
            statusLabel.setText("Could not connect to the database.");
            return;
        }

        String query = "SELECT s.slot_id, t.day, t.start_time, s.availability " +
                "FROM schedule s " +
                "JOIN timeslots t ON s.slot_id = t.slot_id " +
                "WHERE s.doctor_id = ? " +
                "ORDER BY t.day, t.start_time";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                int slotId = rs.getInt("slot_id");
                int day = rs.getInt("day");
                String time = rs.getString("start_time");
                boolean available = rs.getBoolean("availability");

                // convert day number to name
                String dayName = (day >= 1 && day <= 7) ? DAY_NAMES[day] : "Unknown";

                tableModel.addRow(new Object[] {
                        slotId,
                        dayName,
                        time,
                        available ? "Yes" : "No"
                });
                count++;
            }

            statusLabel.setText(count + " slot" + (count == 1 ? "" : "s") + " in your schedule.");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load schedule.");
        }
    }

    // updates the availability of a single slot in the schedule table
    private void toggleAvailability(int slotId, boolean newVal) {
        Connection conn = SystemController.getDBConnection();
        if (conn == null)
            return;

        String query = "UPDATE schedule SET availability = ? WHERE doctor_id = ? AND slot_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, newVal);
            ps.setInt(2, doctorId);
            ps.setInt(3, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not update availability.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManageScheduleFrame(1).setVisible(true);
            }
        });
    }
}