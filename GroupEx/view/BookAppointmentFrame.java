package view;

import javax.swing.*;

import SystemController.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class BookAppointmentFrame extends JFrame {

    private JComboBox<ComboItem> patientCombo;
    private JComboBox<ComboItem> doctorCombo;
    private JSpinner dateSpinner;
    private JComboBox<String> timeCombo;

    public BookAppointmentFrame() {
        super("Book Appointment");

        setSize(500, 450);
        setLocationRelativeTo(null);
        // DISPOSE_ON_CLOSE, not EXIT_ON_CLOSE: this is a popup form opened from
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Patient
        JPanel patientPanel = new JPanel(new BorderLayout());
        patientPanel.add(new JLabel("Patient:"), BorderLayout.NORTH);
        patientCombo = new JComboBox<>();
        loadPatients();
        patientPanel.add(patientCombo, BorderLayout.CENTER);

        // Doctor
        JPanel doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.add(new JLabel("Doctor:"), BorderLayout.NORTH);
        doctorCombo = new JComboBox<>();
        loadDoctors();
        doctorPanel.add(doctorCombo, BorderLayout.CENTER);

        // Date
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.add(new JLabel("Date:"), BorderLayout.NORTH);
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        datePanel.add(dateSpinner, BorderLayout.CENTER);

        // Time (hourly slots, matching the granularity of your timeSlots table)
        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.add(new JLabel("Time:"), BorderLayout.NORTH);
        timeCombo = new JComboBox<>(buildTimeOptions());
        timePanel.add(timeCombo, BorderLayout.CENTER);

        formPanel.add(patientPanel);
        formPanel.add(doctorPanel);
        formPanel.add(datePanel);
        formPanel.add(timePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnConfirm = new JButton("Confirm Booking");
        JButton btnCancel = new JButton("Cancel");

        btnConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmBooking();
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // fills the patient dropdown from patientRecords
    private void loadPatients() {
        String query = "SELECT record_id, patient_name FROM patientrecords ORDER BY patient_name";
        Connection conn = SystemController.getDBConnection();
        if (conn == null)
            return;

        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                patientCombo.addItem(new ComboItem(rs.getInt("record_id"), rs.getString("patient_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (patientCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No patients found. Register a patient first.",
                    "No Patients", JOptionPane.WARNING_MESSAGE);
        }
    }

    // fills the doctor dropdowns from database
    private void loadDoctors() {
        String query = "SELECT doctor_id, doctor_name, specialisation FROM doctorinfo ORDER BY doctor_name";
        Connection conn = SystemController.getDBConnection();
        if (conn == null)
            return;

        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String label = rs.getString("doctor_name");
                String spec = rs.getString("specialisation");
                if (spec != null && !spec.isEmpty()) {
                    label += " (" + spec + ")";
                }
                doctorCombo.addItem(new ComboItem(rs.getInt("doctor_id"), label));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (doctorCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No doctors found. Add a doctor first.",
                    "No Doctors", JOptionPane.WARNING_MESSAGE);
        }
    }

    // hourly options, "00:00" through "23:00"
    private String[] buildTimeOptions() {
        String[] times = new String[24];
        for (int h = 0; h < 24; h++) {
            times[h] = String.format("%02d:00", h);
        }
        return times;
    }

    private void confirmBooking() {
        ComboItem selectedPatient = (ComboItem) patientCombo.getSelectedItem();
        ComboItem selectedDoctor = (ComboItem) doctorCombo.getSelectedItem();

        if (selectedPatient == null || selectedDoctor == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a patient and a doctor.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date selectedDate = (Date) dateSpinner.getValue();
        String selectedTime = (String) timeCombo.getSelectedItem();

        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime localTime = LocalTime.parse(selectedTime);
        LocalDateTime appointmentDateTime = LocalDateTime.of(localDate, localTime);

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot book an appointment in the past.",
                    "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isSlotTaken(selectedDoctor.id, appointmentDateTime)) {
            JOptionPane.showMessageDialog(this,
                    "This doctor already has an appointment at that date and time.\nPlease choose a different slot.",
                    "Time Slot Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (insertAppointment(selectedDoctor.id, selectedPatient.id, appointmentDateTime)) {
            JOptionPane.showMessageDialog(this,
                    "Appointment booked for " + selectedPatient.label + " with " + selectedDoctor.label +
                            "\non " + appointmentDateTime.toLocalDate() + " at " + selectedTime + ".",
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not book the appointment. Please try again.",
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // prevents duplicate time slots checks if this doctor has any active
    // appointment in same date and time
    private boolean isSlotTaken(int doctorId, LocalDateTime time) {
        String query = "SELECT COUNT(*) AS total FROM appointment WHERE doctor_id = ? AND appointment_time = ? AND status = TRUE";
        Connection conn = SystemController.getDBConnection();
        if (conn == null) {
            return true; // fail safe: block booking if we can't even check
        }
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setTimestamp(2, Timestamp.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total") > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean insertAppointment(int doctorId, int recordId, LocalDateTime time) {
        String query = "INSERT INTO appointment (doctor_id, record_id, appointment_time) VALUES (?, ?, ?)";
        Connection conn = SystemController.getDBConnection();
        if (conn == null)
            return false;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, recordId);
            ps.setTimestamp(3, Timestamp.valueOf(time));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // small helper to keep in check
    private static class ComboItem {
        final int id;
        final String label;

        ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BookAppointmentFrame().setVisible(true);
            }
        });
    }
}