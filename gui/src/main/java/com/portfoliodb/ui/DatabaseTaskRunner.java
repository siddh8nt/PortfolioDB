package com.portfoliodb.ui;

import javax.swing.AbstractButton;
import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class DatabaseTaskRunner {

    @FunctionalInterface
    public interface DatabaseCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    public interface DatabaseRunnable {
        void run() throws Exception;
    }

    private DatabaseTaskRunner() {
    }

    public static <T> void runDatabaseTask(
            AbstractButton sourceButton,
            DatabaseCallable<T> databaseWork,
            Consumer<T> onSuccess,
            Consumer<Exception> onError
    ) {
        final String originalText = sourceButton == null ? null : sourceButton.getText();
        if (sourceButton != null) {
            sourceButton.setEnabled(false);
            sourceButton.setText("Loading...");
        }

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return databaseWork.call();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    if (onError != null) {
                        onError.accept(ex);
                    }
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    Exception error = cause instanceof Exception
                            ? (Exception) cause
                            : new RuntimeException(cause);
                    if (onError != null) {
                        onError.accept(error);
                    }
                } finally {
                    if (sourceButton != null) {
                        sourceButton.setEnabled(true);
                        sourceButton.setText(originalText);
                    }
                }
            }
        }.execute();
    }

    public static void runDatabaseTask(
            AbstractButton sourceButton,
            DatabaseRunnable databaseWork,
            Runnable onSuccess,
            Consumer<Exception> onError
    ) {
        runDatabaseTask(
                sourceButton,
                () -> {
                    databaseWork.run();
                    return null;
                },
                ignored -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                },
                onError
        );
    }
}