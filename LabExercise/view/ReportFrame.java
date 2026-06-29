package view;

import SystemController.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportFrame extends JFrame {

    private JLabel lblTotalPatients;
    private JLabel lblTotalAppointments;
    private JLabel lblTotalDoctors;
    private JLabel lblAvailableSlots;
    private DefaultTableModel scheduleTableModel;
    private JLabel statusLabel;

    private static final String[] DAY_NAMES = {
            "", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    public ReportFrame() {
        setTitle("Hospital Report");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Hospital Management System Report", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Summary section
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Report Summary"));

        lblTotalPatients = new JLabel("Total Patients: 0", SwingConstants.CENTER);
        lblTotalAppointments = new JLabel("Total Active Appointments: 0", SwingConstants.CENTER);
        lblTotalDoctors = new JLabel("Total Doctors: 0", SwingConstants.CENTER);
        lblAvailableSlots = new JLabel("Available Schedule Slots: 0", SwingConstants.CENTER);

        lblTotalPatients.setFont(new Font("Arial", Font.PLAIN, 15));
        lblTotalAppointments.setFont(new Font("Arial", Font.PLAIN, 15));
        lblTotalDoctors.setFont(new Font("Arial", Font.PLAIN, 15));
        lblAvailableSlots.setFont(new Font("Arial", Font.PLAIN, 15));

        summaryPanel.add(lblTotalPatients);
        summaryPanel.add(lblTotalAppointments);
        summaryPanel.add(lblTotalDoctors);
        summaryPanel.add(lblAvailableSlots);

        // Doctor schedule table
        String[] columns = {"Doctor ID", "Doctor Name", "Specialisation", "Day", "Time", "Available"};
        scheduleTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable scheduleTable = new JTable(scheduleTableModel);
        scheduleTable.setRowHeight(26);
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Doctor Schedules"));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(summaryPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Refresh Report");
        JButton btnClose = new JButton("Close");

        btnRefresh.addActionListener(e -> loadReport());
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadReport();
    }

    private void loadReport() {
        loadSummary();
        loadDoctorSchedules();
    }

    private void loadSummary() {
        lblTotalPatients.setText("Total Patients: " + getCount("SELECT COUNT(*) FROM patientrecords"));
        lblTotalAppointments.setText("Total Active Appointments: " + getCount("SELECT COUNT(*) FROM activeappointments"));
        lblTotalDoctors.setText("Total Doctors: " + getCount("SELECT COUNT(*) FROM doctorinfo"));
        lblAvailableSlots.setText("Available Schedule Slots: " + getCount("SELECT COUNT(*) FROM schedule WHERE availability = TRUE"));
    }

    private int getCount(String query) {
        Connection conn = DBConnection.getDBConnection();

        if (conn == null) {
            statusLabel.setText("Database connection failed.");
            return 0;
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load report summary.");
        }

        return 0;
    }

    private void loadDoctorSchedules() {
        scheduleTableModel.setRowCount(0);

        Connection conn = DBConnection.getDBConnection();

        if (conn == null) {
            statusLabel.setText("Database connection failed.");
            return;
        }

        String query =
                "SELECT d.doctor_id, d.doctor_name, d.specialisation, " +
                        "t.day, t.start_time, s.availability " +
                        "FROM doctorinfo d " +
                        "LEFT JOIN schedule s ON d.doctor_id = s.doctor_id " +
                        "LEFT JOIN timeslots t ON s.slot_id = t.slot_id " +
                        "ORDER BY d.doctor_id, t.day, t.start_time";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            int count = 0;

            while (rs.next()) {
                int doctorId = rs.getInt("doctor_id");
                String doctorName = rs.getString("doctor_name");
                String specialisation = rs.getString("specialisation");

                int day = rs.getInt("day");
                String dayName;

                if (rs.wasNull()) {
                    dayName = "No schedule";
                } else if (day >= 1 && day <= 7) {
                    dayName = DAY_NAMES[day];
                } else {
                    dayName = "Unknown";
                }

                String time = rs.getString("start_time");
                if (time == null) {
                    time = "-";
                } else if (time.length() > 5) {
                    time = time.substring(0, 5);
                }

                String available;
                boolean availability = rs.getBoolean("availability");

                if (rs.wasNull()) {
                    available = "-";
                } else {
                    available = availability ? "Yes" : "No";
                }

                scheduleTableModel.addRow(new Object[]{
                        doctorId,
                        doctorName,
                        specialisation,
                        dayName,
                        time,
                        available
                });

                count++;
            }

            statusLabel.setText("Report loaded successfully. " + count + " schedule record(s) shown.");

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load doctor schedules.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportFrame().setVisible(true));
    }
}
