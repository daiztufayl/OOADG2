package view;

import SystemController.Login;
import SystemController.DBConnection;
import SystemController.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminDashboard extends JFrame {
    public AdminDashboard() {

        setTitle("Admin Dashboard");
        setSize(600, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Header — greet the admin who actually logged in, name pulled from Session
        String adminName = (Session.getName() != null) ? Session.getName() : "Admin";
        JLabel lblHeader = new JLabel("Welcome, " + adminName, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Dashboard Stats + Buttons
        JPanel centerPanel = new JPanel(new GridLayout(5, 1, 10, 15));

        // Reporting — pulled from database
        JLabel lblTotalPatients = new JLabel("Total Patients Registered: " + getTotalPatients(), SwingConstants.CENTER);
        JLabel lblTotalAppointments = new JLabel("Total Active Appointments: " + getTotalActiveAppointments(), SwingConstants.CENTER);
        lblTotalPatients.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalAppointments.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnManageDoctors = new JButton("Manage Doctors");
        btnManageDoctors.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnViewDoctors = new JButton("View Doctors");
        btnViewDoctors.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnViewAppointments = new JButton("View All Appointments");
        btnViewAppointments.setFont(new Font("Dialog", Font.PLAIN, 18));

        // Opens Manage Doctors window
        btnManageDoctors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ManageDoctorsFrame().setVisible(true);
            }
        });

        // Opens doctor list (ID, Name, Specialisation)
        btnViewDoctors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewDoctorsFrame().setVisible(true);
            }
        });

        // Opens all active appointments — admin sees everything
        btnViewAppointments.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewAppointmentsFrame().setVisible(true);
            }
        });

        centerPanel.add(lblTotalPatients);
        centerPanel.add(lblTotalAppointments);
        centerPanel.add(btnManageDoctors);
        centerPanel.add(btnViewDoctors);
        centerPanel.add(btnViewAppointments);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Logout
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Dialog", Font.PLAIN, 16));

        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Session.clear(); // forget who was logged in
                dispose();       // close Admin Dashboard
                new Login();     // back to login screen
            }
        });

        footerPanel.add(btnLogout);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // counts rows in patientRecords table
    private int getTotalPatients() {
        String query = "SELECT COUNT(*) AS total FROM patientrecords";
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return 0;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // counts rows in activeAppointments view (status = true)
    private int getTotalActiveAppointments() {
        String query = "SELECT COUNT(*) AS total FROM activeappointments";
        Connection conn = DBConnection.getDBConnection();
        if (conn == null) return 0;

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { new AdminDashboard().setVisible(true); }
        });
    }
}