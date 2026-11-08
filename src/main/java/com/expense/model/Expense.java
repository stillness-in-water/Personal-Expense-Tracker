package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense extends Transaction {

    public Expense(int userId,
                   int categoryId,
                   BigDecimal amount,
                   String description,
                   LocalDate date) {

        super(userId, categoryId, amount, description, date);
    }

    @Override
    public BigDecimal calculateImpact() {
        return amount.negate();
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }
}
