package com.expense.util;

import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class DatabaseWorker<T>
        extends SwingWorker<T, Void> {

    private final DatabaseTask<T> task;
    private final Consumer<T> onSuccess;
    private final Consumer<Exception> onError;
    private final Runnable onFinish;

    public DatabaseWorker(
            DatabaseTask<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError,
            Runnable onFinish) {

        this.task = task;
        this.onSuccess = onSuccess;
        this.onError = onError;
        this.onFinish = onFinish;
    }

    @Override
    protected T doInBackground()
            throws Exception {

        return task.execute();
    }

    @Override
    protected void done() {

        try {

            T result = get();

            if (onSuccess != null) {
                onSuccess.accept(result);
            }

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            if (onError != null) {
                onError.accept(e);
            }

        } catch (ExecutionException e) {

            Throwable cause = e.getCause();

            if (onError != null) {

                if (cause instanceof Exception) {
                    onError.accept(
                            (Exception) cause
                    );
                } else {
                    onError.accept(
                            new Exception(cause)
                    );
                }
            }

        } finally {

            if (onFinish != null) {
                onFinish.run();
            }
        }
    }

    @FunctionalInterface
    public interface DatabaseTask<T> {

        T execute() throws Exception;
    }
}
