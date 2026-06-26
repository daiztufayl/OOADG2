package SystemController;

import java.sql.*;
import javax.swing.*;
import model.*;

public class SystemController {
    public static Session currentUser;

    private static final String DATABASEURL = "jdbc:postgresql://localhost:5432/hms";
    private static final String USERNAME = "postgres"; // idk just use default postgres user should be fine
    private static final String PASSWORD = "Zxcv123"; // change to password for the default postgres user 'postgres'
    // (default postgres password is "":nothing)
    private static Connection conn;

    public SystemController() {
        new Login();
    }

    public static void setCurrentUser(Session currUser) {
        currentUser = currUser;
        String role = currentUser.getRole();
        switch (role) {
            case "AD":
                Admin admin = new Admin(currentUser.getUserID(), currentUser.getUsername(), currentUser.getName());
                admin.openDashboard();
                break;
            case "DR":
                Doctor doctor = new Doctor(currentUser.getUserID(), currentUser.getUsername(), currentUser.getName(),
                        "");
                doctor.openDashboard();
                break;
            case "RC":
                Receptionist receptionist = new Receptionist(currentUser.getUserID(), currentUser.getUsername(),
                        currentUser.getName());
                receptionist.openDashboard();
                break;
        }
    }

    public static Session getCurrentUser() {
        return currentUser;
    }

    public static Connection getDBConnection() {
        if (conn == null) {
            try {
                conn = DriverManager.getConnection(DATABASEURL, USERNAME, PASSWORD);
            } catch (SQLException e) {

                JOptionPane.showMessageDialog(null, e.getMessage(), "Database Connection Failed",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                conn = null;
            }
        }
        return conn;
    }

    public static void closeDBConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void redirectLogin() {
        new Login();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SystemController();
            }
        });
    }
}
