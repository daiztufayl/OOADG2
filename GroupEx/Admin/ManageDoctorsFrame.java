import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageDoctorsFrame extends JFrame {

    public ManageDoctorsFrame() {

        setTitle("Manage Doctors");
        setSize(650, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 5, 5));

        JLabel lblName = new JLabel("Doctor Name:");
        JTextField txtName = new JTextField();

        JLabel lblUsername = new JLabel("Username:");
        JTextField txtUsername = new JTextField();

        JLabel lblPassword = new JLabel("Password:");
        JPasswordField txtPassword = new JPasswordField();

        JLabel lblSpecialisation = new JLabel("Specialisation:");
        JTextField txtSpecialisation = new JTextField();

        JButton btnAddDoctor = new JButton("Add Doctor");
        JButton btnClear = new JButton("Clear");
        JButton btnViewDoctors = new JButton("View Doctors");
        JButton btnAssignSpecialisation = new JButton("Assign Specialisation");

        btnViewDoctors.addActionListener(e -> {

            new ViewDoctorsFrame().setVisible(true);
        
        });
        
        btnAddDoctor.addActionListener(e -> {

            if (txtName.getText().trim().isEmpty() ||
                txtUsername.getText().trim().isEmpty() ||
                new String(txtPassword.getPassword()).trim().isEmpty() ||
                txtSpecialisation.getText().trim().isEmpty()) {
        
                JOptionPane.showMessageDialog(
                    null,
                    "Please fill in all fields!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );

                txtName.setText("");
                txtUsername.setText("");
                txtPassword.setText("");
                txtSpecialisation.setText("");
        
            } else {

                Connection conn = DBConnection.getConnection();
            
                if (conn == null) {
                    return;
                }

                String checkQuery =
                    "SELECT COUNT(*) FROM hmsuser WHERE user_username = ?";

                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

                    checkStmt.setString(1, txtUsername.getText().trim());

                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {

                        JOptionPane.showMessageDialog(
                             null,
                            "Username already exists!",
                            "Duplicate Username",
                            JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                } catch (SQLException ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                        null,
                        "Database Error!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }

                String insertUserQuery =
                "INSERT INTO hmsuser (user_role, user_username, user_password, user_name) " +
                "VALUES (?, ?, ?, ?)";

                try (PreparedStatement insertUserStmt = conn.prepareStatement(insertUserQuery)) {

                    insertUserStmt.setString(1, "DR");
                    insertUserStmt.setString(2, txtUsername.getText().trim());
                    insertUserStmt.setString(3, new String(txtPassword.getPassword()).trim());
                    insertUserStmt.setString(4, txtName.getText().trim());

                    insertUserStmt.executeUpdate();

                    // Get the doctor's ID
                    String getDoctorIdQuery =
                        "SELECT user_id FROM hmsuser WHERE user_username = ?";

                    try (PreparedStatement getDoctorStmt =
                            conn.prepareStatement(getDoctorIdQuery)) {

                        getDoctorStmt.setString(1, txtUsername.getText().trim());

                        ResultSet rs = getDoctorStmt.executeQuery();

                        if (rs.next()) {

                            int doctorID = rs.getInt("user_id");

                            String insertDoctorQuery =
                                "INSERT INTO doctor (doctor_id, doctor_specialisation) VALUES (?, ?)";

                            try (PreparedStatement insertDoctorStmt =
                                    conn.prepareStatement(insertDoctorQuery)) {

                                insertDoctorStmt.setInt(1, doctorID);
                                insertDoctorStmt.setString(2, txtSpecialisation.getText().trim());

                                insertDoctorStmt.executeUpdate();
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(
                        null,
                        "Doctor Added Successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (SQLException ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                        null,
                        "Failed to add doctor!",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                    );

                    return;
                }
            
            }
        });

        btnAssignSpecialisation.addActionListener(e -> {

            String doctorIdInput = JOptionPane.showInputDialog(
                "Enter Doctor ID:"
            );
        
            String newSpecialisation = JOptionPane.showInputDialog(
                "Enter New Specialisation:"
            );
        
            if (doctorIdInput == null ||
                newSpecialisation == null ||
                doctorIdInput.trim().isEmpty() ||
                newSpecialisation.trim().isEmpty()) {
        
                JOptionPane.showMessageDialog(
                    null,
                    "Invalid Input!",
                    "Failure",
                    JOptionPane.ERROR_MESSAGE
                );
        
                return;
            }
        
            int doctorID;

            try {

                doctorID = Integer.parseInt(doctorIdInput.trim());

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                    null,
                    "Doctor ID must be a number!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            Connection conn = DBConnection.getConnection();

            if (conn == null) {
                return;
            }

            String updateQuery =
            "UPDATE doctor SET doctor_specialisation = ? WHERE doctor_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

                updateStmt.setString(1, newSpecialisation);
                updateStmt.setInt(2, doctorID);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {

                    JOptionPane.showMessageDialog(
                    null,
                    "Specialisation Updated Successfully!"
                    );

                } else {

                    JOptionPane.showMessageDialog(
                        null,
                        "Doctor Not Found!",
                        "Failure",
                    JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (SQLException ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                    null,
                    "Database Error!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnClear.addActionListener(e -> {
            txtName.setText("");
            txtUsername.setText("");
            txtPassword.setText("");
            txtSpecialisation.setText("");
        });

        panel.add(lblName);
        panel.add(txtName);

        panel.add(lblUsername);
        panel.add(txtUsername);

        panel.add(lblPassword);
        panel.add(txtPassword);

        panel.add(lblSpecialisation);
        panel.add(txtSpecialisation);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(btnAddDoctor);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnViewDoctors);
        buttonPanel.add(btnAssignSpecialisation);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ManageDoctorsFrame().setVisible(true);
            }
        });
    }
}

