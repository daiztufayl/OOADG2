import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

// Single shared place to get a database connection from.
// Anywhere in the app that needs the DB (Login, dashboards, appointment
// booking later) just calls DBConnection.getConnection() instead of
// each class opening its own connection.
public class DBConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/hms";
    private static final String USERNAME = "postgres";

    // TODO: replace this with the postgres password you set when you
    // installed PostgreSQL on your machine. An empty password only works
    // if your local server is configured for "trust" authentication.
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