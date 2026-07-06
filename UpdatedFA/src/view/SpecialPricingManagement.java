package view;

import SystemController.SystemController;
import billing.SpecialPricingRepository;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SpecialPricingManagement extends JPanel {
    private final SpecialPricingRepository repository;

    private JComboBox<SpecialPricingRepository.RoleItem> roleBox;
    private JTextField nameField;
    private JComboBox<String> typeBox;
    private JComboBox<String> operatorBox;
    private JTextField valueField;
    private JTable pricingTable;
    private DefaultTableModel tableModel;

    private int selectedPricingId = -1;

    public SpecialPricingManagement() {
        Connection conn = SystemController.getDBConnection();
        repository = new SpecialPricingRepository(conn);

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        add(titlePanel(), BorderLayout.NORTH);
        add(formPanel(), BorderLayout.WEST);
        add(tablePanel(), BorderLayout.CENTER);

        loadRoles();
        loadPricing();
    }

    private JPanel titlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Manage Special Pricing");
        title.setFont(new Font("Dialog", Font.BOLD, 38));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            clearForm();
            loadRoles();
            loadPricing();
        });

        panel.add(title, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel formPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(330, 520));

        JPanel form = new JPanel(new GridLayout(10, 1, 0, 8));

        roleBox = new JComboBox<>();
        nameField = new JTextField();
        typeBox = new JComboBox<>(new String[] {"Discount", "Promotion"});
        operatorBox = new JComboBox<>(new String[] {"Multiplier", "Fixed Subtraction"});
        valueField = new JTextField();

        form.add(new JLabel("Role"));
        form.add(roleBox);
        form.add(new JLabel("Pricing Name"));
        form.add(nameField);
        form.add(new JLabel("Pricing Type"));
        form.add(typeBox);
        form.add(new JLabel("Calculation"));
        form.add(operatorBox);
        form.add(new JLabel("Adjustment Value"));
        form.add(valueField);

        JButton addButton = new JButton("Add Pricing");
        JButton updateButton = new JButton("Update Pricing");
        JButton deleteButton = new JButton("Remove Pricing");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(e -> addPricing());
        updateButton.addActionListener(e -> updatePricing());
        deleteButton.addActionListener(e -> deletePricing());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(4, 1, 0, 8));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        outer.add(form, BorderLayout.NORTH);
        outer.add(buttons, BorderLayout.SOUTH);

        setFontForPanel(outer, 17);
        return outer;
    }

    private JScrollPane tablePanel() {
        String[] columns = {"ID", "Role", "Name", "Type", "Operator", "Value"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pricingTable = new JTable(tableModel);
        pricingTable.setRowHeight(28);
        pricingTable.setFont(new Font("Dialog", Font.PLAIN, 15));
        pricingTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 15));

        pricingTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                loadSelectedRow();
            }
        });

        return new JScrollPane(pricingTable);
    }

    private void loadRoles() {
        roleBox.removeAllItems();

        try {
            List<SpecialPricingRepository.RoleItem> roles = repository.loadRoles();

            for (SpecialPricingRepository.RoleItem role : roles) {
                roleBox.addItem(role);
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void loadPricing() {
        tableModel.setRowCount(0);

        try {
            List<SpecialPricingRepository.SpecialPricingRecord> records = repository.loadAll();

            for (SpecialPricingRepository.SpecialPricingRecord record : records) {
                tableModel.addRow(new Object[] {
                        record.getPricingId(),
                        record.getRoleName(),
                        record.getName(),
                        record.getType(),
                        record.getOperator(),
                        record.getValue()
                });
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void addPricing() {
        if (!validateInput()) {
            return;
        }

        SpecialPricingRepository.RoleItem role =
                (SpecialPricingRepository.RoleItem) roleBox.getSelectedItem();

        try {
            repository.add(
                    role.getId(),
                    nameField.getText().trim(),
                    selectedType(),
                    selectedOperator(),
                    new BigDecimal(valueField.getText().trim())
            );

            showInfo("Special pricing added successfully.");
            clearForm();
            loadPricing();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void updatePricing() {
        if (selectedPricingId == -1) {
            showInfo("Please select a pricing row to update.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        SpecialPricingRepository.RoleItem role =
                (SpecialPricingRepository.RoleItem) roleBox.getSelectedItem();

        try {
            repository.update(
                    selectedPricingId,
                    role.getId(),
                    nameField.getText().trim(),
                    selectedType(),
                    selectedOperator(),
                    new BigDecimal(valueField.getText().trim())
            );

            showInfo("Special pricing updated successfully.");
            clearForm();
            loadPricing();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void deletePricing() {
        if (selectedPricingId == -1) {
            showInfo("Please select a pricing row to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove this special pricing rule?",
                "Confirm Remove",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            repository.delete(selectedPricingId);
            showInfo("Special pricing removed successfully.");
            clearForm();
            loadPricing();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private boolean validateInput() {
        if (roleBox.getSelectedItem() == null) {
            showInfo("Please select a role.");
            return false;
        }

        String name = nameField.getText().trim();
        String valueText = valueField.getText().trim();

        if (name.isEmpty() || valueText.isEmpty()) {
            showInfo("Please fill in all fields.");
            return false;
        }

        if (!name.matches("^[A-Za-z0-9]+( [A-Za-z0-9]+)*$")) {
            showInfo("Pricing name can only contain letters, numbers, and spaces.");
            return false;
        }

        try {
            BigDecimal value = new BigDecimal(valueText);

            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                showInfo("Adjustment value must be greater than 0.");
                return false;
            }

            if ("MUL".equals(selectedOperator()) && value.compareTo(BigDecimal.ONE) > 0) {
                showInfo("Multiplier must be between 0 and 1. Example: 0.80 for 20% off.");
                return false;
            }
        } catch (NumberFormatException e) {
            showInfo("Adjustment value must be a valid number.");
            return false;
        }

        return true;
    }

    private void loadSelectedRow() {
        int row = pricingTable.getSelectedRow();

        if (row < 0) {
            return;
        }

        selectedPricingId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String roleName = tableModel.getValueAt(row, 1).toString();

        selectRole(roleName);
        nameField.setText(tableModel.getValueAt(row, 2).toString());
        typeBox.setSelectedIndex("DC".equals(tableModel.getValueAt(row, 3).toString()) ? 0 : 1);
        operatorBox.setSelectedIndex("MUL".equals(tableModel.getValueAt(row, 4).toString()) ? 0 : 1);
        valueField.setText(tableModel.getValueAt(row, 5).toString());
    }

    private void selectRole(String roleName) {
        for (int i = 0; i < roleBox.getItemCount(); i++) {
            SpecialPricingRepository.RoleItem role = roleBox.getItemAt(i);

            if (role.toString().equals(roleName)) {
                roleBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private String selectedType() {
        return typeBox.getSelectedIndex() == 0 ? "DC" : "PR";
    }

    private String selectedOperator() {
        return operatorBox.getSelectedIndex() == 0 ? "MUL" : "SUB";
    }

    private void clearForm() {
        selectedPricingId = -1;
        pricingTable.clearSelection();

        if (roleBox.getItemCount() > 0) {
            roleBox.setSelectedIndex(0);
        }

        typeBox.setSelectedIndex(0);
        operatorBox.setSelectedIndex(0);
        nameField.setText("");
        valueField.setText("");
    }

    private void setFontForPanel(Container container, int size) {
        for (Component component : container.getComponents()) {
            component.setFont(new Font("Dialog", Font.PLAIN, size));

            if (component instanceof Container) {
                setFontForPanel((Container) component, size);
            }
        }
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Special Pricing",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Special Pricing Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
