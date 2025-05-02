package utils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class LogHandler {

    private static final String LOG_FILE = Constants.LOG_FILE_PATH;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    static {
        init();
    }

    private static void init() {
        try {
            Files.createDirectories(Paths.get("logs"));
            new File(LOG_FILE).createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to initialize log directory or file: " + e.getMessage());
        }
    }

    private static String timestamp() {
        return "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "]";
    }

    public static void log(String message) {
        if (executor.isShutdown()) {
            System.err.println("Warning: LogHandler executor is shut down, cannot log: " + message);
            return;
        }
        String logEntry = timestamp() + " " + message;
        executor.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                writer.write(logEntry);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Failed to write to log file: " + e.getMessage());
            }
        });
    }

    public static void logAndPrint(String message) {
        System.out.println(timestamp() + " " + message);
        System.out.flush();
        log(message);
    }

    public static void shutdown() {
        log("Flushing logs and shutting down logging system.");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(3, TimeUnit.SECONDS))
                    System.err.println("Log handler did not terminate properly.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    // TESTING ONLY
    public static void resetExecutorForTests() {
        if (executor.isShutdown() || executor.isTerminated()) {
            executor = Executors.newSingleThreadExecutor();
        }
    }
}
