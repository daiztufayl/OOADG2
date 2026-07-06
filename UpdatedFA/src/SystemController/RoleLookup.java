package SystemController;

import java.sql.*;
import java.util.ArrayList;

public class RoleLookup { // store list of roles + relationship with id
    private Connection conn;
    private static ArrayList<String> roleList = new ArrayList<>();

    public RoleLookup() {
        this.conn = SystemController.getDBConnection();
        String lookupString = "SELECT role_name FROM role ORDER BY role_id";

        roleList.clear();
        roleList.add("AD"); // AD conveniently able to fill out index 0 so id mapping is 1:1

        try {
            Statement lookup = conn.createStatement();
            ResultSet roleMapping = lookup.executeQuery(lookupString);

            while (roleMapping.next()) {
                roleList.add(roleMapping.getString("role_name")); // adds roles in role table to list
            }
            lookup.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> getRoleList() { // getter function to retrieve roleList
        return roleList;
    }

    public static String roleLookup(int roleID) { // get role_name from role_id
        try {
            roleList.get(roleID);
            return roleList.get(roleID);
        } catch (Exception e) {
            return "";
        }
    }

    public static int roleIDLookup(String role) { // get role_id from role_name
        int roleInt;
        for (roleInt = 0; roleInt < roleList.size(); roleInt++) {
            if (roleList.get(roleInt).equals(role)) {
                return roleInt;
            }
        }
        return -1;
    }
}