public class Session {
    private static String role;
    private static int user_id;
    private static String username;
    private static String name;

    // setter that initialises User Session
    public static void startUserSession(String role, int user_id, String username, String name) {
        Session.role = role;
        Session.user_id = user_id;
        Session.username = username;
        Session.name = name;
    }

    // getters
    public static String getRole() {
        return role;
    }
    public static int getUserID() {
        return user_id;
    }
    public static String getUsername() {
        return username;
    }
    public static String getName() {
        return name;
    }

    // sets values to null/-1 to indicate empty
    public static void closeUserSession(){
        role = username = name = null;
        user_id = -1;
    }
}
