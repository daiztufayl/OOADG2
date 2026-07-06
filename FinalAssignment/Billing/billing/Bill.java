package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bill {
    private final int rentalId;
    private final String equipmentName;
    private final String categoryName;
    private final LocalDate startDate;
    private final LocalDate billDate;
    private final int rentalDays;
    private final BigDecimal dailyRate;
    private final BigDecimal baseFee;
    private final List<BillLine> lines;
    private final BigDecimal totalDiscount;
    private final BigDecimal totalPenalty;
    private final BigDecimal netPayable;

    private Bill(Builder builder) {
        rentalId = builder.rentalId;
        equipmentName = builder.equipmentName;
        categoryName = builder.categoryName;
        startDate = builder.startDate;
        billDate = builder.billDate;
        rentalDays = builder.rentalDays;
        dailyRate = money(builder.dailyRate);
        baseFee = money(builder.baseFee);
        lines = Collections.unmodifiableList(new ArrayList<>(builder.lines));

        BigDecimal discounts = BigDecimal.ZERO;
        BigDecimal penalties = BigDecimal.ZERO;

        for (BillLine line : lines) {
            if (line.getType() == BillLine.Type.DISCOUNT) {
                discounts = discounts.add(line.getAmount());
            } else {
                penalties = penalties.add(line.getAmount());
            }
        }

        totalDiscount = money(discounts);
        totalPenalty = money(penalties);

        BigDecimal payable = baseFee.subtract(totalDiscount).add(totalPenalty);
        netPayable = money(payable.max(BigDecimal.ZERO));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public int getRentalId() {
        return rentalId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public List<BillLine> getLines() {
        return lines;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public BigDecimal getTotalPenalty() {
        return totalPenalty;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    // Builder pattern: the bill is built one part at a time.
    public static class Builder {
        private int rentalId;
        private String equipmentName;
        private String categoryName;
        private LocalDate startDate;
        private LocalDate billDate;
        private int rentalDays;
        private BigDecimal dailyRate = BigDecimal.ZERO;
        private BigDecimal baseFee = BigDecimal.ZERO;
        private final List<BillLine> lines = new ArrayList<>();

        public Builder rental(int rentalId, String equipmentName, String categoryName) {
            this.rentalId = rentalId;
            this.equipmentName = equipmentName;
            this.categoryName = categoryName;
            return this;
        }

        public Builder rentalPeriod(LocalDate startDate, LocalDate billDate, int rentalDays) {
            this.startDate = startDate;
            this.billDate = billDate;
            this.rentalDays = rentalDays;
            return this;
        }

        public Builder baseCharge(BigDecimal dailyRate, BigDecimal baseFee) {
            this.dailyRate = dailyRate;
            this.baseFee = baseFee;
            return this;
        }

        public Builder addAdjustment(BillLine line) {
            lines.add(line);
            return this;
        }

        public Bill build() {
            if (equipmentName == null || categoryName == null || startDate == null || billDate == null) {
                throw new IllegalStateException("Bill details are incomplete.");
            }

            if (rentalDays < 1 || dailyRate.compareTo(BigDecimal.ZERO) < 0
                    || baseFee.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Bill values are invalid.");
            }

            return new Bill(this);
        }
    }
}
