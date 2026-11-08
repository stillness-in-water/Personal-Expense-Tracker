package com.expense.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {

    @Test
    void incomeShouldHavePositiveImpact() {

        Transaction income =
                new Income(
                        1,
                        1,
                        new BigDecimal("5000"),
                        "Salary",
                        LocalDate.now()
                );

        assertEquals(
                new BigDecimal("5000"),
                income.calculateImpact()
        );
    }

    @Test
    void expenseShouldHaveNegativeImpact() {

        Transaction expense =
                new Expense(
                        1,
                        1,
                        new BigDecimal("1200"),
                        "Food",
                        LocalDate.now()
                );

        assertEquals(
                new BigDecimal("-1200"),
                expense.calculateImpact()
        );
    }
}
