import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class DoctorDashboard extends JFrame {
    public DoctorDashboard(){

        super("Doctor Dashboard"); // Title of UI

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensures the app actually stops running when closed
        setLocationRelativeTo(null); // Centers the window on the screen

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // greet the doctor who actually logged in, name get from database
        String doctorName = (Session.getName() != null) ? Session.getName() : "Doctor"; //session getname from here
        JLabel headerLabel = new JLabel("Welcome, Dr. " + doctorName, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 20));
        JButton btnViewSchedule = new JButton("Manage Schedule & Availability");
        JButton btnUpdateRecords = new JButton("Update Patient Records");
        JButton btnActiveAppointments = new JButton("View Active Appointments");

        // Button to open ManageScheduleFrame, ViewPatientFrame and ActiveAppointmentsFrame
        btnViewSchedule.addActionListener(new ActionListener() // Button For Doctor's Schedule
        {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Opening Manage Schedule & Availability");
                // open management page
                // new ManageScheduleFrame(Session.getUserId()).setVisible(true);
            }
        });

        btnUpdateRecords.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {
                new PatientRecordPanel().setVisible(true);
        }
        });

        btnActiveAppointments.addActionListener(new ActionListener() // Button For Appointment
        {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Opening Active Appointments");
                // open ViewAppointments page, data received from db
                new ViewAppointmentsFrame(Session.getUserId()).setVisible(true); // straight get patient id to pull out info saved from db like schedule etc
            }
        });

        menuPanel.add(btnViewSchedule);
        menuPanel.add(btnUpdateRecords);
        menuPanel.add(btnActiveAppointments);
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");

        btnLogout.addActionListener(new ActionListener()  // Button to log out
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(null, "Log out successfully");
                Session.clear(); // forget who was logged in
                dispose(); // Closes the dashboard window
                new Login(); // back to the login screen
            }
        });
        navPanel.add(btnLogout);
        mainPanel.add(navPanel, BorderLayout.SOUTH);

        // Add the main container to the JFrame
        add(mainPanel);

        setVisible(true);
    }
    public static void main(String[] args)
    {
        new DoctorDashboard();
    }

}