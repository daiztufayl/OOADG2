package view;

import SystemController.Session;
import SystemController.SystemController;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RentalManagement extends JPanel {

    private Connection conn;

    private JTable rentalTable;
    private DefaultTableModel tableModel;

    private JComboBox<EquipmentItem> equipmentBox;
    private JComboBox<RentalItem> rentalBox;

    private JButton rentButton;
    private JButton returnButton;
    private JButton refreshButton;
    private JButton clearButton;

    public RentalManagement() {

        conn = SystemController.getDBConnection();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(titlePanel(), BorderLayout.NORTH);
        add(formPanel(), BorderLayout.WEST);
        add(tablePanel(), BorderLayout.CENTER);

        loadEquipment();
        loadRentalCombo();
        loadRentalTable();

    }

    private JPanel titlePanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Rental & Return Management");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 36));

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        panel.add(title, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;

    }

    private JPanel formPanel() {

        JPanel panel = new JPanel(new GridLayout(8, 1, 5, 10));

        equipmentBox = new JComboBox<>();
        rentalBox = new JComboBox<>();

        rentButton = new JButton("Rent Equipment");
        returnButton = new JButton("Return Equipment");
        clearButton = new JButton("Clear");

        rentButton.addActionListener(e -> rentEquipment());

        returnButton.addActionListener(e -> returnEquipment());

        clearButton.addActionListener(e -> clearForm());

        panel.add(new JLabel("Available Equipment"));
        panel.add(equipmentBox);

        panel.add(rentButton);

        panel.add(new JLabel("Current Rental"));
        panel.add(rentalBox);

        panel.add(returnButton);

        panel.add(clearButton);

        return panel;

    }

    private JScrollPane tablePanel() {

        String[] columns = {
                "Rental ID",
                "Equipment",
                "User",
                "Date Rented"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        
        };

        rentalTable = new JTable(tableModel);

        rentalTable.setRowHeight(25);

        return new JScrollPane(rentalTable);

    }

    private void loadEquipment() {

        equipmentBox.removeAllItems();
    
        String sql =
            "SELECT equipment_id, equipment_name " +
            "FROM equipment " +
            "WHERE num_available > 0 " +
            "ORDER BY equipment_name";
    
        try {
    
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
    
            while (rs.next()) {
    
                equipmentBox.addItem(
                    new EquipmentItem(
                        rs.getInt("equipment_id"),
                        rs.getString("equipment_name")
                    )
                );
    
            }
    
            rs.close();
            ps.close();
    
        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Rental Management",
                    JOptionPane.ERROR_MESSAGE
            );
        
        }
    
    }

    private void loadRentalCombo() {

        rentalBox.removeAllItems();
    
        String sql =
            "SELECT rental_id, equipment " +
            "FROM currentrentals " +
            "ORDER BY rental_id";
    
        try {
    
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
    
            while (rs.next()) {
    
                rentalBox.addItem(
                    new RentalItem(
                        rs.getInt("rental_id"),
                        rs.getString("equipment")
                    )
                );
    
            }
    
            rs.close();
            ps.close();
    
        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Rental Management",
                    JOptionPane.ERROR_MESSAGE
            );
        
        }
    
    }

    private void loadRentalTable() {

        tableModel.setRowCount(0);
    
        String sql = "SELECT * FROM currentrentals ORDER BY rental_id";
    
        try {
    
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
    
            while (rs.next()) {
    
                tableModel.addRow(new Object[] {
    
                    rs.getInt("rental_id"),
                    rs.getString("equipment"),
                    rs.getString("name"),
                    rs.getDate("day_rented")
    
                });
    
            }
    
            rs.close();
            ps.close();
    
        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Rental Management",
                    JOptionPane.ERROR_MESSAGE
            );
        
        }
    
    }

    private void rentEquipment() {

        EquipmentItem equipment = (EquipmentItem) equipmentBox.getSelectedItem();
    
        if (equipment == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an equipment.");
            return;
        }
    
        Session current = SystemController.getCurrentUser();
    
        if (current == null) {
            JOptionPane.showMessageDialog(this,
                    "User session not found.");
            return;
        }
    
        try {
    
            conn.setAutoCommit(false);
    
            String insertRental =
                    "INSERT INTO rental(equipment_id, time_start) " +
                    "VALUES (?, CURRENT_DATE) RETURNING rental_id";
    
            PreparedStatement ps1 = conn.prepareStatement(insertRental);
    
            ps1.setInt(1, equipment.getId());
    
            ResultSet rs = ps1.executeQuery();

            int rentalId = -1;

            if (rs.next()) {
                rentalId = rs.getInt("rental_id");
            }

            if (rentalId == -1) {
                throw new Exception("Failed to create rental.");
            }
    
            rs.close();
            ps1.close();
    
            String updateUser =
                    "UPDATE sysuser " +
                    "SET currrental=? " +
                    "WHERE user_id=?";
    
            PreparedStatement ps2 = conn.prepareStatement(updateUser);
    
            ps2.setInt(1, rentalId);
            ps2.setInt(2, current.getUserID());
    
            ps2.executeUpdate();
    
            ps2.close();
    
            conn.commit();
            conn.setAutoCommit(true);
    
            JOptionPane.showMessageDialog(this,
                    "Equipment rented successfully.");
    
            clearForm();
            loadEquipment();
            loadRentalCombo();
            loadRentalTable();
    
        } catch (Exception e) {
    
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (Exception ex) {}
    
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Rental Management",
                JOptionPane.ERROR_MESSAGE
            );
    
        }
    
    }

    private void returnEquipment() {

        RentalItem rental = (RentalItem) rentalBox.getSelectedItem();
    
        if (rental == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a rental.");
            return;
        }
    
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Return this equipment?",
                "Confirm Return",
                JOptionPane.YES_NO_OPTION);
    
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
    
        try {
    
            conn.setAutoCommit(false);
    
            String updateRental =
                    "UPDATE rental " +
                    "SET time_end=CURRENT_DATE " +
                    "WHERE rental_id=?";
    
            PreparedStatement ps1 = conn.prepareStatement(updateRental);
    
            ps1.setInt(1, rental.getId());
    
            ps1.executeUpdate();
    
            ps1.close();
    
            Session current = SystemController.getCurrentUser();
    
            String updateUser =
                    "UPDATE sysuser " +
                    "SET currrental=NULL " +
                    "WHERE user_id=?";
    
            PreparedStatement ps2 = conn.prepareStatement(updateUser);
    
            ps2.setInt(1, current.getUserID());
    
            ps2.executeUpdate();
    
            ps2.close();
    
            conn.commit();
            conn.setAutoCommit(true);
    
            JOptionPane.showMessageDialog(this,
                    "Equipment returned successfully.");
    
            clearForm();
            loadEquipment();
            loadRentalCombo();
            loadRentalTable();
    
        } catch (Exception e) {
    
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (Exception ex) {}
    
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Rental Management",
                JOptionPane.ERROR_MESSAGE
            );
    
        }
    
    }

    private void refreshData() {

        clearForm();
        loadEquipment();
        loadRentalCombo();
        loadRentalTable();
    
    }

    private void clearForm() {

        if (equipmentBox.getItemCount() > 0)
            equipmentBox.setSelectedIndex(0);
    
        if (rentalBox.getItemCount() > 0)
            rentalBox.setSelectedIndex(0);
    
        rentalTable.clearSelection();
    
    }

    private static class EquipmentItem {

        private int id;
        private String name;
    
        public EquipmentItem(int id, String name) {
    
            this.id = id;
            this.name = name;
    
        }
    
        public int getId() {
    
            return id;
    
        }
    
        @Override
        public String toString() {
    
            return name;
    
        }
    
    }

    private static class RentalItem {

        private int id;
        private String equipment;
    
        public RentalItem(int id, String equipment) {
    
            this.id = id;
            this.equipment = equipment;
    
        }
    
        public int getId() {
    
            return id;
    
        }
    
        @Override
        public String toString() {
    
            return "Rental #" + id + " - " + equipment;
    
        }
    
    }
}