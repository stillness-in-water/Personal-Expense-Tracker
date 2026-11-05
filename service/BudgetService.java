package com.expense.service;

import com.expense.dao.BudgetDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Budget;

import java.math.BigDecimal;
import java.util.List;

public class BudgetService {

    private final BudgetDAO dao = new BudgetDAO();

    public void addBudget(Budget budget)
            throws Exception {

        if (budget.getAmountLimit() == null ||
                budget.getAmountLimit()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new ValidationException(
                    "Budget amount must be greater than zero."
            );
        }

        if (budget.getMonth() < 1 ||
                budget.getMonth() > 12) {

            throw new ValidationException(
                    "Month must be between 1 and 12."
            );
        }

        dao.save(budget);
    }

    public List<Budget> getBudgets(int userId)
            throws Exception {

        return dao.findByUser(userId);
    }
}
