package view;

import SystemController.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewDoctorsFrame extends JFrame {

    public ViewDoctorsFrame() {

        setTitle("View Doctors");
        setSize(500, 300);
        setLocationRelativeTo(null);

        // Table columns
        String[] columns = {
                "Doctor ID",
                "Doctor Name",
                "Specialisation"
        };

        // Create table model
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Connect to database
        Connection conn = DBConnection.getDBConnection();

        if (conn != null) {

            // Retrieve doctor records
            String query = "SELECT doctor_id, doctor_name, specialisation FROM doctorinfo ORDER BY doctor_id";

            try (PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                // Add records to table
                while (rs.next()) {

                    model.addRow(new Object[] {
                            rs.getInt("doctor_id"),
                            rs.getString("doctor_name"),
                            rs.getString("specialisation")
                    });

                }

            } catch (SQLException e) {

                e.printStackTrace();

                JOptionPane.showMessageDialog(
                        null,
                        "Unable to load doctors.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // Display table
        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
    }

    // Launch the View Doctors window
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewDoctorsFrame().setVisible(true);
        });
    }
}