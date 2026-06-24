import javax.swing.*;
import java.awt.*;

public class ManageDoctorsFrame extends JFrame {

    public ManageDoctorsFrame() {

        setTitle("Manage Doctors");
        setSize(500, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 5, 5));

        JLabel lblDoctorID = new JLabel("Doctor ID:");
        JTextField txtDoctorID = new JTextField();

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

        btnViewDoctors.addActionListener(e -> {

            new ViewDoctorsFrame().setVisible(true);
        
        });
        
        btnAddDoctor.addActionListener(e -> {

            if (txtDoctorID.getText().trim().isEmpty() ||
                txtName.getText().trim().isEmpty() ||
                txtUsername.getText().trim().isEmpty() ||
                new String(txtPassword.getPassword()).trim().isEmpty() ||
                txtSpecialisation.getText().trim().isEmpty()) {
        
                JOptionPane.showMessageDialog(
                    null,
                    "Please fill in all fields!",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
                );
        
            } else {
        
                int doctorID = Integer.parseInt(txtDoctorID.getText());

                Doctor doctor = new Doctor(
                doctorID,
                txtName.getText(),
                txtSpecialisation.getText()
                );

                DoctorList.doctors.add(doctor);

                JOptionPane.showMessageDialog(
                null,
                "Doctor Added Successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        btnClear.addActionListener(e -> {
            txtDoctorID.setText("");
            txtName.setText("");
            txtUsername.setText("");
            txtPassword.setText("");
            txtSpecialisation.setText("");
        });

        panel.add(lblDoctorID);
        panel.add(txtDoctorID);

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

