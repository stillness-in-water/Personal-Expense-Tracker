package com.expense.service;

import com.expense.dao.UserDAO;
import com.expense.model.User;
import com.expense.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationTest {

    @Test
    void validCredentialsShouldAuthenticate() throws Exception {

        String email = "test@example.com";
        String password = "password123";

        User expectedUser = new User(
                1,
                "Test User",
                email,
                PasswordUtil.hashPassword(password)
        );

        UserDAO fakeDAO = new UserDAO() {
            @Override
            public User login(String requestedEmail,
                              String passwordHash) {
                if (email.equals(requestedEmail)
                        && expectedUser.getPasswordHash().equals(passwordHash)) {
                    return expectedUser;
                }

                return null;
            }
        };

        UserService service = new UserService(fakeDAO);

        User result = service.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Test User", result.getName());
    }

    @Test
    void invalidPasswordShouldFailAuthentication() throws Exception {

        String email = "test@example.com";
        String correctPassword = "password123";

        User expectedUser = new User(
                1,
                "Test User",
                email,
                PasswordUtil.hashPassword(correctPassword)
        );

        UserDAO fakeDAO = new UserDAO() {
            @Override
            public User login(String requestedEmail,
                              String passwordHash) {
                if (email.equals(requestedEmail)
                        && expectedUser.getPasswordHash().equals(passwordHash)) {
                    return expectedUser;
                }

                return null;
            }
        };

        UserService service = new UserService(fakeDAO);

        User result = service.login(
                email,
                "wrongpassword"
        );

        assertNull(result);
    }

    @Test
    void invalidEmailShouldFailRegistrationValidation()
            throws Exception {

        UserDAO fakeDAO = new UserDAO() {
            @Override
            public boolean emailExists(String email) {
                return false;
            }
        };

        UserService service = new UserService(fakeDAO);

        assertThrows(
                com.expense.exception.ValidationException.class,
                () -> service.register(
                        "Test User",
                        "invalid-email",
                        "password123"
                )
        );
    }
}
