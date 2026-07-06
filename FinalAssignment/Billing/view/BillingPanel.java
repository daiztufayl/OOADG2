package view;

import SystemController.Session;
import SystemController.SystemController;
import billing.Bill;
import billing.BillLine;
import billing.BillingRepository;
import billing.BillingService;

import java.awt.*;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;

public class BillingPanel extends JPanel {
    private final BillingRepository repository;
    private final BillingService billingService;

    private JComboBox<ConditionItem> conditionBox;
    private JTextArea billArea;

    public BillingPanel() {
        Connection conn = SystemController.getDBConnection();
        repository = new BillingRepository(conn);
        billingService = new BillingService(repository);

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        add(titlePanel(), BorderLayout.NORTH);
        add(controlPanel(), BorderLayout.WEST);
        add(billPanel(), BorderLayout.CENTER);

        loadConditionPenalties();
        calculateBill();
    }

    private JPanel titlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Billing & Payment Calculation");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setFont(new Font("Dialog", Font.BOLD, 36));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            loadConditionPenalties();
            calculateBill();
        });

        panel.add(title, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel controlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(250, 500));

        JLabel conditionLabel = new JLabel("Equipment Condition");
        conditionLabel.setFont(new Font("Dialog", Font.PLAIN, 18));

        conditionBox = new JComboBox<>();
        conditionBox.setMaximumSize(new Dimension(230, 35));
        conditionBox.setFont(new Font("Dialog", Font.PLAIN, 16));

        JLabel note = new JLabel(
                "<html><br>Temporary selection until<br>Return Management sends<br>the equipment condition.</html>"
        );
        note.setFont(new Font("Dialog", Font.PLAIN, 13));

        JButton calculateButton = new JButton("Calculate Bill");
        calculateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        calculateButton.addActionListener(e -> calculateBill());

        panel.add(conditionLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(conditionBox);
        panel.add(note);
        panel.add(Box.createVerticalStrut(25));
        panel.add(calculateButton);

        return panel;
    }

    private JScrollPane billPanel() {
        billArea = new JTextArea();
        billArea.setEditable(false);
        billArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
        billArea.setMargin(new Insets(20, 20, 20, 20));

        return new JScrollPane(billArea);
    }

    private void loadConditionPenalties() {
        conditionBox.removeAllItems();
        conditionBox.addItem(new ConditionItem(null, "No Damage"));

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            return;
        }

        try {
            billing.RentalBillingData rental = repository.findCurrentRental(current.getUserID());

            if (rental == null) {
                return;
            }

            List<BillingRepository.PenaltyOption> options =
                    repository.loadConditionPenalties(rental.getCategoryId());

            for (BillingRepository.PenaltyOption option : options) {
                conditionBox.addItem(new ConditionItem(option.getId(), option.toString()));
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void calculateBill() {
        Session current = SystemController.getCurrentUser();

        if (current == null) {
            billArea.setText("User session not found.");
            return;
        }

        ConditionItem selected = (ConditionItem) conditionBox.getSelectedItem();
        Integer penaltyId = selected == null ? null : selected.getPenaltyId();

        try {
            Bill bill = billingService.calculateCurrentBill(current.getUserID(), penaltyId);

            if (bill == null) {
                billArea.setText("No current rental found.\n\nRent equipment before calculating a bill.");
                return;
            }

            billArea.setText(formatBill(bill));
            billArea.setCaretPosition(0);
        } catch (Exception e) {
            showError(e.getMessage());
        }
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

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Billing Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class ConditionItem {
        private final Integer penaltyId;
        private final String name;

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
