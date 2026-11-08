package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Income extends Transaction {

    public Income(int userId,
                  int categoryId,
                  BigDecimal amount,
                  String description,
                  LocalDate date) {

        super(userId, categoryId, amount, description, date);
    }

    @Override
    public BigDecimal calculateImpact() {
        return amount;
    }

    @Override
    public String getType() {
        return "INCOME";
    }
}
