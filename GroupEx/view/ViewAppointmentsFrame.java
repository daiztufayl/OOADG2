package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import SystemController.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ViewAppointmentsFrame extends JFrame {

    private DefaultTableModel tableModel;
    private JTable appointmentsTable;
    private JLabel statusLabel;
    private final Integer doctorId; // null = show all (admin/receptionist view)

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // system-wide view, e.g. for admin/receptionist
    public ViewAppointmentsFrame() {
        this(null);
    }

    // doctor view: only that doctor's own appointments
    public ViewAppointmentsFrame(Integer doctorId) {
        super(doctorId == null ? "All Active Appointments" : "My Active Appointments");
        this.doctorId = doctorId;

        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"Appointment ID", "Doctor", "Patient", "Date", "Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table, this is a view-only screen
            }
        };
        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setRowHeight(28);
        appointmentsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mainPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnClose = new JButton("Close");

        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAppointments();
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
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadAppointments(); // populate immediately on open
    }

    // loads active appointments: all of them, or just this doctor's if doctorId is set
    private void loadAppointments() {
        tableModel.setRowCount(0); // clear existing rows before reloading

        boolean filterByDoctor = (doctorId != null);
        String query = "SELECT appointment_id, doctor_name, patient_name, appointment_time " +
                "FROM activeappointments " +
                (filterByDoctor ? "WHERE doctor_id = ? " : "") +
                "ORDER BY appointment_time";

        Connection conn = SystemController.getDBConnection();
        if (conn == null) {
            statusLabel.setText("Could not connect to the database.");
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            if (filterByDoctor) {
                ps.setInt(1, doctorId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    int appointmentId = rs.getInt("appointment_id");
                    String doctorName = rs.getString("doctor_name");
                    String patientName = rs.getString("patient_name");
                    LocalDateTime time = rs.getTimestamp("appointment_time").toLocalDateTime();

                    tableModel.addRow(new Object[]{
                            appointmentId,
                            doctorName,
                            patientName,
                            time.format(DATE_FORMAT),
                            time.format(TIME_FORMAT)
                    });
                    count++;
                }
                String scope = filterByDoctor ? "" : " system-wide";
                statusLabel.setText(count + " active appointment" + (count == 1 ? "" : "s") + scope);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load appointments.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ViewAppointmentsFrame().setVisible(true);
            }
        });
    }
}