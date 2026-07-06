package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentagePenalty extends BillAdjustment {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public PercentagePenalty(String name, BigDecimal percentage) {
        super(name, percentage);
    }

    @Override
    public BillLine.Type getType() {
        return BillLine.Type.PENALTY;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return amount.multiply(getValue())
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }
}
