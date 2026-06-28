package view;

import SystemController.Session;
import SystemController.Login;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class DoctorDashboard extends JFrame {
    public DoctorDashboard() {

        super("Doctor Dashboard");

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // greet the doctor who actually logged in, name pulled from Session
        String doctorName = (Session.getName() != null) ? Session.getName() : "Doctor";
        JLabel headerLabel = new JLabel("Welcome, Dr. " + doctorName, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 20));

        JButton btnViewSchedule = new JButton("Manage Schedule & Availability");
        btnViewSchedule.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnUpdateRecords = new JButton("Update Patient Records");
        btnUpdateRecords.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnActiveAppointments = new JButton("View Active Appointments");
        btnActiveAppointments.setFont(new Font("Dialog", Font.PLAIN, 18));

        // opens ManageScheduleFrame — removed JOptionPane that was blocking the window
        btnViewSchedule.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ManageScheduleFrame(Session.getUserId()).setVisible(true);
            }
        });

        btnUpdateRecords.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new DoctorPatientPanel().setVisible(true);
            }
        });

        // opens ViewAppointmentsFrame filtered to this doctor — removed JOptionPane that was blocking the window
        btnActiveAppointments.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ViewAppointmentsFrame(Session.getUserId()).setVisible(true);
            }
        });

        menuPanel.add(btnViewSchedule);
        menuPanel.add(btnUpdateRecords);
        menuPanel.add(btnActiveAppointments);
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Dialog", Font.PLAIN, 16));

        // logout — removed JOptionPane, just clear session and go back to login
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Session.clear();
                dispose();
                new Login();
            }
        });

        navPanel.add(btnLogout);
        mainPanel.add(navPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    public static void main(String[] args) {
        new DoctorDashboard();
    }
}