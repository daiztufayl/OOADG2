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
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Header Text
        // greet the admin who actually logged in, name getting from database
        String adminName = (Session.getName() != null) ? Session.getName() : "Admin";
        JLabel lblHeader = new JLabel("Welcome, " + adminName, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Dashboard Stats
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 10, 15));

        // Reporting
        // pulled from the database
        JLabel lblTotalPatients = new JLabel("Total Patients Registered: " + getTotalPatients(), SwingConstants.CENTER);
        JLabel lblTotalAppointments = new JLabel("Total Active Appointments: " + getTotalActiveAppointments(), SwingConstants.CENTER);
        lblTotalPatients.setFont(new Font("Arial", Font.PLAIN, 16));
        lblTotalAppointments.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton btnManageDoctors = new JButton("Manage Doctors");
        JButton btnViewSchedules = new JButton("View Doctor Schedules");

        // Button to (Manage Doctors)
        btnManageDoctors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // new ManageDoctorsFrame().setVisible(true);
                JOptionPane.showMessageDialog(null, "Placeholder: Opening Manage Doctors Window");
            }
        });

        // Button to (View Schedules)
        btnViewSchedules.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // need viewdoctorschedule.java
                // new ViewDoctorSchedulesFrame().setVisible(true);
                JOptionPane.showMessageDialog(null, "Placeholder: Opening Master Schedule Viewer");
            }
        });

        centerPanel.add(lblTotalPatients);
        centerPanel.add(lblTotalAppointments);
        centerPanel.add(btnManageDoctors);
        centerPanel.add(btnViewSchedules);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Logout
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");

        // Logout and return to Login Screen
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Logging out... Returning to Login Screen.");

                Session.clear(); // forget who was logged in
                dispose(); // Closes the Admin Dashboard
                new Login(); // back to the login screen
            }
        });

        footerPanel.add(btnLogout);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // counts rows in patientRecords, the data is connected to database
    private int getTotalPatients() {
        String query = "SELECT COUNT(*) AS total FROM patientRecords";
        Connection conn = DBConnection.getConnection();
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

    // counts rows in the activeAppointments view (appointments with status = true)
    private int getTotalActiveAppointments() {
        String query = "SELECT COUNT(*) AS total FROM activeAppointments";
        Connection conn = DBConnection.getConnection();
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
