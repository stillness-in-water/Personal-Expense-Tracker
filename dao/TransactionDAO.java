package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.database.JdbcHelper;
import com.expense.interfaces.RowMapper;
import com.expense.interfaces.UserScopedRepository;
import com.expense.model.Expense;
import com.expense.model.Income;
import com.expense.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TransactionDAO
        implements UserScopedRepository<Transaction, Integer> {

    /** Builds an Income or an Expense depending on the stored type. */
    private static final RowMapper<Transaction> MAPPER = rs -> {

        int userId = rs.getInt("user_id");
        int categoryId = rs.getInt("category_id");
        BigDecimal amount = rs.getBigDecimal("amount");
        String description = rs.getString("description");
        LocalDate date = rs.getDate("transaction_date").toLocalDate();

        Transaction transaction =
                "INCOME".equals(rs.getString("type"))
                        ? new Income(userId, categoryId, amount,
                                description, date)
                        : new Expense(userId, categoryId, amount,
                                description, date);

        transaction.setTransactionId(rs.getInt("transaction_id"));

        return transaction;
    };

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
            ps.setDate(6, Date.valueOf(transaction.getTransactionDate()));

            ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findByUser(int userId) throws Exception {

        String sql = """
                SELECT * FROM transactions
                WHERE user_id = ?
                ORDER BY transaction_date DESC, transaction_id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return JdbcHelper.toList(rs, MAPPER);
            }
        }
    }

    public BigDecimal getTotalByType(int userId, String type)
            throws Exception {

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE user_id = ? AND type = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Total expenses in one calendar month, optionally limited to one
     * category. Used to compare spending against a budget.
     *
     * @param categoryId null means all expense categories
     */
    public BigDecimal getExpenseTotal(int userId,
                                      Integer categoryId,
                                      int month,
                                      int year) throws Exception {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM transactions
                WHERE user_id = ?
                AND type = 'EXPENSE'
                AND transaction_date >= ?
                AND transaction_date < ?
                """
                + (categoryId == null ? "" : "AND category_id = ?");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            if (categoryId != null) {
                ps.setInt(4, categoryId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
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

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(MAPPER.map(rs))
                        : Optional.empty();
            }
        }
    }

    @Override
    public List<Transaction> findAll() throws Exception {

        String sql = """
                SELECT * FROM transactions
                ORDER BY transaction_date DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return JdbcHelper.toList(rs, MAPPER);
        }
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
            ps.setDate(5, Date.valueOf(transaction.getTransactionDate()));
            ps.setInt(6, transaction.getTransactionId());
            ps.setInt(7, transaction.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id, int userId) throws Exception {

        String sql = """
                DELETE FROM transactions
                WHERE transaction_id = ? AND user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {

        String sql = "DELETE FROM transactions WHERE transaction_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }
}
