package com.expense.service;

import com.expense.dao.TransactionDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public class TransactionService {

    private final TransactionDAO dao = new TransactionDAO();

    public void addTransaction(Transaction transaction)
            throws Exception {

        if (transaction.getAmount() == null ||
                transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ValidationException(
                    "Amount must be greater than zero."
            );
        }

        if (transaction.getCategoryId() <= 0) {
            throw new ValidationException(
                    "Select a category."
            );
        }

        dao.save(transaction);
    }

    public List<Transaction> getTransactions(int userId)
            throws Exception {

        return dao.findByUser(userId);
    }

    public BigDecimal getIncome(int userId)
            throws Exception {

        return dao.getTotalByType(userId, "INCOME");
    }

    public BigDecimal getExpenses(int userId)
            throws Exception {

        return dao.getTotalByType(userId, "EXPENSE");
    }

    public BigDecimal getBalance(int userId)
            throws Exception {

        return getIncome(userId)
                .subtract(getExpenses(userId));
    }
}
