package billing;

import java.math.BigDecimal;
import java.time.LocalDate;

// Encapsulation: rental data needed by billing stays in one object.
public class RentalBillingData {
    private final int rentalId;
    private final int categoryId;
    private final String equipmentName;
    private final String categoryName;
    private final String role;
    private final BigDecimal dailyRate;
    private final int maxRentalDays;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public RentalBillingData(int rentalId, int categoryId, String equipmentName,
                             String categoryName, String role, BigDecimal dailyRate,
                             int maxRentalDays, LocalDate startDate, LocalDate endDate) {
        this.rentalId = rentalId;
        this.categoryId = categoryId;
        this.equipmentName = equipmentName;
        this.categoryName = categoryName;
        this.role = role;
        this.dailyRate = dailyRate;
        this.maxRentalDays = maxRentalDays;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getRentalId() {
        return rentalId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getRole() {
        return role;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public int getMaxRentalDays() {
        return maxRentalDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
