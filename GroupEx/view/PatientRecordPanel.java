package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Main entry point for the Patient Record Management module.
// Called from ReceptionistDashboard and DoctorDashboard.
public class PatientRecordPanel extends JFrame {

    public PatientRecordPanel() {
        setTitle("Patient Record Management");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel lblTitle = new JLabel("Patient Record Management", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 15));

        JButton btnAddPatient = new JButton("Add New Patient");
        JButton btnUpdatePatient = new JButton("Update Patient Details");
        JButton btnViewHistory = new JButton("View Patient History");

        btnAddPatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddPatientFrame().setVisible(true);
            }
        });

        btnUpdatePatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdatePatientFrame().setVisible(true);
            }
        });

        btnViewHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewPatientHistoryFrame().setVisible(true);
            }
        });

        menuPanel.add(btnAddPatient);
        menuPanel.add(btnUpdatePatient);
        menuPanel.add(btnViewHistory);
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        footerPanel.add(btnClose);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new PatientRecordPanel().setVisible(true);
            }
        });
    }
}