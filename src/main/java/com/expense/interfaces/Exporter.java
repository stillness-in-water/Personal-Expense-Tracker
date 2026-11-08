package com.expense.interfaces;

import java.io.File;
import java.util.List;

public interface Exporter<T> {

    void export(List<T> items, File file) throws Exception;
}
