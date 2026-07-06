package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedDiscount extends BillAdjustment {
    public FixedDiscount(String name, BigDecimal value) {
        super(name, value);
    }

    @Override
    public BillLine.Type getType() {
        return BillLine.Type.DISCOUNT;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return getValue().min(amount).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
