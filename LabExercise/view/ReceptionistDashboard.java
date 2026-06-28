package view;

import SystemController.Session;
import SystemController.Login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ReceptionistDashboard extends JFrame {

    public ReceptionistDashboard() {

        setTitle("Receptionist Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // greet the receptionist who actually logged in, name taken from database via Session
        String receptionistName = (Session.getName() != null) ? Session.getName() : "Receptionist";
        JLabel lblHeader = new JLabel("Welcome, " + receptionistName, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 20));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnRegisterPatient = new JButton("Register New Patient");
        btnRegisterPatient.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnBookAppointment = new JButton("Book Appointment");
        btnBookAppointment.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnViewAppointments = new JButton("View All Appointments");
        btnViewAppointments.setFont(new Font("Dialog", Font.PLAIN, 18));

        btnRegisterPatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PatientRecordPanel().setVisible(true);
            }
        });

        btnBookAppointment.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BookAppointmentFrame().setVisible(true);
            }
        });

        btnViewAppointments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewAppointmentsFrame().setVisible(true);
            }
        });

        menuPanel.add(btnRegisterPatient);
        menuPanel.add(btnBookAppointment);
        menuPanel.add(btnViewAppointments);
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        // Logout — removed JOptionPane that was blocking the window from closing properly
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Dialog", Font.PLAIN, 16));

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Session.clear(); // forget who was logged in
                dispose();       // close Receptionist Dashboard
                new Login();     // back to login screen
            }
        });

        footerPanel.add(btnLogout);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { new ReceptionistDashboard().setVisible(true); }
        });
    }
}