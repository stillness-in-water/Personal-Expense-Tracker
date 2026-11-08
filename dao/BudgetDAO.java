package com.expense.dao;

import com.expense.interfaces.UserScopedRepository;
import com.expense.database.DBConnection;
import com.expense.interfaces.Repository;
import com.expense.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetDAO implements UserScopedRepository<Budget, Integer> {

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

            if (budget.getCategoryId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, budget.getCategoryId());
            }

            ps.setBigDecimal(3, budget.getAmountLimit());
            ps.setInt(4, budget.getMonth());
            ps.setInt(5, budget.getYear());

            ps.executeUpdate();
        }
    }

    public List<Budget> findByUser(int userId) throws Exception {

        List<Budget> list = new ArrayList<>();

        String sql = """
                SELECT * FROM budgets
                WHERE user_id = ?
                ORDER BY year DESC, month DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Integer categoryId =
                        rs.getObject("category_id", Integer.class);

                Budget budget = new Budget(
                        userId,
                        categoryId,
                        rs.getBigDecimal("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year")
                );

                budget.setBudgetId(
                        rs.getInt("budget_id")
                );

                list.add(budget);
            }
        }

        return list;
    }

    @Override
    public Optional<Budget> findById(Integer id) throws Exception {

        String sql =
                "SELECT * FROM budgets WHERE budget_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Integer categoryId =
                        rs.getObject("category_id", Integer.class);

                Budget budget = new Budget(
                        rs.getInt("user_id"),
                        categoryId,
                        rs.getBigDecimal("amount_limit"),
                        rs.getInt("month"),
                        rs.getInt("year")
                );

                budget.setBudgetId(
                        rs.getInt("budget_id")
                );

                return Optional.of(budget);
            }
        }

        return Optional.empty();
    }

    @Override
    public void delete(Integer id, int userId)
        throws Exception {

    String sql = """
            DELETE FROM budgets
            WHERE budget_id = ?
            AND user_id = ?
            """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        ps.setInt(2, userId);

        ps.executeUpdate();
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

            if (budget.getCategoryId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, budget.getCategoryId());
            }

            ps.setBigDecimal(2, budget.getAmountLimit());
            ps.setInt(3, budget.getMonth());
            ps.setInt(4, budget.getYear());
            ps.setInt(5, budget.getBudgetId());
            ps.setInt(6, budget.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {

        String sql =
                "DELETE FROM budgets WHERE budget_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }
}
