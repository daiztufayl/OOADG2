package SystemController;

import java.sql.*;
import javax.swing.*;

public class DBConnection {
    // initialises the connection details
    private static final String DATABASEURL = "jdbc:postgresql://localhost:5432/hms";
    private static final String USERNAME = "postgres"; // idk just use default postgres user should be fine
    private static final String PASSWORD = "admin"; // change to password for the default postgres user 'postgres'
    // (default postgres password is "":nothing)
    private static Connection conn;

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
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}