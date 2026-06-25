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

public class UpdatePatientFrame extends JFrame {

    private JTextField txtSearchId;
    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbGender;
    private JTextArea txtMedicalHistory;
    private JLabel statusLabel;

    private int currentRecordId = -1; // tracks which patient is loaded

    public UpdatePatientFrame() {
        setTitle("Update Patient Details");
        setSize(520, 440);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));

        JLabel lblTitle = new JLabel("Update Patient Details", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // search bar at the top of the form area
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.add(new JLabel("Patient ID:"));
        txtSearchId = new JTextField(8);
        JButton btnSearch = new JButton("Search");
        searchPanel.add(txtSearchId);
        searchPanel.add(btnSearch);

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPatient();
            }
        });

        // editable fields (disabled until a patient is found)
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel lblName = new JLabel("Name:");
        txtName = new JTextField();
        txtName.setEnabled(false);

        JLabel lblAge = new JLabel("Age:");
        txtAge = new JTextField();
        txtAge.setEnabled(false);

        JLabel lblGender = new JLabel("Gender:");
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbGender.setEnabled(false);

        JLabel lblHistory = new JLabel("Medical History:");
        txtMedicalHistory = new JTextArea(3, 10);
        txtMedicalHistory.setLineWrap(true);
        txtMedicalHistory.setWrapStyleWord(true);
        txtMedicalHistory.setEnabled(false);
        JScrollPane historyScroll = new JScrollPane(txtMedicalHistory);

        formPanel.add(lblName);
        formPanel.add(txtName);
        formPanel.add(lblAge);
        formPanel.add(txtAge);
        formPanel.add(lblGender);
        formPanel.add(cmbGender);
        formPanel.add(lblHistory);
        formPanel.add(historyScroll);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 8));
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnSave = new JButton("Save Changes");
        JButton btnClose = new JButton("Close");

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
            }
        });

        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnClose);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(statusLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void searchPatient() {
        String idText = txtSearchId.getText().trim();

        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Patient ID.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Patient ID must be a number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = DBConnection.getDBConnection();
        if (conn == null) {
            statusLabel.setText("Could not connect to the database.");
            return;
        }

        String query = "SELECT record_id, patient_name, patient_age, patient_gender, medical_history FROM patientrecords WHERE record_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentRecordId = rs.getInt("record_id");
                txtName.setText(rs.getString("patient_name"));
                txtAge.setText(String.valueOf(rs.getInt("patient_age")));

                // convert M/F/X from DB to Male/Female/Other for dropdown
                String gender = rs.getString("patient_gender");
                String genderDisplay;
                switch (gender != null ? gender.trim() : "X") {
                    case "M": genderDisplay = "Male"; break;
                    case "F": genderDisplay = "Female"; break;
                    default:  genderDisplay = "Other"; break;
                }
                cmbGender.setSelectedItem(genderDisplay);

                String history = rs.getString("medical_history");
                txtMedicalHistory.setText(history != null ? history : "");

                txtName.setEnabled(true);
                txtAge.setEnabled(true);
                cmbGender.setEnabled(true);
                txtMedicalHistory.setEnabled(true);

                statusLabel.setText("Patient found. Edit fields and click Save Changes.");
            } else {
                currentRecordId = -1;
                clearForm();
                statusLabel.setText("No patient found with ID " + id + ".");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error searching for patient.");
        }
    }
    private void saveChanges() {
        if (currentRecordId == -1) {
            JOptionPane.showMessageDialog(this,
                    "No patient loaded. Search for a patient first.",
                    "No Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String ageText = txtAge.getText().trim();
        String genderRaw = (String) cmbGender.getSelectedItem();
        String history = txtMedicalHistory.getText().trim();
        String gender;
        switch (genderRaw) {
            case "Male":   gender = "M"; break;
            case "Female": gender = "F"; break;
            default:       gender = "X"; break;
        }

        if (name.isEmpty() || ageText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Name and Age cannot be empty.",
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

        Connection conn = DBConnection.getDBConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Could not connect to the database.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "UPDATE patientrecords SET patient_name = ?, patient_age = ?, patient_gender = ?, medical_history = ? WHERE record_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            ps.setString(3, gender);
            ps.setString(4, history);
            ps.setInt(5, currentRecordId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // sync in-memory list if the patient exists there
                for (Patient p : PatientList.patients) {
                    if (p.getRecordId() == currentRecordId) {
                        p.setPatientName(name);
                        p.setAge(age);
                        p.setGender(gender);
                        p.setMedicalHistory(history);
                        break;
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Patient record updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Record updated.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Update failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error during update.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtAge.setText("");
        cmbGender.setSelectedIndex(0);
        txtMedicalHistory.setText("");
        txtName.setEnabled(false);
        txtAge.setEnabled(false);
        cmbGender.setEnabled(false);
        txtMedicalHistory.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UpdatePatientFrame().setVisible(true);
            }
        });
    }
}