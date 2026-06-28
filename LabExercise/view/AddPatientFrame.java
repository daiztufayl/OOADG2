package view;

import SystemController.DBConnection;
import model.Patient;
import model.PatientList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddPatientFrame extends JFrame {

    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbGender;
    private JTextArea txtMedicalHistory;

    public AddPatientFrame() {
        setTitle("Add New Patient");
        setSize(500, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("Register New Patient", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 10));

        JLabel lblName = new JLabel("Patient Name:");
        txtName = new JTextField();

        JLabel lblAge = new JLabel("Age:");
        txtAge = new JTextField();

        JLabel lblGender = new JLabel("Gender:");
        // removed "Other" — DB column is CHAR(1) and only accepts M or F
        cmbGender = new JComboBox<>(new String[]{"Male", "Female"});

        JLabel lblHistory = new JLabel("Medical History:");
        txtMedicalHistory = new JTextArea(3, 10);
        txtMedicalHistory.setLineWrap(true);
        txtMedicalHistory.setWrapStyleWord(true);
        JScrollPane historyScroll = new JScrollPane(txtMedicalHistory);

        formPanel.add(lblName);
        formPanel.add(txtName);
        formPanel.add(lblAge);
        formPanel.add(txtAge);
        formPanel.add(lblGender);
        formPanel.add(cmbGender);
        formPanel.add(lblHistory);
        formPanel.add(historyScroll);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnAdd = new JButton("Add Patient");
        JButton btnClear = new JButton("Clear");
        JButton btnClose = new JButton("Close");

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });

        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtName.setText("");
                txtAge.setText("");
                cmbGender.setSelectedIndex(0);
                txtMedicalHistory.setText("");
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnClose);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addPatient() {
        String name = txtName.getText().trim();
        String ageText = txtAge.getText().trim();
        String genderDisplay = (String) cmbGender.getSelectedItem();
        String history = txtMedicalHistory.getText().trim();

        if (name.isEmpty() || ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name and Age are required.",
                    "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 150) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid age (1 - 150).",
                    "Invalid Age", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // convert display value to single char before sending to DB
        // was the bug: code was passing "Male"/"Female" directly into a CHAR(1) column
        String genderChar = genderDisplay.equals("Male") ? "M" : "F";

        Connection conn = DBConnection.getDBConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "INSERT INTO patientrecords (patient_name, patient_age, patient_gender, medical_history) " +
                "VALUES (?, ?, ?, ?) RETURNING record_id";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, genderChar); // fixed: send "M" or "F", not the full word
            ps.setString(4, history);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("record_id");
                PatientList.patients.add(new Patient(newId, name, age, genderChar, history));
            }

            JOptionPane.showMessageDialog(this,
                    "Patient \"" + name + "\" registered successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            txtName.setText("");
            txtAge.setText("");
            cmbGender.setSelectedIndex(0);
            txtMedicalHistory.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to add patient. Please try again.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AddPatientFrame().setVisible(true);
            }
        });
    }
}