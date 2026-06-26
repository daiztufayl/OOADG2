package SystemController;
public class Session {
    private String role;
    private int user_id;
    private String username;
    private String name;

    // setter that initialises User Session
    public Session(String role, int user_id, String username, String name) {
        this.role = role;
        this.user_id = user_id;
        this.username = username;
        this.name = name;
    }

    // getters
    public String getRole() {
        return role;
    }

    public int getUserID() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    // sets values to null/-1 to indicate empty
    public void closeUserSession() {
        role = username = name = null;
        user_id = -1;
    }
}
