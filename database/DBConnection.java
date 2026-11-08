package com.expense.database;

import com.expense.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL =
            System.getenv().getOrDefault(
                    "DB_URL",
                    "jdbc:mysql://localhost:3306/expense_tracker"
            );

    private static final String USER =
            System.getenv().getOrDefault(
                    "DB_USER",
                    "root"
            );

    private static final String PASSWORD =
            System.getenv().getOrDefault(
                    "DB_PASSWORD",
                    ""
            );

    private DBConnection() {
    }

    public static Connection getConnection() throws DatabaseException {

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (SQLException e) {
            throw new DatabaseException(
                    "Could not connect to the database. Make sure MySQL "
                            + "is running and that DB_URL, DB_USER and "
                            + "DB_PASSWORD are set correctly.",
                    e
            );
        }
    }

    public static void testConnection() throws DatabaseException {

        try (Connection ignored = getConnection()) {

        } catch (SQLException e) {
            throw new DatabaseException(
                    "The database connection could not be closed cleanly.",
                    e
            );
        }
    }
}
