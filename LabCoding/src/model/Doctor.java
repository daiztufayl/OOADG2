package model;

import view.DoctorDashboard;

public class Doctor extends User {
    private String specialisation;

    public Doctor(int userId, String username, String name, String specialisation) {
        super(userId, username, name, "DR");
        this.specialisation = specialisation;
    }

    public String getSpecialisation() { return specialisation; }
    public void setSpecialisation(String s) { this.specialisation = s; }

    @Override
    public void openDashboard() {
        new DoctorDashboard().setVisible(true);
    }
}