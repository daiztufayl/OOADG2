package model;

import view.ReceptionistDashboard;

public class Receptionist extends User {
    public Receptionist(int userId, String username, String name) {
        super(userId, username, name, "RC");
    }

    @Override
    public void openDashboard() {
        new ReceptionistDashboard().setVisible(true);
    }
}