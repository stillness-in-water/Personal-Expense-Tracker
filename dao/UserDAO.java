package com.expense.dao;

import com.expense.database.DBConnection;
import com.expense.database.JdbcHelper;
import com.expense.interfaces.Repository;
import com.expense.interfaces.RowMapper;
import com.expense.model.Category;
import com.expense.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class UserDAO implements Repository<User, Integer> {

    private static final RowMapper<User> MAPPER = rs -> new User(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password_hash")
    );

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

    /**
     * Creates the user and their starter categories in ONE database
     * transaction: either everything is saved, or nothing is.
     *
     * @param defaults category templates; their user id is ignored
     * @return the generated user id
     */
    public int saveWithDefaultCategories(User user,
                                         List<Category> defaults)
            throws Exception {

        String userSql = """
                INSERT INTO users(name, email, password_hash)
                VALUES (?, ?, ?)
                """;

        String categorySql = """
                INSERT INTO categories(user_id, category_name, type)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int userId;

                try (PreparedStatement ps = con.prepareStatement(
                        userSql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getPasswordHash());

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "The new user id was not returned."
                            );
                        }

                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps =
                             con.prepareStatement(categorySql)) {

                    for (Category category : defaults) {

                        ps.setInt(1, userId);
                        ps.setString(2, category.getCategoryName());
                        ps.setString(3, category.getType());

                        ps.addBatch();
                    }

                    ps.executeBatch();
                }

                con.commit();

                return userId;

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

    /** @return the matching user, or null when the email is unknown */
    public User findByEmail(String email) throws Exception {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? MAPPER.map(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws Exception {

        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public Optional<User> findById(Integer id) throws Exception {

        String sql = "SELECT * FROM users WHERE user_id = ?";

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
    public List<User> findAll() throws Exception {

        String sql = "SELECT * FROM users ORDER BY user_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return JdbcHelper.toList(rs, MAPPER);
        }
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
