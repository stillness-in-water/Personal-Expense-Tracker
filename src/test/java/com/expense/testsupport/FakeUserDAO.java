package com.expense.testsupport;

import com.expense.dao.UserDAO;
import com.expense.model.Category;
import com.expense.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeUserDAO extends UserDAO {

    public final Map<String, User> byEmail = new ConcurrentHashMap<>();

    public final List<User> registered = new ArrayList<>();

    public final List<User> updated = new ArrayList<>();

    public List<Category> lastDefaults;

    @Override
    public User findByEmail(String email) {
        return byEmail.get(email);
    }

    @Override
    public boolean emailExists(String email) {
        return byEmail.containsKey(email);
    }

    @Override
    public int saveWithDefaultCategories(User user,
                                         List<Category> defaults) {

        user.setUserId(registered.size() + 1);

        registered.add(user);
        byEmail.put(user.getEmail(), user);
        lastDefaults = defaults;

        return user.getUserId();
    }

    @Override
    public void update(User user) {
        updated.add(user);
        byEmail.put(user.getEmail(), user);
    }
}
