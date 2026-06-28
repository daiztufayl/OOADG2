package view;

import SystemController.DBConnection;

import javax.swing.*;
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
    private JSpinner              dateSpinner;
    private JComboBox<ComboItem> timeCombo;
    private JLabel                slotHintLabel; // tells receptionist when no slots exist

    public BookAppointmentFrame() {
        super("Book Appointment");
        setSize(520, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 12));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        // ── Patient ────────────────────────────────────────────────────────
        JPanel patientPanel = new JPanel(new BorderLayout(4, 2));
        patientPanel.add(new JLabel("Patient:"), BorderLayout.NORTH);
        patientCombo = new JComboBox<>();
        loadPatients();
        patientPanel.add(patientCombo, BorderLayout.CENTER);

        // ── Doctor ─────────────────────────────────────────────────────────
        JPanel doctorPanel = new JPanel(new BorderLayout(4, 2));
        doctorPanel.add(new JLabel("Doctor:"), BorderLayout.NORTH);
        doctorCombo = new JComboBox<>();
        loadDoctors();
        doctorPanel.add(doctorCombo, BorderLayout.CENTER);

        // ── Date ───────────────────────────────────────────────────────────
        JPanel datePanel = new JPanel(new BorderLayout(4, 2));
        datePanel.add(new JLabel("Date:"), BorderLayout.NORTH);
        dateSpinner = new JSpinner(new SpinnerDateModel(
                new Date(),   // initial value = today
                null,         // no min
                null,         // no max
                java.util.Calendar.DAY_OF_MONTH  // up/down arrow steps by 1 day
        ));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        datePanel.add(dateSpinner, BorderLayout.CENTER);

        // ── Available Time Slots ────────────────────────────────────────────
        JPanel timePanel = new JPanel(new BorderLayout(4, 2));
        timePanel.add(new JLabel("Available Time Slots:"), BorderLayout.NORTH);
        timeCombo = new JComboBox<>();
        timePanel.add(timeCombo, BorderLayout.CENTER);

        // hint label shown when no slots are available
        slotHintLabel = new JLabel(" ");
        slotHintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        slotHintLabel.setForeground(new Color(180, 60, 0));
        timePanel.add(slotHintLabel, BorderLayout.SOUTH);

        formPanel.add(patientPanel);
        formPanel.add(doctorPanel);
        formPanel.add(datePanel);
        formPanel.add(timePanel);

        // refresh slots whenever doctor or date changes
        doctorCombo.addActionListener(e -> loadAvailableSlots());
        dateSpinner.addChangeListener(e -> loadAvailableSlots());

        // ── Buttons ────────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnConfirm = new JButton("Confirm Booking");
        JButton btnCancel  = new JButton("Cancel");
        btnConfirm.addActionListener(e -> confirmBooking());
        btnCancel .addActionListener(e -> dispose());
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        add(formPanel,    BorderLayout.CENTER);
        add(buttonPanel,  BorderLayout.SOUTH);

        loadAvailableSlots(); // initial populate
    }

    private void loadPatients() {
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT record_id, patient_name FROM patientrecords ORDER BY patient_name")) {
            while (rs.next())
                patientCombo.addItem(new ComboItem(rs.getInt("record_id"), rs.getString("patient_name")));
        } catch (SQLException e) { e.printStackTrace(); }

        if (patientCombo.getItemCount() == 0)
            JOptionPane.showMessageDialog(this,
                    "No patients found. Register a patient first.",
                    "No Patients", JOptionPane.WARNING_MESSAGE);
    }

    private void loadDoctors() {
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT doctor_id, doctor_name, specialisation FROM doctorinfo ORDER BY doctor_name")) {
            while (rs.next()) {
                String label = rs.getString("doctor_name");
                String spec  = rs.getString("specialisation");
                if (spec != null && !spec.isEmpty()) label += " (" + spec + ")";
                doctorCombo.addItem(new ComboItem(rs.getInt("doctor_id"), label));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        if (doctorCombo.getItemCount() == 0)
            JOptionPane.showMessageDialog(this,
                    "No doctors found. Add a doctor first.",
                    "No Doctors", JOptionPane.WARNING_MESSAGE);
    }

    private void loadAvailableSlots() {
        timeCombo.removeAllItems();
        slotHintLabel.setText(" ");

        ComboItem selectedDoctor = (ComboItem) doctorCombo.getSelectedItem();
        if (selectedDoctor == null) return;

        Date      selectedDate = (Date) dateSpinner.getValue();
        LocalDate localDate    = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int       dayOfWeek    = localDate.getDayOfWeek().getValue(); // 1=Mon … 7=Sun

        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return;

        String query =
                "SELECT t.slot_id, t.start_time " +
                        "FROM schedule s " +
                        "JOIN timeslots t ON s.slot_id = t.slot_id " +
                        "WHERE s.doctor_id = ? " +
                        "  AND s.availability = TRUE " +
                        "  AND t.day = ? " +
                        "  AND NOT EXISTS ( " +
                        "      SELECT 1 FROM appointment " +
                        "      WHERE doctor_id = ? " +
                        "        AND appointment_time::date = ? " +
                        "        AND appointment_time::time = t.start_time " +
                        "        AND status = TRUE " +
                        "  ) " +
                        "ORDER BY t.start_time";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1,  selectedDoctor.id);
            ps.setInt(2,  dayOfWeek);
            ps.setInt(3,  selectedDoctor.id);
            ps.setDate(4, java.sql.Date.valueOf(localDate));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int    slotId = rs.getInt("slot_id");
                    String time   = rs.getString("start_time");
                    if (time != null && time.length() > 5) time = time.substring(0, 5);
                    timeCombo.addItem(new ComboItem(slotId, time));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (timeCombo.getItemCount() == 0) {
            timeCombo.addItem(new ComboItem(-1, "— No available slots —"));
            slotHintLabel.setText(
                    "This doctor has no open slots on " +
                            localDate.getDayOfWeek().toString().charAt(0) +
                            localDate.getDayOfWeek().toString().substring(1).toLowerCase() +
                            ". Ask them to open slots in their schedule first.");
        }
    }

    private void confirmBooking() {
        ComboItem patient = (ComboItem) patientCombo.getSelectedItem();
        ComboItem doctor  = (ComboItem) doctorCombo .getSelectedItem();
        ComboItem slot    = (ComboItem) timeCombo   .getSelectedItem();

        if (patient == null || doctor == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a patient and a doctor.",
                    "Missing Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (slot == null || slot.id == -1) {
            JOptionPane.showMessageDialog(this,
                    "No available slot selected.\n" +
                            "Ask the doctor to open their schedule in the Doctor Dashboard first.",
                    "No Slot", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date      selectedDate      = (Date) dateSpinner.getValue();
        LocalDate localDate         = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalTime localTime         = LocalTime.parse(slot.label);          // "HH:mm"
        LocalDateTime appointmentDT = LocalDateTime.of(localDate, localTime);

        if (appointmentDT.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot book an appointment in the past.",
                    "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // race-condition guard: re-check right before INSERT
        if (isSlotTaken(doctor.id, appointmentDT)) {
            JOptionPane.showMessageDialog(this,
                    "This slot was just taken. Please refresh and choose another.",
                    "Slot No Longer Available", JOptionPane.WARNING_MESSAGE);
            loadAvailableSlots();
            return;
        }

        if (insertAppointment(doctor.id, patient.id, appointmentDT)) {
            JOptionPane.showMessageDialog(this,
                    "Appointment booked!\n" +
                            "Patient : " + patient.label + "\n" +
                            "Doctor  : " + doctor.label  + "\n" +
                            "Date    : " + localDate      + "\n" +
                            "Time    : " + slot.label,
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not book the appointment. Please try again.",
                    "Booking Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isSlotTaken(int doctorId, LocalDateTime time) {
        String query =
                "SELECT COUNT(*) FROM appointment " +
                        "WHERE doctor_id = ? AND appointment_time = ? AND status = TRUE";
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return true;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setTimestamp(2, Timestamp.valueOf(time));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return true;
    }

    private boolean insertAppointment(int doctorId, int recordId, LocalDateTime time) {
        String query = "INSERT INTO appointment (doctor_id, record_id, appointment_time) VALUES (?, ?, ?)";
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ps.setInt(2, recordId);
            ps.setTimestamp(3, Timestamp.valueOf(time));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // PostgreSQL error code 23505 = unique_violation
            // Fires when two receptionists book the same doctor+time at the exact same moment
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(null,
                        "Another receptionist just booked this slot.\nPlease choose a different time.",
                        "Slot Just Taken", JOptionPane.WARNING_MESSAGE);
                loadAvailableSlots(); // refresh so the taken slot disappears from dropdown
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class ComboItem {
        final int    id;
        final String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookAppointmentFrame().setVisible(true));
    }
}