package com.expense.util;

import com.expense.interfaces.Exporter;
import com.expense.model.Transaction;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;


public class CSVExporter implements Exporter<Transaction> {

    private final Map<Integer, String> categoryNames;

    public CSVExporter() {
        this(Map.of());
    }

    public CSVExporter(Map<Integer, String> categoryNames) {
        this.categoryNames = Map.copyOf(categoryNames);
    }

    @Override
    public void export(List<Transaction> transactions, File file)
            throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file.toPath(), StandardCharsets.UTF_8)) {

            writer.write(
                    "Transaction ID,Type,Category,Amount,Description,Date"
            );
            writer.newLine();

            for (Transaction t : transactions) {

                String category = categoryNames.getOrDefault(
                        t.getCategoryId(),
                        "Category " + t.getCategoryId()
                );

                writer.write(String.join(",",
                        String.valueOf(t.getTransactionId()),
                        t.getType(),
                        escape(category),
                        t.getAmount().toPlainString(),
                        escape(t.getDescription()),
                        t.getTransactionDate().toString()
                ));

                writer.newLine();
            }
        }
    }

   
    static String escape(String value) {

        if (value == null) {
            return "";
        }

        String safe = value;

        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }

        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
