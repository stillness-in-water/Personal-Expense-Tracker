package com.expense.util;

import com.expense.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class CSVExporter {

    private CSVExporter() {
    }

    public static void exportTransactions(
            List<Transaction> transactions,
            File file) throws Exception {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println(
                    "Transaction ID,Type,Amount,Description,Date"
            );

            for (Transaction t : transactions) {

                writer.printf(
                        "%d,%s,%s,%s,%s%n",
                        t.getTransactionId(),
                        t.getType(),
                        t.getAmount(),
                        escape(t.getDescription()),
                        t.getTransactionDate()
                );
            }
        }
    }

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }
}
