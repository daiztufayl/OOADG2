package SystemController;

import java.sql.*;
import javax.swing.*;

public class SystemController {
    public static Session currentUser;

    private static final String DATABASEURL = "jdbc:postgresql://localhost:5432/smartequip";
    private static final String USERNAME = "postgres"; // idk just use default postgres user should be fine
    private static final String PASSWORD = "Zxcv123"; // change to password for the default postgres user 'postgres'
    // (default postgres password is "":nothing)
    private static Connection conn;

    public SystemController() {
        new RoleLookup();
        new Landing();
    }

    public static void setCurrentUser(Session currUser) {
        currentUser = currUser;
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
    
    // validates that an input contains only alphanumerical characters
    protected static boolean alphaNumValidate(String input) {
        return input.matches("^[A-Za-z0-9]+$");
    }

    public static void redirectLanding(){
        new Landing();
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
