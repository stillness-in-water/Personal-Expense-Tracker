package com.expense.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BudgetStatus(Budget budget, BigDecimal spent) {

    public BigDecimal remaining() {
        return budget.getAmountLimit().subtract(spent);
    }

    public boolean isExceeded() {
        return spent.compareTo(budget.getAmountLimit()) > 0;
    }

    public int percentUsed() {

        BigDecimal limit = budget.getAmountLimit();

        if (limit.signum() == 0) {
            return 0;
        }

        return spent
                .multiply(BigDecimal.valueOf(100))
                .divide(limit, 0, RoundingMode.HALF_UP)
                .intValue();
    }
}
