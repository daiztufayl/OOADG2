package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Handles bill calculation rules
public class BillingService {
    private final BillingRepository repository;

    // DIP: the service receives its data source instead of creating it.
    public BillingService(BillingRepository repository) {
        this.repository = repository;
    }

    public Bill calculateCurrentBill(int userId, Integer conditionPenaltyId) throws SQLException {
        RentalBillingData rental = repository.findCurrentRental(userId);

        if (rental == null) {
            return null;
        }

        LocalDate billDate = rental.getEndDate() == null
                ? LocalDate.now()
                : rental.getEndDate();

        int rentalDays = calculateRentalDays(rental.getStartDate(), billDate);
        BigDecimal baseFee = rental.getDailyRate()
                .multiply(BigDecimal.valueOf(rentalDays))
                .setScale(2, RoundingMode.HALF_UP);

        Bill.Builder builder = new Bill.Builder()
                .rental(rental.getRentalId(), rental.getEquipmentName(), rental.getCategoryName())
                .rentalPeriod(rental.getStartDate(), billDate, rentalDays)
                .baseCharge(rental.getDailyRate(), baseFee);

        BigDecimal discountedAmount = baseFee;
        List<BillAdjustment> pricingRules = repository.loadRoleAdjustments(rental.getRole());

        // OCP and polymorphism: the service works with the abstract rule type.
        for (BillAdjustment rule : pricingRules) {
            BigDecimal discount = rule.calculate(discountedAmount);
            builder.addAdjustment(new BillLine(rule.getName(), rule.getType(), discount));
            discountedAmount = discountedAmount.subtract(discount).max(BigDecimal.ZERO);
        }

        if (rentalDays > rental.getMaxRentalDays()) {
            BillAdjustment latePenalty = repository.loadLatePenalty(rental.getCategoryId());

            if (latePenalty != null) {
                BigDecimal penalty = latePenalty.calculate(baseFee);
                builder.addAdjustment(new BillLine(
                        latePenalty.getName(),
                        latePenalty.getType(),
                        penalty
                ));
            }
        }

        if (conditionPenaltyId != null) {
            BillAdjustment conditionPenalty = repository.loadPenalty(
                    rental.getCategoryId(),
                    conditionPenaltyId
            );

            if (conditionPenalty != null) {
                BigDecimal penalty = conditionPenalty.calculate(baseFee);
                builder.addAdjustment(new BillLine(
                        conditionPenalty.getName(),
                        conditionPenalty.getType(),
                        penalty
                ));
            }
        }

        return builder.build();
    }

    private int calculateRentalDays(LocalDate startDate, LocalDate billDate) {
        long days = ChronoUnit.DAYS.between(startDate, billDate) + 1;
        return (int) Math.max(1, days);
    }
}