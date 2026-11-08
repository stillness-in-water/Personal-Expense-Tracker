package com.expense.util;

import com.expense.exception.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class InputParser {

    private InputParser() {
    }

    public static BigDecimal parseAmount(String text)
            throws ValidationException {

        if (text == null || text.isBlank()) {
            throw new ValidationException("Amount is required.");
        }

        try {
            return new BigDecimal(text.trim().replace(",", ""));

        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "Enter a valid amount, for example 1250.50."
            );
        }
    }

    public static LocalDate parseDate(String text)
            throws ValidationException {

        if (text == null || text.isBlank()) {
            throw new ValidationException("Date is required.");
        }

        try {
            return LocalDate.parse(text.trim());

        } catch (DateTimeParseException e) {
            throw new ValidationException(
                    "Enter the date as YYYY-MM-DD, for example 2026-11-08."
            );
        }
    }

    public static int parseInt(String text, String fieldName)
            throws ValidationException {

        if (text == null || text.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }

        try {
            return Integer.parseInt(text.trim());

        } catch (NumberFormatException e) {
            throw new ValidationException(
                    fieldName + " must be a whole number."
            );
        }
    }
}
