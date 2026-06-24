public class Doctor {

    private int doctorID;
    private String doctorName;
    private String specialization;

    public Doctor(int doctorID, String doctorName, String specialization) {
        this.doctorID = doctorID;
        this.doctorName = doctorName;
        this.specialization = specialization;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return doctorID + " - " + doctorName + " (" + specialization + ")";
    }
}