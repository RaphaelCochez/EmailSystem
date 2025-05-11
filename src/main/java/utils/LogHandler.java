package utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

/**
 * LogHandler is a hybrid logging utility that bridges SLF4J logging
 * and manual file-based persistence for audit traceability.
 *
 * Used primarily on the server side for:
 * - Debugging during development
 * - Auditable logging during assessment
 * - Runtime crash diagnostics
 */
@Slf4j
public class LogHandler {

    private static final String LOG_FILE = ServerConstants.LOG_FILE_PATH;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    static {
        init(); // Setup log file at server startup
    }

    /** Initializes the log directory and file if they do not exist */
    private static void init() {
        try {
            Files.createDirectories(Paths.get("logs"));
            Files.createFile(Paths.get(LOG_FILE));
        } catch (FileAlreadyExistsException ignored) {
            // No action needed
        } catch (IOException e) {
            System.err.println("[LOG_INIT_ERROR] Failed to initialize logger: " + e.getMessage());
        }
    }

    /** Returns timestamp in format [yyyy-MM-dd HH:mm:ss] */
    private static String timestamp() {
        return "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "]";
    }

    /**
     * Unified logging method for compatibility with existing calls like log("...").
     * Delegates to SLF4J info and persists to file.
     * 
     * @param message the message to log
     */
    public static void log(String message) {
        info(message); // Default to info-level
    }

    /** Logs an info-level message */
    public static void info(String message) {
        log.info(message);
        logAsync("INFO", message);
    }

    /** Logs a warning-level message */
    public static void warn(String message) {
        log.warn(message);
        logAsync("WARN", message);
    }

    /** Logs an error-level message */
    public static void error(String message) {
        log.error(message);
        logAsync("ERROR", message);
    }

    /**
     * Asynchronously writes log entries to file to avoid blocking main threads.
     * 
     * @param level   log severity (INFO, WARN, ERROR)
     * @param message content to log
     */
    private static void logAsync(String level, String message) {
        if (executor.isShutdown()) {
            System.err.println("[WARN] Logger is shut down. Skipping: " + message);
            return;
        }

        String origin = Thread.currentThread().getStackTrace()[3].getClassName();
        String entry = String.format("%s [%s] (%s): %s", timestamp(), level, origin, message);

        executor.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(entry);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("[LOG_WRITE_ERROR] Failed to write to log file: " + e.getMessage());
            }
        });
    }

    /**
     * Call this during shutdown to flush and terminate the logging executor
     * cleanly.
     */
    public static void shutdown() {
        info("Shutting down LogHandler...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("[LOG_HANDLER] Forced shutdown failed.");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /** Resets executor after shutdown for use in test environments */
    public static void resetExecutorForTests() {
        if (executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }
}
