package billing;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BillLine {
    public enum Type {
        DISCOUNT,
        PENALTY
    }

    private final String description;
    private final Type type;
    private final BigDecimal amount;

    public BillLine(String description, Type type, BigDecimal amount) {
        this.description = description;
        this.type = type;
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
