package com.expense.service;

import com.expense.dao.CategoryDAO;
import com.expense.exception.ValidationException;
import com.expense.model.Category;

import java.util.List;

public class CategoryService {

    private final CategoryDAO dao =
            new CategoryDAO();

    public List<Category> getCategories(
            int userId,
            String type)
            throws Exception {

        List<Category> categories =
                dao.findByUser(userId);

        if (type == null || type.isBlank()) {
            return categories;
        }

        return categories.stream()
                .filter(category ->
                        type.equals(
                                category.getType()
                        ))
                .toList();
    }

    public void addCategory(
            Category category)
            throws Exception {

        if (category.getCategoryName() == null ||
                category.getCategoryName()
                        .isBlank()) {

            throw new ValidationException(
                    "Category name is required."
            );
        }

        if (!"INCOME".equals(category.getType()) &&
                !"EXPENSE".equals(category.getType())) {

            throw new ValidationException(
                    "Invalid category type."
            );
        }

        dao.save(category);
    }
}
