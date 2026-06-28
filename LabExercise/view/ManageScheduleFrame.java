package view;

import SystemController.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

/**
 * Allows a doctor to:
 *   1. VIEW their existing schedule slots
 *   2. ADD new time slots (day + hour) to their schedule
 *   3. TOGGLE a slot between Available / Unavailable
 *
 * Flow: Doctor logs in → Dashboard → "Manage Schedule & Availability" → this frame.
 * Once a slot is marked Available, receptionists can book it in BookAppointmentFrame.
 */
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

        setSize(640, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel("My Weekly Schedule", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // ── table ──────────────────────────────────────────────────────────
        String[] columns = {"Slot ID", "Day", "Time", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(26);
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        mainPanel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // ── status + buttons ───────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnAddSlot   = new JButton("Add Time Slot");
        JButton btnToggle    = new JButton("Toggle Availability");
        JButton btnRefresh   = new JButton("Refresh");
        JButton btnClose     = new JButton("Close");

        // ADD SLOT — lets the doctor pick a day + hour and adds it to timeslots + schedule
        btnAddSlot.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddSlotDialog();
                loadSchedule();
            }
        });

        // TOGGLE — flips Available ↔ Unavailable for the selected row
        btnToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = scheduleTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(ManageScheduleFrame.this,
                            "Please select a slot to toggle.",
                            "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int    slotId     = (int)    tableModel.getValueAt(row, 0);
                String currentVal = tableModel.getValueAt(row, 3).toString();
                boolean newVal    = currentVal.equals("No"); // flip
                toggleAvailability(slotId, newVal);
                loadSchedule();
            }
        });

        btnRefresh.addActionListener(e -> loadSchedule());
        btnClose  .addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(btnAddSlot);
        buttonPanel.add(btnToggle);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel,  BorderLayout.NORTH);
        southPanel.add(buttonPanel,  BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        loadSchedule();
    }

    // ── load ────────────────────────────────────────────────────────────────

    private void loadSchedule() {
        tableModel.setRowCount(0);
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) { statusLabel.setText("Database connection failed."); return; }

        String query =
                "SELECT s.slot_id, t.day, t.start_time, s.availability " +
                        "FROM schedule s " +
                        "JOIN timeslots t ON s.slot_id = t.slot_id " +
                        "WHERE s.doctor_id = ? " +
                        "ORDER BY t.day, t.start_time";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                int     slotId    = rs.getInt("slot_id");
                int     day       = rs.getInt("day");
                String  time      = rs.getString("start_time");
                boolean available = rs.getBoolean("availability");

                // trim seconds off "HH:mm:ss" for cleaner display
                if (time != null && time.length() > 5) time = time.substring(0, 5);

                String dayName = (day >= 1 && day <= 7) ? DAY_NAMES[day] : "Unknown";
                tableModel.addRow(new Object[]{ slotId, dayName, time, available ? "Yes" : "No" });
                count++;
            }
            statusLabel.setText(count + " slot" + (count == 1 ? "" : "s") + " in your schedule.");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Could not load schedule.");
        }
    }

    // ── add slot dialog ─────────────────────────────────────────────────────

    /**
     * Pops up a small dialog: doctor picks a day and an hour.
     * We look up (or create) the matching timeslots row, then insert into schedule.
     */
    private void showAddSlotDialog() {
        JDialog dialog = new JDialog(this, "Add Time Slot", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(0, 2, 8, 8));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // Day picker
        String[] dayOptions = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        JComboBox<String> dayCombo = new JComboBox<>(dayOptions);

        // Hour picker (08:00 – 17:00, matching typical clinic hours)
        String[] hourOptions = new String[13];
        for (int h = 8; h <= 20; h++) hourOptions[h - 8] = String.format("%02d:00", h);
        JComboBox<String> hourCombo = new JComboBox<>(hourOptions);

        JButton btnOk     = new JButton("Add");
        JButton btnCancel = new JButton("Cancel");

        dialog.add(new JLabel("Day:"));     dialog.add(dayCombo);
        dialog.add(new JLabel("Time:"));    dialog.add(hourCombo);
        dialog.add(btnOk);                  dialog.add(btnCancel);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnOk.addActionListener(e -> {
            int    dayIndex  = dayCombo.getSelectedIndex() + 1;   // 1=Monday … 7=Sunday
            String startTime = (String) hourCombo.getSelectedItem(); // "HH:00"

            if (addSlotToSchedule(dayIndex, startTime)) {
                JOptionPane.showMessageDialog(dialog,
                        "Slot added: " + dayOptions[dayIndex - 1] + " at " + startTime,
                        "Slot Added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Could not add slot (it may already exist).",
                        "Add Failed", JOptionPane.WARNING_MESSAGE);
            }
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    /**
     * Finds or creates a row in `timeslots` for (day, start_time),
     * then inserts a row in `schedule` linking this doctor to that slot.
     * Availability defaults to TRUE so the slot is immediately bookable.
     *
     * Returns true on success.
     */
    private boolean addSlotToSchedule(int day, String startTime) {
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return false;

        try {
            // 1. Find existing timeslot for this day+time, or insert a new one
            int slotId = -1;
            String findSlot = "SELECT slot_id FROM timeslots WHERE day = ? AND start_time = ?::time";
            try (PreparedStatement ps = conn.prepareStatement(findSlot)) {
                ps.setInt(1, day);
                ps.setString(2, startTime);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) slotId = rs.getInt("slot_id");
            }

            if (slotId == -1) {
                // create the timeslot row
                String insertSlot =
                        "INSERT INTO timeslots (day, start_time) VALUES (?, ?::time) RETURNING slot_id";
                try (PreparedStatement ps = conn.prepareStatement(insertSlot)) {
                    ps.setInt(1, day);
                    ps.setString(2, startTime);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) slotId = rs.getInt("slot_id");
                }
            }

            if (slotId == -1) return false;

            // 2. Check doctor doesn't already have this slot in schedule
            String checkSchedule =
                    "SELECT 1 FROM schedule WHERE doctor_id = ? AND slot_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSchedule)) {
                ps.setInt(1, doctorId);
                ps.setInt(2, slotId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    // already exists — just make it available
                    String reactivate =
                            "UPDATE schedule SET availability = TRUE WHERE doctor_id = ? AND slot_id = ?";
                    try (PreparedStatement upd = conn.prepareStatement(reactivate)) {
                        upd.setInt(1, doctorId);
                        upd.setInt(2, slotId);
                        upd.executeUpdate();
                    }
                    return true;
                }
            }

            // 3. Insert into schedule with availability = TRUE
            String insertSchedule =
                    "INSERT INTO schedule (doctor_id, slot_id, availability) VALUES (?, ?, TRUE)";
            try (PreparedStatement ps = conn.prepareStatement(insertSchedule)) {
                ps.setInt(1, doctorId);
                ps.setInt(2, slotId);
                ps.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── toggle ──────────────────────────────────────────────────────────────

    private void toggleAvailability(int slotId, boolean newVal) {
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return;

        String query = "UPDATE schedule SET availability = ? WHERE doctor_id = ? AND slot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, newVal);
            ps.setInt(2, doctorId);
            ps.setInt(3, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Could not update availability.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManageScheduleFrame(1).setVisible(true));
    }
}