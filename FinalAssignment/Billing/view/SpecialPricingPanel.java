package view;

import SystemController.Session;
import SystemController.SystemController;
import billing.BillingRepository;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SpecialPricingPanel extends JPanel {
    private final BillingRepository repository;
    private final DefaultTableModel tableModel;

    public SpecialPricingPanel() {
        Connection conn = SystemController.getDBConnection();
        repository = new BillingRepository(conn);

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Display Special Pricing");
        title.setFont(new Font("Dialog", Font.BOLD, 38));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        String[] columns = {"Pricing Name", "Type", "Calculation"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Dialog", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 16));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPricing());

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.CENTER);
        top.add(refreshButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadPricing();
    }

    private void loadPricing() {
        tableModel.setRowCount(0);

        Session current = SystemController.getCurrentUser();

        if (current == null) {
            return;
        }

        try {
            List<BillingRepository.PricingDisplayItem> items =
                    repository.loadPricingForRole(current.getRole());

            for (BillingRepository.PricingDisplayItem item : items) {
                tableModel.addRow(new Object[] {
                        item.getName(),
                        displayType(item.getType()),
                        displayCalculation(item.getOperator(), item.getValue())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Special Pricing Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String displayType(String type) {
        return "DC".equals(type) ? "Discount" : "Promotion";
    }

    private String displayCalculation(String operator, BigDecimal value) {
        if ("MUL".equals(operator)) {
            BigDecimal discountPercent = BigDecimal.ONE.subtract(value)
                    .multiply(new BigDecimal("100"));
            return discountPercent.stripTrailingZeros().toPlainString() + "% off";
        }

        return "RM " + value.setScale(2) + " off";
    }
}
