package view;

import SystemController.SystemController;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EquipmentManagement extends JPanel {
    private final Connection conn;

    private JTable equipmentTable;
    private DefaultTableModel tableModel;

    private JComboBox<CategoryItem> categoryBox;
    private JTextField nameField;
    private JTextField dailyRentalField;
    private JTextField quantityField;

    private int selectedEquipmentId = -1;

    public EquipmentManagement() {
        conn = SystemController.getDBConnection();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        add(titlePanel(), BorderLayout.NORTH);
        add(formPanel(), BorderLayout.WEST);
        add(tablePanel(), BorderLayout.CENTER);

        loadCategories();
        loadEquipmentTable();
    }

    private JPanel titlePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Equipment Management");
        title.setFont(new Font("Dialog", Font.BOLD, 42));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Dialog", Font.PLAIN, 20));
        refreshButton.addActionListener(e -> {
            loadCategories();
            loadEquipmentTable();
        });

        panel.add(title, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel formPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(360, 500));

        JPanel form = new JPanel(new GridLayout(10, 1, 0, 10));

        categoryBox = new JComboBox<>();
        nameField = new JTextField();
        dailyRentalField = new JTextField();
        quantityField = new JTextField();

        form.add(fieldLabel("Category:"));
        form.add(categoryBox);
        form.add(fieldLabel("Equipment Name:"));
        form.add(nameField);
        form.add(fieldLabel("Daily Rental Rate:"));
        form.add(dailyRentalField);
        form.add(fieldLabel("Number Available:"));
        form.add(quantityField);

        JButton addButton = new JButton("Add Equipment");
        JButton updateButton = new JButton("Update Equipment");
        JButton removeButton = new JButton("Remove Equipment");
        JButton clearButton = new JButton("Clear Form");

        addButton.addActionListener(new AddListener());
        updateButton.addActionListener(new UpdateListener());
        removeButton.addActionListener(new RemoveListener());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(4, 1, 0, 10));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(removeButton);
        buttons.add(clearButton);

        outer.add(form, BorderLayout.NORTH);
        outer.add(buttons, BorderLayout.SOUTH);

        setFontForPanel(outer, 18);

        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.PLAIN, 18));
        return label;
    }

    private JScrollPane tablePanel() {
        String[] columns = {"ID", "Category", "Equipment Name", "Daily Rental", "Available"};

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        equipmentTable = new JTable(tableModel);
        equipmentTable.setFont(new Font("Dialog", Font.PLAIN, 16));
        equipmentTable.setRowHeight(30);
        equipmentTable.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 16));

        equipmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = equipmentTable.getSelectedRow();

                if (row >= 0) {
                    selectedEquipmentId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    String categoryName = tableModel.getValueAt(row, 1).toString();

                    selectCategoryByName(categoryName);
                    nameField.setText(tableModel.getValueAt(row, 2).toString());
                    dailyRentalField.setText(tableModel.getValueAt(row, 3).toString());
                    quantityField.setText(tableModel.getValueAt(row, 4).toString());
                }
            }
        });

        return new JScrollPane(equipmentTable);
    }

    private void loadCategories() {
        categoryBox.removeAllItems();

        String sql = "SELECT category_id, category_name FROM category ORDER BY category_name";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                categoryBox.addItem(new CategoryItem(
                        result.getInt("category_id"),
                        result.getString("category_name")
                ));
            }

        } catch (SQLException e) {
            showError("Failed to load categories: " + e.getMessage());
        }
    }

    private void loadEquipmentTable() {
        tableModel.setRowCount(0);

        String sql =
                "SELECT e.equipment_id, c.category_name, e.equipment_name, " +
                        "e.daily_rental, e.num_available " +
                        "FROM equipment e " +
                        "JOIN category c ON e.category_id = c.category_id " +
                        "ORDER BY e.equipment_id";

        try (PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                tableModel.addRow(new Object[] {
                        result.getInt("equipment_id"),
                        result.getString("category_name"),
                        result.getString("equipment_name"),
                        result.getBigDecimal("daily_rental"),
                        result.getInt("num_available")
                });
            }

        } catch (SQLException e) {
            showError("Failed to load equipment: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (categoryBox.getSelectedItem() == null) {
            showInfo("Please select a category.");
            return false;
        }

        String equipmentName = nameField.getText().trim();
        String dailyRental = dailyRentalField.getText().trim();
        String quantity = quantityField.getText().trim();

        if (equipmentName.isEmpty() || dailyRental.isEmpty() || quantity.isEmpty()) {
            showInfo("Please fill in all fields.");
            return false;
        }

        if (!equipmentName.matches("^[A-Za-z0-9]+( [A-Za-z0-9]+)*$")) {
            showInfo("Equipment name can only contain letters, numbers, and spaces.");
            return false;
        }

        try {
            double rate = Double.parseDouble(dailyRental);
            if (rate <= 0) {
                showInfo("Daily rental rate must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            showInfo("Daily rental rate must be a valid number.");
            return false;
        }

        try {
            int available = Integer.parseInt(quantity);
            if (available < 0) {
                showInfo("Number available cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            showInfo("Number available must be a whole number.");
            return false;
        }

        return true;
    }

    private class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!validateInput()) {
                return;
            }

            CategoryItem category = (CategoryItem) categoryBox.getSelectedItem();

            String sql =
                    "INSERT INTO equipment(category_id, equipment_name, daily_rental, num_available) " +
                            "VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, category.getId());
                statement.setString(2, nameField.getText().trim());
                statement.setBigDecimal(3, new java.math.BigDecimal(dailyRentalField.getText().trim()));
                statement.setInt(4, Integer.parseInt(quantityField.getText().trim()));

                statement.executeUpdate();

                showInfo("Equipment added successfully.");
                clearForm();
                loadEquipmentTable();

            } catch (SQLException ex) {
                showError("Failed to add equipment: " + ex.getMessage());
            }
        }
    }

    private class UpdateListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (selectedEquipmentId == -1) {
                showInfo("Please select an equipment row to update.");
                return;
            }

            if (!validateInput()) {
                return;
            }

            CategoryItem category = (CategoryItem) categoryBox.getSelectedItem();

            String sql =
                    "UPDATE equipment " +
                            "SET category_id = ?, equipment_name = ?, daily_rental = ?, num_available = ? " +
                            "WHERE equipment_id = ?";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, category.getId());
                statement.setString(2, nameField.getText().trim());
                statement.setBigDecimal(3, new java.math.BigDecimal(dailyRentalField.getText().trim()));
                statement.setInt(4, Integer.parseInt(quantityField.getText().trim()));
                statement.setInt(5, selectedEquipmentId);

                statement.executeUpdate();

                showInfo("Equipment updated successfully.");
                clearForm();
                loadEquipmentTable();

            } catch (SQLException ex) {
                showError("Failed to update equipment: " + ex.getMessage());
            }
        }
    }

    private class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (selectedEquipmentId == -1) {
                showInfo("Please select an equipment row to remove.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    EquipmentManagement.this,
                    "Are you sure you want to remove this equipment?",
                    "Confirm Remove",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            String sql = "DELETE FROM equipment WHERE equipment_id = ?";

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, selectedEquipmentId);
                statement.executeUpdate();

                showInfo("Equipment removed successfully.");
                clearForm();
                loadEquipmentTable();

            } catch (SQLException ex) {
                showError(
                        "Failed to remove equipment. It may already be used in a rental record.\n" +
                                ex.getMessage()
                );
            }
        }
    }

    private void clearForm() {
        selectedEquipmentId = -1;
        equipmentTable.clearSelection();

        if (categoryBox.getItemCount() > 0) {
            categoryBox.setSelectedIndex(0);
        }

        nameField.setText("");
        dailyRentalField.setText("");
        quantityField.setText("");
    }

    private void selectCategoryByName(String categoryName) {
        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            CategoryItem item = categoryBox.getItemAt(i);

            if (item.getName().equals(categoryName)) {
                categoryBox.setSelectedIndex(i);
                return;
            }
        }
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
        JOptionPane.showMessageDialog(this, message, "Equipment Management", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Equipment Management Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class CategoryItem {
        private final int id;
        private final String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }
    }
}
