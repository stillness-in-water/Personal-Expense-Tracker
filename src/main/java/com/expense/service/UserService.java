package com.expense.service;

import com.expense.dao.UserDAO;
import com.expense.exception.ValidationException;
import com.expense.model.User;
import com.expense.util.PasswordUtil;

import java.util.regex.Pattern;

public class UserService {

    private static final Pattern EMAIL =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserDAO userDAO;

    public UserService() {
        this(new UserDAO());
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void register(String name,
                         String email,
                         String password) throws Exception {

        if (name == null || name.isBlank()) {
            throw new ValidationException("Name is required.");
        }

        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException("Enter a valid email.");
        }

        if (password == null || password.length() < 6) {
            throw new ValidationException(
                    "Password must contain at least 6 characters."
            );
        }

        String cleanEmail = email.trim();

        if (userDAO.emailExists(cleanEmail)) {
            throw new ValidationException(
                    "Email is already registered."
            );
        }

        User user = new User(
                name.trim(),
                cleanEmail,
                PasswordUtil.hashPassword(password)
        );

        userDAO.saveWithDefaultCategories(
                user,
                CategoryService.defaultCategories()
        );
    }


    public User login(String email, String password) throws Exception {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {

            throw new ValidationException(
                    "Email and password are required."
            );
        }

        User user = userDAO.findByEmail(email.trim());

        if (user == null
                || !PasswordUtil.verifyPassword(
                        password, user.getPasswordHash())) {

            return null;
        }

        if (PasswordUtil.needsUpgrade(user.getPasswordHash())) {

            user.setPasswordHash(PasswordUtil.hashPassword(password));
            userDAO.update(user);
        }

        return user;
    }
}
