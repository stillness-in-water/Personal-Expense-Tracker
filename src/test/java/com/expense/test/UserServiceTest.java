
package com.expense.service;

import com.expense.exception.ValidationException;
import com.expense.model.User;
import com.expense.testsupport.FakeUserDAO;
import com.expense.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static void assertRegistrationRejected(FakeUserDAO dao,
                                                   String name,
                                                   String email,
                                                   String password) {

        assertThrows(
                ValidationException.class,
                () -> new UserService(dao)
                        .register(name, email, password)
        );

        assertTrue(dao.registered.isEmpty());
    }

    @Test
    void blankNameShouldBeRejected() {
        assertRegistrationRejected(
                new FakeUserDAO(), "  ", "a@b.com", "password123");
    }

    @Test
    void invalidEmailShouldBeRejected() {
        assertRegistrationRejected(
                new FakeUserDAO(), "Test User", "not-an-email", "password123");
    }

    @Test
    void shortPasswordShouldBeRejected() {
        assertRegistrationRejected(
                new FakeUserDAO(), "Test User", "a@b.com", "123");
    }

    @Test
    void duplicateEmailShouldBeRejected() {

        FakeUserDAO dao = new FakeUserDAO();
        dao.byEmail.put("a@b.com",
                new User(1, "Existing", "a@b.com", "hash"));

        assertRegistrationRejected(
                dao, "Test User", "a@b.com", "password123");
    }

    @Test
    void validRegistrationShouldStoreSaltedHash() throws Exception {

        FakeUserDAO dao = new FakeUserDAO();

        new UserService(dao).register(
                "  Test User ",
                " a@b.com ",
                "password123"
        );

        assertEquals(1, dao.registered.size());

        User saved = dao.registered.get(0);

        assertEquals("Test User", saved.getName());
        assertEquals("a@b.com", saved.getEmail());

        assertNotEquals("password123", saved.getPasswordHash());
        assertTrue(saved.getPasswordHash().startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verifyPassword(
                "password123", saved.getPasswordHash()));
    }

    @Test
    void newAccountsReceiveStarterCategories() throws Exception {

        FakeUserDAO dao = new FakeUserDAO();

        new UserService(dao).register(
                "Test User", "a@b.com", "password123");

        assertFalse(dao.lastDefaults.isEmpty());

        assertTrue(dao.lastDefaults.stream()
                .anyMatch(c -> "INCOME".equals(c.getType())));

        assertTrue(dao.lastDefaults.stream()
                .anyMatch(c -> "EXPENSE".equals(c.getType())));
    }
}
