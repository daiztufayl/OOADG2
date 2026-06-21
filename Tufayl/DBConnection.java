import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

// one shared connection so we're not opening a new one in every class
public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/hms";
    private static final String USERNAME = "postgres";
    
    // change this to whatever password you set for postgres locally example admin123
    private static final String PASSWORD = "admin";

    private static Connection connection;

    private DBConnection() {
        // utility class, no instances
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not connect to the database.\n" +
                            "Check that PostgreSQL is running and the URL/username/password in DBConnection.java are correct.\n\n" +
                            e.getMessage(),
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            connection = null;
        }
        return connection;
    }
}
