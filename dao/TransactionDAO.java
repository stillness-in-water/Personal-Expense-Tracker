package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.interfaces.Repository;
import com.expense.model.Expense;
import com.expense.model.Income;
import com.expense.model.Transaction;
import com.expense.interfaces.UserScopedRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDAO
        implements Repository<Transaction, Integer> {

    @Override
    public void save(Transaction transaction) throws Exception {

        String sql = """
                INSERT INTO transactions
                (user_id, category_id, amount, type,
                 description, transaction_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, transaction.getUserId());
            ps.setInt(2, transaction.getCategoryId());
            ps.setBigDecimal(3, transaction.getAmount());
            ps.setString(4, transaction.getType());
            ps.setString(5, transaction.getDescription());
            ps.setDate(
                    6,
                    Date.valueOf(transaction.getTransactionDate())
            );

            ps.executeUpdate();
        }
    }

    public List<Transaction> findByUser(int userId) throws Exception {

        List<Transaction> list = new ArrayList<>();

        String sql = """
                SELECT * FROM transactions
                WHERE user_id = ?
                ORDER BY transaction_date DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String type = rs.getString("type");

                Transaction transaction;

                if ("INCOME".equals(type)) {

                    transaction = new Income(
                            userId,
                            rs.getInt("category_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("transaction_date")
                                    .toLocalDate()
                    );

                } else {

                    transaction = new Expense(
                            userId,
                            rs.getInt("category_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("transaction_date")
                                    .toLocalDate()
                    );
                }

                transaction.setTransactionId(
                        rs.getInt("transaction_id")
                );

                list.add(transaction);
            }
        }

        return list;
    }

    public BigDecimal getTotalByType(int userId,
                                     String type) throws Exception {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE user_id = ? AND type = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }

        return BigDecimal.ZERO;
    }

    @Override
    public Optional<Transaction> findById(Integer id) throws Exception {

        String sql = """
                SELECT * FROM transactions
                WHERE transaction_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String type = rs.getString("type");

                Transaction transaction;

                if ("INCOME".equals(type)) {

                    transaction = new Income(
                            rs.getInt("user_id"),
                            rs.getInt("category_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("transaction_date")
                                    .toLocalDate()
                    );

                } else {

                    transaction = new Expense(
                            rs.getInt("user_id"),
                            rs.getInt("category_id"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getDate("transaction_date")
                                    .toLocalDate()
                    );
                }

                transaction.setTransactionId(
                        rs.getInt("transaction_id")
                );

                return Optional.of(transaction);
            }
        }

        return Optional.empty();
    }

    @Override
public List<Transaction> findAll() throws Exception {

    List<Transaction> list = new ArrayList<>();

    String sql = """
            SELECT *
            FROM transactions
            ORDER BY transaction_date DESC
            """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            String type = rs.getString("type");

            Transaction transaction;

            if ("INCOME".equals(type)) {

                transaction = new Income(
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("description"),
                        rs.getDate("transaction_date")
                                .toLocalDate()
                );

            } else {

                transaction = new Expense(
                        rs.getInt("user_id"),
                        rs.getInt("category_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("description"),
                        rs.getDate("transaction_date")
                                .toLocalDate()
                );
            }

            transaction.setTransactionId(
                    rs.getInt("transaction_id")
            );

            list.add(transaction);
        }
    }

    return list;
}

    @Override
    public void update(Transaction transaction) throws Exception {

        String sql = """
                UPDATE transactions
                SET category_id = ?,
                    amount = ?,
                    type = ?,
                    description = ?,
                    transaction_date = ?
                WHERE transaction_id = ?
                AND user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, transaction.getCategoryId());
            ps.setBigDecimal(2, transaction.getAmount());
            ps.setString(3, transaction.getType());
            ps.setString(4, transaction.getDescription());
            ps.setDate(
                    5,
                    Date.valueOf(transaction.getTransactionDate())
            );
            ps.setInt(6, transaction.getTransactionId());
            ps.setInt(7, transaction.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id, int userId)
        throws Exception {

    String sql = """
            DELETE FROM transactions
            WHERE transaction_id = ?
            AND user_id = ?
            """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        ps.setInt(2, userId);

        ps.executeUpdate();
    }
}

public BigDecimal calculateBalance(
        List<Transaction> transactions) {

    BigDecimal balance = BigDecimal.ZERO;

    for (Transaction transaction : transactions) {
        balance = balance.add(
                transaction.calculateImpact()
        );
    }

    return balance;
}
                
}
