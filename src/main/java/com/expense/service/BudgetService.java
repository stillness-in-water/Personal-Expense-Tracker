package com.expense.service;

import com.expense.dao.BudgetDAO;
import com.expense.dao.CategoryDAO;
import com.expense.dao.TransactionDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Budget;
import com.expense.model.BudgetStatus;
import com.expense.model.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;
    private final CategoryDAO categoryDAO;

    public BudgetService() {
        this(new BudgetDAO(), new TransactionDAO(), new CategoryDAO());
    }

    public BudgetService(BudgetDAO budgetDAO,
                         TransactionDAO transactionDAO,
                         CategoryDAO categoryDAO) {

        this.budgetDAO = budgetDAO;
        this.transactionDAO = transactionDAO;
        this.categoryDAO = categoryDAO;
    }

    public synchronized void addBudget(Budget budget) throws Exception {

        TransactionService.requireValidAmount(
                budget.getAmountLimit(), "Budget amount");

        if (budget.getMonth() < 1 || budget.getMonth() > 12) {
            throw new ValidationException(
                    "Month must be between 1 and 12."
            );
        }

        if (budget.getYear() < 2000 || budget.getYear() > 2100) {
            throw new ValidationException(
                    "Year must be between 2000 and 2100."
            );
        }

        if (budget.getCategoryId() != null) {

            Category category = categoryDAO
                    .findById(budget.getCategoryId())
                    .orElseThrow(() -> new ValidationException(
                            "The selected category does not exist."
                    ));

            if (category.getUserId() != budget.getUserId()
                    || !"EXPENSE".equals(category.getType())) {

                throw new ValidationException(
                        "Choose one of your expense categories."
                );
            }
        }

        boolean duplicate = budgetDAO.findByUser(budget.getUserId())
                .stream()
                .anyMatch(existing ->
                        Objects.equals(existing.getCategoryId(),
                                budget.getCategoryId())
                                && existing.getMonth() == budget.getMonth()
                                && existing.getYear() == budget.getYear());

        if (duplicate) {
            throw new ValidationException(
                    "A budget for that category and month already exists."
            );
        }

        budgetDAO.save(budget);
    }

    public List<Budget> getBudgets(int userId) throws Exception {
        return budgetDAO.findByUser(userId);
    }

    public List<BudgetStatus> getBudgetStatuses(int userId)
            throws Exception {

        List<BudgetStatus> statuses = new ArrayList<>();

        for (Budget budget : budgetDAO.findByUser(userId)) {

            BigDecimal spent = transactionDAO.getExpenseTotal(
                    userId,
                    budget.getCategoryId(),
                    budget.getMonth(),
                    budget.getYear()
            );

            statuses.add(new BudgetStatus(budget, spent));
        }

        return statuses;
    }
}
