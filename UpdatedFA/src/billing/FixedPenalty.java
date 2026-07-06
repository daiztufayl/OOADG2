package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FixedPenalty extends BillAdjustment {
    public FixedPenalty(String name, BigDecimal value) {
        super(name, value);
    }

    @Override
    public BillLine.Type getType() {
        return BillLine.Type.PENALTY;
    }

    @Override
    public BigDecimal calculate(BigDecimal amount) {
        return getValue().max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
