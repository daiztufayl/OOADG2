// Session is a holder for "who's logged in right now"
public class Session {

    private static String role;
    private static int userId;
    private static String username;
    private static String name;

    private Session() {
        // utility class, no instances
    }

    public static void setCurrentUser(String role, int userId, String username, String name) {
        Session.role = role;
        Session.userId = userId;
        Session.username = username;
        Session.name = name;
    }

    public static String getRole() {
        return role;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getName() {
        return name;
    }

    public static void clear() {
        role = null;
        userId = 0;
        username = null;
        name = null;
    }
}
