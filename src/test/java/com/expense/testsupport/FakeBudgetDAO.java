package com.expense.testsupport;

import com.expense.dao.BudgetDAO;
import com.expense.model.Budget;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FakeBudgetDAO extends BudgetDAO {

    public final List<Budget> rows = new CopyOnWriteArrayList<>();

    @Override
    public void save(Budget budget) {
        rows.add(budget);
    }

    @Override
    public List<Budget> findByUser(int userId) {

        return rows.stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }
}
