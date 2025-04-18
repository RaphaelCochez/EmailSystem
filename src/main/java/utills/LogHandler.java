package utills;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class LogHandler {

    private static final String LOG_FILE = "logs/server.log";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    static {
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
        String logEntry = timestamp() + " " + message;
        System.out.println(logEntry);
        log(message);
    }

    public static void shutdown() {
        log("Flushing logs and shutting down logging system.");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
