package model;

import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private int doctorId;
    private int recordId;
    private LocalDateTime appointmentTime;
    private String diagnosis;
    private String prescription;
    private boolean status;

    public Appointment(int appointmentId, int doctorId, int recordId,
                       LocalDateTime appointmentTime,
                       String diagnosis, String prescription,
                       boolean status) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.recordId = recordId;
        this.appointmentTime = appointmentTime;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getRecordId() {
        return recordId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public boolean isStatus() {
        return status;
    }

    public void setAppointment(String diagnosis, String prescription) {
        this.diagnosis = diagnosis;
        this.prescription = prescription;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getStatusLabel() {
        return status ? "Active" : "Inactive";
    }
}