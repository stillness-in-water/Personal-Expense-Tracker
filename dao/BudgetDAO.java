package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.database.JdbcHelper;
import com.expense.interfaces.RowMapper;
import com.expense.interfaces.UserScopedRepository;
import com.expense.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

public class BudgetDAO implements UserScopedRepository<Budget, Integer> {

    private static final RowMapper<Budget> MAPPER = rs -> {

        Budget budget = new Budget(
                rs.getInt("user_id"),
                rs.getObject("category_id", Integer.class),
                rs.getBigDecimal("amount_limit"),
                rs.getInt("month"),
                rs.getInt("year")
        );

        budget.setBudgetId(rs.getInt("budget_id"));

        return budget;
    };

    @Override
    public void save(Budget budget) throws Exception {

        String sql = """
                INSERT INTO budgets
                (user_id, category_id, amount_limit, month, year)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, budget.getUserId());
            JdbcHelper.setNullableInt(ps, 2, budget.getCategoryId());
            ps.setBigDecimal(3, budget.getAmountLimit());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());

            ps.executeUpdate();
        }
    }

    @Override
    public List<Budget> findByUser(int userId) throws Exception {

        String sql = """
                SELECT * FROM budgets
                WHERE user_id = ?
                ORDER BY year DESC, month DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return JdbcHelper.toList(rs, MAPPER);
            }
        }
    }

    @Override
    public Optional<Budget> findById(Integer id) throws Exception {

        String sql = "SELECT * FROM budgets WHERE budget_id = ?";

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
    public List<Budget> findAll() throws Exception {

        String sql = """
                SELECT * FROM budgets
                ORDER BY year DESC, month DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return JdbcHelper.toList(rs, MAPPER);
        }
    }

    @Override
    public void update(Budget budget) throws Exception {

        String sql = """
                UPDATE budgets
                SET category_id = ?,
                    amount_limit = ?,
                    month = ?,
                    year = ?
                WHERE budget_id = ?
                AND user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            JdbcHelper.setNullableInt(ps, 1, budget.getCategoryId());
            ps.setBigDecimal(2, budget.getAmountLimit());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.setInt(5, budget.getBudgetId());
            ps.setInt(6, budget.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id, int userId) throws Exception {

        String sql = """
                DELETE FROM budgets
                WHERE budget_id = ? AND user_id = ?
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

        String sql = "DELETE FROM budgets WHERE budget_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }
}
