package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.interfaces.Repository;
import com.expense.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAO implements Repository<Category, Integer> {

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

    public List<Category> findByUser(int userId) throws Exception {

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT * FROM categories
                WHERE user_id = ?
                ORDER BY category_name
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                categories.add(
                        new Category(
                                rs.getInt("category_id"),
                                rs.getInt("user_id"),
                                rs.getString("category_name"),
                                rs.getString("type")
                        )
                );
            }
        }

        return categories;
    }

    @Override
    public Optional<Category> findById(Integer id) throws Exception {

        String sql =
                "SELECT * FROM categories WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return Optional.of(
                        new Category(
                                rs.getInt("category_id"),
                                rs.getInt("user_id"),
                                rs.getString("category_name"),
                                rs.getString("type")
                        )
                );
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Category> findAll() throws Exception {

        return findByUser(1);
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
    public void delete(Integer id) throws Exception {

        String sql =
                "DELETE FROM categories WHERE category_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
