package com.expense.interfaces;

import java.util.List;

public interface UserScopedRepository<T, ID>
        extends Repository<T, ID> {

    List<T> findByUser(int userId) throws Exception;
}
