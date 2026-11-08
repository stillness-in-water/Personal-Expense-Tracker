package com.expense.service;

import com.expense.dao.CategoryDAO;
import com.expense.dao.TransactionDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Category;
import com.expense.model.Transaction;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class TransactionService {

  
    private static final BigDecimal MAX_AMOUNT =
            new BigDecimal("9999999999.99");

    private final TransactionDAO dao;
    private final CategoryDAO categoryDAO;

    public TransactionService() {
        this(new TransactionDAO(), new CategoryDAO());
    }

    public TransactionService(TransactionDAO dao,
                              CategoryDAO categoryDAO) {
        this.dao = dao;
        this.categoryDAO = categoryDAO;
    }

    public static void requireValidAmount(BigDecimal amount, String label)
            throws ValidationException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(
                    label + " must be greater than zero."
            );
        }

        if (amount.scale() > 2) {
            throw new ValidationException(
                    label + " can have at most 2 decimal places."
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException(label + " is too large.");
        }
    }

    public synchronized void addTransaction(Transaction transaction)
            throws Exception {

        if (transaction == null) {
            throw new ValidationException("Transaction is required.");
        }

        requireValidAmount(transaction.getAmount(), "Amount");

        if (transaction.getTransactionDate() == null) {
            throw new ValidationException("Date is required.");
        }

        if (transaction.getDescription() != null
                && transaction.getDescription().length() > 255) {

            throw new ValidationException(
                    "Description must be 255 characters or fewer."
            );
        }

        if (transaction.getCategoryId() <= 0) {
            throw new ValidationException("Select a category.");
        }

        Category category = categoryDAO
                .findById(transaction.getCategoryId())
                .orElseThrow(() -> new ValidationException(
                        "The selected category does not exist."
                ));

        if (category.getUserId() != transaction.getUserId()) {
            throw new ValidationException(
                    "That category belongs to another user."
            );
        }

        if (!category.getType().equals(transaction.getType())) {
            throw new ValidationException(
                    "The category type does not match the transaction."
            );
        }

        dao.save(transaction);
    }

    public synchronized void deleteTransaction(int transactionId,
                                               int userId)
            throws Exception {

        if (transactionId <= 0) {
            throw new ValidationException("Select a transaction first.");
        }

        dao.delete(transactionId, userId);
    }

    public List<Transaction> getTransactions(int userId)
            throws Exception {

        return dao.findByUser(userId);
    }

 
    public BigDecimal calculateBalance(List<Transaction> transactions) {

        BigDecimal balance = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            balance = balance.add(transaction.calculateImpact());
        }

        return balance;
    }

    public BigDecimal getIncome(int userId) throws Exception {
        return dao.getTotalByType(userId, "INCOME");
    }

    public BigDecimal getExpenses(int userId) throws Exception {
        return dao.getTotalByType(userId, "EXPENSE");
    }

    public BigDecimal getBalance(int userId) throws Exception {
        return getIncome(userId).subtract(getExpenses(userId));
    }


    public static <K extends Comparable<K>> Map<K, BigDecimal> sumBy(
            List<Transaction> transactions,
            Predicate<Transaction> filter,
            Function<Transaction, K> classifier) {

        Map<K, BigDecimal> totals = new TreeMap<>();

        for (Transaction transaction : transactions) {

            if (filter.test(transaction)) {
                totals.merge(
                        classifier.apply(transaction),
                        transaction.getAmount(),
                        BigDecimal::add
                );
            }
        }

        return totals;
    }

    public Map<String, BigDecimal> expenseTotalsByCategory(
            List<Transaction> transactions,
            Map<Integer, String> categoryNames) {

        Map<String, BigDecimal> totals = sumBy(
                transactions,
                t -> "EXPENSE".equals(t.getType()),
                t -> categoryNames.getOrDefault(
                        t.getCategoryId(),
                        "Category " + t.getCategoryId()
                )
        );

        Map<String, BigDecimal> sorted = new LinkedHashMap<>();

        totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue()
                        .reversed())
                .forEach(entry ->
                        sorted.put(entry.getKey(), entry.getValue()));

        return sorted;
    }
}
