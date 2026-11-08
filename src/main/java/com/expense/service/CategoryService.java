package com.expense.service;

import com.expense.dao.CategoryDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CategoryService {

    private static final Map<String, List<String>> DEFAULTS =
            new LinkedHashMap<>();

    static {
        DEFAULTS.put("INCOME",
                List.of("Salary", "Freelance", "Other Income"));

        DEFAULTS.put("EXPENSE",
                List.of("Food", "Transport", "Rent", "Bills",
                        "Shopping", "Health", "Entertainment", "Other"));
    }

    private final CategoryDAO dao;

    private final Map<Integer, List<Category>> cache =
            new ConcurrentHashMap<>();

    public CategoryService() {
        this(new CategoryDAO());
    }

    public CategoryService(CategoryDAO dao) {
        this.dao = dao;
    }

    public static List<Category> defaultCategories() {

        List<Category> categories = new ArrayList<>();

        DEFAULTS.forEach((type, names) -> {
            for (String name : names) {
                categories.add(new Category(0, name, type));
            }
        });

        return categories;
    }

    public List<Category> getCategories(int userId, String type)
            throws Exception {

        List<Category> all = loadAll(userId);

        if (type == null || type.isBlank()) {
            return all;
        }

        return all.stream()
                .filter(category -> type.equals(category.getType()))
                .toList();
    }

    public Map<Integer, String> getCategoryNames(int userId)
            throws Exception {

        Map<Integer, String> names = new HashMap<>();

        for (Category category : loadAll(userId)) {
            names.put(category.getCategoryId(), category.getCategoryName());
        }

        return names;
    }

    public synchronized void ensureDefaults(int userId) throws Exception {

        if (!loadAll(userId).isEmpty()) {
            return;
        }

        List<Category> starter = new ArrayList<>();

        for (Category template : defaultCategories()) {
            starter.add(new Category(
                    userId,
                    template.getCategoryName(),
                    template.getType()
            ));
        }

        dao.saveAll(starter);
        cache.remove(userId);
    }

    public synchronized void addCategory(Category category)
            throws Exception {

        if (category == null
                || category.getCategoryName() == null
                || category.getCategoryName().isBlank()) {

            throw new ValidationException("Category name is required.");
        }

        String name = category.getCategoryName().trim();

        if (name.length() > 100) {
            throw new ValidationException(
                    "Category name must be 100 characters or fewer."
            );
        }

        if (!"INCOME".equals(category.getType())
                && !"EXPENSE".equals(category.getType())) {

            throw new ValidationException("Invalid category type.");
        }

        Set<String> existing = dao.findByUser(category.getUserId())
                .stream()
                .filter(c -> category.getType().equals(c.getType()))
                .map(c -> c.getCategoryName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (existing.contains(name.toLowerCase(Locale.ROOT))) {
            throw new ValidationException("That category already exists.");
        }

        category.setCategoryName(name);
        dao.save(category);

        cache.remove(category.getUserId());
    }

    private List<Category> loadAll(int userId) throws Exception {

        try {
       
            return cache.computeIfAbsent(userId, id -> {
                try {
                    return List.copyOf(dao.findByUser(id));
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            });

        } catch (CompletionException e) {

            if (e.getCause() instanceof Exception cause) {
                throw cause;
            }

            throw e;
        }
    }
}
