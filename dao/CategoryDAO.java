package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.database.JdbcHelper;
import com.expense.interfaces.RowMapper;
import com.expense.interfaces.UserScopedRepository;
import com.expense.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoryDAO
        implements UserScopedRepository<Category, Integer> {

    private static final RowMapper<Category> MAPPER = rs -> new Category(
            rs.getInt("category_id"),
            rs.getInt("user_id"),
            rs.getString("category_name"),
            rs.getString("type")
    );

    @Override
    public void save(Category category) throws Exception {

        String sql = """
                INSERT INTO categories(user_id, category_name, type)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, category.getUserId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getType());

            ps.executeUpdate();
        }
    }

    /**
     * Inserts many categories with one JDBC batch inside a single
     * database transaction (all rows are saved, or none are).
     */
    public void saveAll(List<Category> categories) throws Exception {

        if (categories.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO categories(user_id, category_name, type)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                for (Category category : categories) {

                    ps.setInt(1, category.getUserId());
                    ps.setString(2, category.getCategoryName());
                    ps.setString(3, category.getType());

                    ps.addBatch();
                }

                ps.executeBatch();
                con.commit();

            } catch (SQLException | RuntimeException e) {

                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }

    @Override
    public List<Category> findByUser(int userId) throws Exception {

        String sql = """
                SELECT * FROM categories
                WHERE user_id = ?
                ORDER BY category_name
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
    public Optional<Category> findById(Integer id) throws Exception {

        String sql = "SELECT * FROM categories WHERE category_id = ?";

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
    public List<Category> findAll() throws Exception {

        String sql = "SELECT * FROM categories ORDER BY category_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return JdbcHelper.toList(rs, MAPPER);
        }
    }

    @Override
    public void update(Category category) throws Exception {

        String sql = """
                UPDATE categories
                SET category_name = ?, type = ?
                WHERE category_id = ? AND user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryName());
            ps.setString(2, category.getType());
            ps.setInt(3, category.getCategoryId());
            ps.setInt(4, category.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id, int userId) throws Exception {

        String sql = """
                DELETE FROM categories
                WHERE category_id = ? AND user_id = ?
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

        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ps.executeUpdate();
        }
    }
}
