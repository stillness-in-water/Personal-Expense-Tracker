package com.expense.service;

import com.expense.dao.UserDAO;
import com.expense.exception.ValidationException;
import com.expense.model.User;
import com.expense.util.PasswordUtil;

public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public void register(String name,
                         String email,
                         String password) throws Exception {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is required.");
        }

        if (email == null || !email.contains("@")) {
            throw new ValidationException("Enter a valid email.");
        }

        if (password == null || password.length() < 6) {
            throw new ValidationException(
                    "Password must contain at least 6 characters."
            );
        }

        if (userDAO.emailExists(email)) {
            throw new ValidationException(
                    "Email is already registered."
            );
        }

        String hash = PasswordUtil.hashPassword(password);

        userDAO.save(
                new User(name, email, hash)
        );
    }

    public User login(String email,
                      String password) throws Exception {

        String hash = PasswordUtil.hashPassword(password);

        return userDAO.login(email, hash);
    }
}
