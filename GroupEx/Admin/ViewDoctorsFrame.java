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

        String[] columns = {
            "Doctor ID",
            "Doctor Name",
            "Specialisation"
        };
    
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        Connection conn = DBConnection.getDBConnection();

        if (conn != null) {

            String query =
                "SELECT doctor_id, doctor_name, specialisation FROM doctorInfo ORDER BY doctor_id";

            try (PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

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

        JTable table = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViewDoctorsFrame().setVisible(true);
        });
    }
}