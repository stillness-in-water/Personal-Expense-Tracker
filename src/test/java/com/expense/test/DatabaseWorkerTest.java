package com.expense.util;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseWorkerTest {

    @Test
    void taskRunsOffTheEventThreadAndCallbacksRunOnIt() throws Exception {

        AtomicBoolean taskOnEventThread = new AtomicBoolean(true);
        AtomicBoolean callbackOnEventThread = new AtomicBoolean(false);
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        new DatabaseWorker<String>(
                () -> {
                    taskOnEventThread.set(
                            SwingUtilities.isEventDispatchThread());
                    return "done";
                },
                value -> {
                    callbackOnEventThread.set(
                            SwingUtilities.isEventDispatchThread());
                    result.set(value);
                },
                error -> result.set("error: " + error.getMessage()),
                finished::countDown
        ).execute();

        assertTrue(finished.await(10, TimeUnit.SECONDS));

        assertFalse(taskOnEventThread.get());
        assertTrue(callbackOnEventThread.get());
        assertEquals("done", result.get());
    }

    @Test
    void failuresAreHandedToTheErrorCallback() throws Exception {

        AtomicReference<String> result = new AtomicReference<>();
        AtomicBoolean successCalled = new AtomicBoolean(false);
        CountDownLatch finished = new CountDownLatch(1);

        new DatabaseWorker<String>(
                () -> {
                    throw new IllegalStateException("boom");
                },
                value -> successCalled.set(true),
                error -> result.set(error.getMessage()),
                finished::countDown
        ).execute();

        assertTrue(finished.await(10, TimeUnit.SECONDS));

        assertFalse(successCalled.get());
        assertEquals("boom", result.get());
    }
}
