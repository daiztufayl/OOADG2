package model;

import java.time.LocalTime;

public class TimeSlot {
    private int timeSlotId;
    private int day;
    private LocalTime time;
    private boolean availability;

    public TimeSlot(int timeSlotId, int day, LocalTime time, boolean availability) {
        this.timeSlotId = timeSlotId;
        this.day = day;
        this.time = time;
        this.availability = availability;
    }

    public int getTimeSlotId() {
        return timeSlotId;
    }

    public int getDay() {
        return day;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getDayName() {
        switch (day) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
            default: return "Unknown";
        }
    }
}