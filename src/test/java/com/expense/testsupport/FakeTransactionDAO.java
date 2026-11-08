package com.expense.testsupport;

import com.expense.dao.TransactionDAO;
import com.expense.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FakeTransactionDAO extends TransactionDAO {

    public final List<Transaction> saved = new CopyOnWriteArrayList<>();

    public final List<int[]> deleted = new CopyOnWriteArrayList<>();

    public final AtomicInteger maxConcurrentSaves = new AtomicInteger();

    private final AtomicInteger savesInProgress = new AtomicInteger();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public void save(Transaction transaction) throws Exception {

        int running = savesInProgress.incrementAndGet();
        maxConcurrentSaves.accumulateAndGet(running, Math::max);

        try {
            Thread.sleep(2);

            transaction.setTransactionId(nextId.getAndIncrement());
            saved.add(transaction);

        } finally {
            savesInProgress.decrementAndGet();
        }
    }

    @Override
    public List<Transaction> findByUser(int userId) {

        return saved.stream()
                .filter(t -> t.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Integer id, int userId) {
        deleted.add(new int[]{id, userId});
    }

    @Override
    public BigDecimal getTotalByType(int userId, String type) {

        return saved.stream()
                .filter(t -> t.getUserId() == userId
                        && type.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getExpenseTotal(int userId,
                                      Integer categoryId,
                                      int month,
                                      int year) {

        return saved.stream()
                .filter(t -> t.getUserId() == userId
                        && "EXPENSE".equals(t.getType()))
                .filter(t -> categoryId == null
                        || t.getCategoryId() == categoryId)
                .filter(t -> t.getTransactionDate().getMonthValue() == month
                        && t.getTransactionDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
