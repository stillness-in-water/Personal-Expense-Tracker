package com.expense.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Transaction {

  private int transactionId;
  private int userId;
  private int categoryId;
  private BigDecimal amount;
  private String description;
  private LocalDate transactionDate;

    public Transaction() {
    }

    public Transaction(int userId,
                       int categoryId,
                       BigDecimal amount,
                       String description,
                       LocalDate transactionDate) {

        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public abstract BigDecimal calculateImpact();

    public abstract String getType();

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}
