package com.expense.model;

public class Category {

    private int categoryId;
    private int userId;
    private String categoryName;
    private String type;

    public Category() {
    }

    public Category(int userId, String categoryName, String type) {
        this.userId = userId;
        this.categoryName = categoryName;
        this.type = type;
    }

    public Category(int categoryId, int userId,
                    String categoryName, String type) {

        this.categoryId = categoryId;
        this.userId = userId;
        this.categoryName = categoryName;
        this.type = type;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}
