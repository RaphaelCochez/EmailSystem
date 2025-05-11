package utils;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogHandlerTest {

    private static final Path LOG_FILE = Path.of("logs", "server.log");

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(LOG_FILE.getParent());
        Files.deleteIfExists(LOG_FILE);
        Files.createFile(LOG_FILE);
        LogHandler.resetExecutorForTests(); // ensure clean executor for each test
    }

    private void flushLogs() {
        // Give time for async logger to write
        try {
            Thread.sleep(200); // Can be tuned; async flushes quickly
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<String> readLogLines() throws IOException {
        flushLogs();
        return Files.readAllLines(LOG_FILE);
    }

    @Test
    void testInfoLogIsWrittenToFile() throws IOException {
        LogHandler.info("Test INFO log");
        List<String> lines = readLogLines();
        assertTrue(lines.stream().anyMatch(l -> l.contains("INFO") && l.contains("Test INFO log")));
    }

    @Test
    void testWarnLogIsWrittenToFile() throws IOException {
        LogHandler.warn("Test WARN log");
        List<String> lines = readLogLines();
        assertTrue(lines.stream().anyMatch(l -> l.contains("WARN") && l.contains("Test WARN log")));
    }

    @Test
    void testErrorLogIsWrittenToFile() throws IOException {
        LogHandler.error("Test ERROR log");
        List<String> lines = readLogLines();
        assertTrue(lines.stream().anyMatch(l -> l.contains("ERROR") && l.contains("Test ERROR log")));
    }

    @Test
    void testLogHandlerShutdownIsNonBlocking() {
        assertDoesNotThrow(LogHandler::shutdown);
    }

    @AfterEach
    void tearDown() throws IOException {
        LogHandler.shutdown(); // ensure proper flush
        Files.deleteIfExists(LOG_FILE);
    }
}
