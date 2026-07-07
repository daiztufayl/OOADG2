package billing;

import java.math.BigDecimal;

// Abstraction for billing
public abstract class BillAdjustment {
    private final String name;
    private final BigDecimal value;

    protected BillAdjustment(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    protected BigDecimal getValue() {
        return value;
    }

    public abstract BillLine.Type getType();

    // Polymorphism: each subclass calculates its own adjustment.
    public abstract BigDecimal calculate(BigDecimal amount);
}
