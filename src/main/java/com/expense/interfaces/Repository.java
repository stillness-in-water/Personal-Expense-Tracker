package com.expense.interfaces;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {

    void save(T entity) throws Exception;

    Optional<T> findById(ID id) throws Exception;

    List<T> findAll() throws Exception;

    void update(T entity) throws Exception;

    void delete(ID id) throws Exception;
}
