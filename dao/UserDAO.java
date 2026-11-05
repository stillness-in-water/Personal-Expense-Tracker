package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.interfaces.Repository;
import com.expense.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO implements Repository<User, Integer> {

    @Override
    public void save(User user) throws Exception {

        String sql = """
                INSERT INTO users(name, email, password_hash)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());

            ps.executeUpdate();
        }
    }

    public User login(String email, String passwordHash) throws Exception {

        String sql = """
                SELECT * FROM users
                WHERE email = ? AND password_hash = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, passwordHash);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
            }
        }

        return null;
    }

    public boolean emailExists(String email) throws Exception {

        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            return rs.next();
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws Exception {

        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return Optional.of(
                        new User(
                                rs.getInt("user_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("password_hash")
                        )
                );
            }
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() throws Exception {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {

                users.add(
                        new User(
                                rs.getInt("user_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("password_hash")
                        )
                );
            }
        }

        return users;
    }

    @Override
    public void update(User user) throws Exception {

        String sql = """
                UPDATE users
                SET name = ?, email = ?, password_hash = ?
                WHERE user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setInt(4, user.getUserId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Integer id) throws Exception {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
