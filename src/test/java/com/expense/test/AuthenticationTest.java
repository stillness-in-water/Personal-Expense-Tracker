package com.expense.service;

import com.expense.exception.ValidationException;
import com.expense.model.User;
import com.expense.testsupport.FakeUserDAO;
import com.expense.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

    private static FakeUserDAO daoWithUser(String storedHash) {

        FakeUserDAO dao = new FakeUserDAO();

        dao.byEmail.put(
                EMAIL,
                new User(1, "Test User", EMAIL, storedHash)
        );

        return dao;
    }

    @Test
    void validCredentialsShouldAuthenticate() throws Exception {

        UserService service = new UserService(
                daoWithUser(PasswordUtil.hashPassword(PASSWORD)));

        User result = service.login(EMAIL, PASSWORD);

        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
        assertEquals("Test User", result.getName());
    }

    @Test
    void invalidPasswordShouldFailAuthentication() throws Exception {

        UserService service = new UserService(
                daoWithUser(PasswordUtil.hashPassword(PASSWORD)));

        assertNull(service.login(EMAIL, "wrongpassword"));
    }

    @Test
    void unknownEmailShouldFailAuthentication() throws Exception {

        UserService service = new UserService(
                daoWithUser(PasswordUtil.hashPassword(PASSWORD)));

        assertNull(service.login("nobody@example.com", PASSWORD));
    }

    @Test
    void emailIsTrimmedBeforeTheLookup() throws Exception {

        UserService service = new UserService(
                daoWithUser(PasswordUtil.hashPassword(PASSWORD)));

        assertNotNull(service.login("  " + EMAIL + "  ", PASSWORD));
    }

    @Test
    void blankCredentialsShouldBeRejected() {

        UserService service = new UserService(
                daoWithUser(PasswordUtil.hashPassword(PASSWORD)));

        assertThrows(ValidationException.class,
                () -> service.login("", PASSWORD));

        assertThrows(ValidationException.class,
                () -> service.login(EMAIL, ""));

        assertThrows(ValidationException.class,
                () -> service.login(null, null));
    }

    @Test
    void oldSha256AccountCanLogInAndIsUpgraded() throws Exception {

        FakeUserDAO dao = daoWithUser(sha256Hex(PASSWORD));

        User result = new UserService(dao).login(EMAIL, PASSWORD);

        assertNotNull(result);

        assertEquals(1, dao.updated.size());

        String upgraded = dao.updated.get(0).getPasswordHash();

        assertTrue(upgraded.startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verifyPassword(PASSWORD, upgraded));
    }

    @Test
    void currentHashesAreNotRewrittenOnLogin() throws Exception {

        FakeUserDAO dao =
                daoWithUser(PasswordUtil.hashPassword(PASSWORD));

        new UserService(dao).login(EMAIL, PASSWORD);

        assertTrue(dao.updated.isEmpty());
    }

    @Test
    void invalidEmailShouldFailRegistrationValidation() {

        UserService service = new UserService(new FakeUserDAO());

        assertThrows(
                ValidationException.class,
                () -> service.register(
                        "Test User",
                        "invalid-email",
                        PASSWORD
                )
        );
    }

    private static String sha256Hex(String text) throws Exception {

        byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
