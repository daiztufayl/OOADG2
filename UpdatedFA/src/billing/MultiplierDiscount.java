package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MultiplierDiscount extends BillAdjustment {
    public MultiplierDiscount(String name, BigDecimal multiplier) {
        super(name, multiplier);
    }

    @Override
    public BillLine.Type getType() {
        return BillLine.Type.DISCOUNT;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        BigDecimal adjusted = amount.multiply(getValue());
        return amount.subtract(adjusted).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
