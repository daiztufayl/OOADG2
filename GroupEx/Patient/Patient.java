public class Patient {

    private int recordId;
    private String patientName;
    private int age;
    private String gender;
    private String medicalHistory;

    public Patient(int recordId, String patientName, int age, String gender, String medicalHistory) {
        this.recordId = recordId;
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.medicalHistory = medicalHistory;
    }

    public int getRecordId() {
        return recordId;
    }

    public String getPatientName() {
        return patientName;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    @Override
    public String toString() {
        return recordId + " - " + patientName + " (" + age + ", " + gender + ")";
    }
}
