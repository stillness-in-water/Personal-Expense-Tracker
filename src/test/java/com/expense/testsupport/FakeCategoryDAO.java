package com.expense.testsupport;

import com.expense.dao.CategoryDAO;
import com.expense.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FakeCategoryDAO extends CategoryDAO {

    public final List<Category> rows = new CopyOnWriteArrayList<>();

    public final AtomicInteger findByUserCalls = new AtomicInteger();
    public final AtomicInteger saveAllCalls = new AtomicInteger();

    public volatile long readDelayMillis = 0;

    private final AtomicInteger nextId = new AtomicInteger(1);

    public Category add(int userId, String name, String type) {

        Category category =
                new Category(nextId.getAndIncrement(), userId, name, type);

        rows.add(category);

        return category;
    }

    @Override
    public void save(Category category) {
        category.setCategoryId(nextId.getAndIncrement());
        rows.add(category);
    }

    @Override
    public void saveAll(List<Category> categories) {

        saveAllCalls.incrementAndGet();

        for (Category category : categories) {
            save(category);
        }
    }

    @Override
    public List<Category> findByUser(int userId) throws Exception {

        findByUserCalls.incrementAndGet();

        if (readDelayMillis > 0) {
            Thread.sleep(readDelayMillis);
        }

        return rows.stream()
                .filter(c -> c.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findById(Integer id) {

        return rows.stream()
                .filter(c -> c.getCategoryId() == id)
                .findFirst();
    }
}
