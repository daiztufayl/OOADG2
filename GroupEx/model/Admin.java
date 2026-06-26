package model;

import view.AdminDashboard;

public class Admin extends User {
    public Admin(int userId, String username, String name) {
        super(userId, username, name, "AD");
    }

    @Override
    public void openDashboard() {
        new AdminDashboard().setVisible(true);
    }
}