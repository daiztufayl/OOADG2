package view;

import SystemController.Session;
import SystemController.SystemController;

import billing.Bill;
import billing.BillLine;
import billing.BillingRepository;
import billing.BillingService;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RentalManagement extends JPanel {

    private Connection conn;

    private BillingRepository billingRepository;
    private BillingService billingService;

    private JTable rentalTable;
    private DefaultTableModel tableModel;

    private JComboBox<EquipmentItem> equipmentBox;
    private JComboBox<RentalItem> rentalBox;
    private JComboBox<ConditionItem> conditionBox;

    private JButton rentButton;
    private JButton returnButton;
    private JButton refreshButton;
    private JButton clearButton;

    public RentalManagement() {

        conn = SystemController.getDBConnection();

        billingRepository = new BillingRepository(conn);
        billingService = new BillingService(billingRepository);

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(titlePanel(), BorderLayout.NORTH);
        add(formPanel(), BorderLayout.WEST);
        add(tablePanel(), BorderLayout.CENTER);

        loadEquipment();
        loadRentalCombo();
        loadConditionPenalties();
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

        JPanel panel = new JPanel(new GridLayout(11, 1, 5, 10));
        panel.setPreferredSize(new Dimension(300, 560));

        equipmentBox = new JComboBox<>();
        rentalBox = new JComboBox<>();
        conditionBox = new JComboBox<>();

        rentButton = new JButton("Rent Equipment");
        returnButton = new JButton("Return Equipment");
        clearButton = new JButton("Clear");

        rentButton.addActionListener(e -> rentEquipment());
        returnButton.addActionListener(e -> returnEquipment());
        clearButton.addActionListener(e -> clearForm());

        panel.add(new JLabel("Available Equipment"));
        panel.add(equipmentBox);

        panel.add(rentButton);

        panel.add(new JLabel("Your Current Rental"));
        panel.add(rentalBox);

        panel.add(new JLabel("Equipment Condition"));
        panel.add(conditionBox);

        panel.add(returnButton);
        panel.add(clearButton);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        setFontForPanel(panel, 17);

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
        rentalTable.setFont(new Font("Dialog", Font.PLAIN, 15));
        rentalTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 15));

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

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            return;
        }

        String sql =
                "SELECT cr.rental_id, cr.equipment " +
                        "FROM currentrentals cr " +
                        "JOIN sysuser u ON u.currrental = cr.rental_id " +
                        "WHERE u.user_id = ? " +
                        "ORDER BY cr.rental_id";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, current.getUserID());

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

    private void loadConditionPenalties() {

        conditionBox.removeAllItems();
        conditionBox.addItem(new ConditionItem(null, "No Damage"));

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            return;
        }

        try {

            billing.RentalBillingData rental =
                    billingRepository.findCurrentRental(current.getUserID());

            if (rental == null) {
                return;
            }

            List<BillingRepository.PenaltyOption> options =
                    billingRepository.loadConditionPenalties(rental.getCategoryId());

            for (BillingRepository.PenaltyOption option : options) {
                conditionBox.addItem(
                        new ConditionItem(option.getId(), option.toString())
                );
            }

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

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            return;
        }

        String sql =
                "SELECT cr.rental_id, cr.equipment, cr.name, cr.day_rented " +
                        "FROM currentrentals cr " +
                        "JOIN sysuser u ON u.currrental = cr.rental_id " +
                        "WHERE u.user_id = ? " +
                        "ORDER BY cr.rental_id";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, current.getUserID());

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

    private boolean hasActiveRental(int userId) {

        String sql =
                "SELECT r.rental_id " +
                        "FROM sysuser u " +
                        "JOIN rental r ON u.currrental = r.rental_id " +
                        "WHERE u.user_id = ? AND r.time_end IS NULL";

        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            boolean active = rs.next();

            rs.close();
            ps.close();

            return active;

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Rental Management",
                    JOptionPane.ERROR_MESSAGE
            );

            return true;

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

        if (hasActiveRental(current.getUserID())) {
            JOptionPane.showMessageDialog(this,
                    "Please return your current rental before renting another equipment.");
            return;
        }

        boolean oldAutoCommit = true;

        try {

            oldAutoCommit = conn.getAutoCommit();
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
                            "SET currrental = ? " +
                            "WHERE user_id = ?";

            PreparedStatement ps2 = conn.prepareStatement(updateUser);

            ps2.setInt(1, rentalId);
            ps2.setInt(2, current.getUserID());

            ps2.executeUpdate();

            ps2.close();

            conn.commit();
            conn.setAutoCommit(oldAutoCommit);

            JOptionPane.showMessageDialog(this,
                    "Equipment rented successfully.");

            refreshData();

        } catch (Exception e) {

            try {
                conn.rollback();
                conn.setAutoCommit(oldAutoCommit);
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
                    "You do not have a current rental to return.");
            return;
        }

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            JOptionPane.showMessageDialog(this,
                    "User session not found.");
            return;
        }

        ConditionItem selectedCondition = (ConditionItem) conditionBox.getSelectedItem();
        Integer penaltyId = selectedCondition == null ? null : selectedCondition.getPenaltyId();

        Bill bill;

        try {

            // calculate the bill before clearing current rental
            bill = billingService.calculateCurrentBill(current.getUserID(), penaltyId);

            if (bill == null) {
                JOptionPane.showMessageDialog(this,
                        "No current rental found for billing.");
                return;
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Billing Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;

        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Return this equipment and generate bill?",
                "Confirm Return",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean oldAutoCommit = true;

        try {

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String updateRental =
                    "UPDATE rental " +
                            "SET time_end = CURRENT_DATE " +
                            "WHERE rental_id = ? AND time_end IS NULL";

            PreparedStatement ps1 = conn.prepareStatement(updateRental);

            ps1.setInt(1, rental.getId());

            int updatedRental = ps1.executeUpdate();

            ps1.close();

            if (updatedRental == 0) {
                throw new Exception("Rental already returned or not found.");
            }

            String updateUser =
                    "UPDATE sysuser " +
                            "SET currrental = NULL " +
                            "WHERE user_id = ?";

            PreparedStatement ps2 = conn.prepareStatement(updateUser);

            ps2.setInt(1, current.getUserID());

            ps2.executeUpdate();

            ps2.close();

            conn.commit();
            conn.setAutoCommit(oldAutoCommit);

            showBillDialog(bill);

            JOptionPane.showMessageDialog(this,
                    "Equipment returned successfully.");

            refreshData();

        } catch (Exception e) {

            try {
                conn.rollback();
                conn.setAutoCommit(oldAutoCommit);
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
        loadConditionPenalties();
        loadRentalTable();

    }

    private void clearForm() {

        if (equipmentBox.getItemCount() > 0)
            equipmentBox.setSelectedIndex(0);

        if (rentalBox.getItemCount() > 0)
            rentalBox.setSelectedIndex(0);

        if (conditionBox.getItemCount() > 0)
            conditionBox.setSelectedIndex(0);

        rentalTable.clearSelection();

    }

    private void showBillDialog(Bill bill) {

        JTextArea billArea = new JTextArea(formatBill(bill));
        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        billArea.setMargin(new Insets(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(billArea);
        scrollPane.setPreferredSize(new Dimension(520, 500));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Detailed Bill",
                JOptionPane.INFORMATION_MESSAGE
        );

    }

    private String formatBill(Bill bill) {

        StringBuilder text = new StringBuilder();

        text.append("SMART EQUIPMENT RENTAL - DETAILED BILL\n");
        text.append("========================================\n\n");
        text.append(String.format("Rental ID       : %d%n", bill.getRentalId()));
        text.append(String.format("Equipment       : %s%n", bill.getEquipmentName()));
        text.append(String.format("Category        : %s%n", bill.getCategoryName()));
        text.append(String.format("Rental Date     : %s%n", bill.getStartDate()));
        text.append(String.format("Bill Date       : %s%n", bill.getBillDate()));
        text.append(String.format("Rental Duration : %d day(s)%n", bill.getRentalDays()));
        text.append(String.format("Daily Rate      : RM %8.2f%n", bill.getDailyRate()));

        text.append("\nCHARGES\n");
        text.append("----------------------------------------\n");
        text.append(String.format("%-24s RM %8.2f%n", "Base Rental Fee", bill.getBaseFee()));

        for (BillLine line : bill.getLines()) {
            String sign = line.getType() == BillLine.Type.DISCOUNT ? "-" : "+";
            text.append(String.format(
                    "%-24s %sRM %8.2f%n",
                    line.getDescription(),
                    sign,
                    line.getAmount()
            ));
        }

        text.append("----------------------------------------\n");
        text.append(String.format("%-24s RM %8.2f%n", "Total Discount", bill.getTotalDiscount()));
        text.append(String.format("%-24s RM %8.2f%n", "Total Penalty", bill.getTotalPenalty()));
        text.append("========================================\n");
        text.append(String.format("%-24s RM %8.2f%n", "NET PAYABLE", bill.getNetPayable()));

        return text.toString();

    }

    private void setFontForPanel(Container container, int size) {

        for (Component component : container.getComponents()) {

            component.setFont(new Font("Dialog", Font.PLAIN, size));

            if (component instanceof Container) {
                setFontForPanel((Container) component, size);
            }

        }

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

    private static class ConditionItem {

        private Integer penaltyId;
        private String name;

        public ConditionItem(Integer penaltyId, String name) {
            this.penaltyId = penaltyId;
            this.name = name;
        }

        public Integer getPenaltyId() {
            return penaltyId;
        }

        @Override
        public String toString() {
            return name;
        }

    }
}