package utills;

import org.junit.jupiter.api.*;

import utils.LogHandler;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogHandlerTest {

    private static final String TEST_LOG_DIR = "logs/";
    private static final String TEST_LOG_FILE = "logs/server.log";

    @BeforeEach
    void setUp() throws IOException {
        // Clean up the log file before each test
        Files.createDirectories(Paths.get(TEST_LOG_DIR));
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
        Files.createFile(Paths.get(TEST_LOG_FILE));
    }

    @AfterAll
    void tearDown() {
        LogHandler.shutdown();
    }

    @Test
    void testLogFileCreation() {
        assertTrue(Files.exists(Paths.get(TEST_LOG_FILE)), "Log file should exist after setup.");
    }

    @Test
    void testSimpleLogEntry() throws Exception {
        String testMessage = "Test simple log entry";

        LogHandler.log(testMessage);

        TimeUnit.MILLISECONDS.sleep(300); // Wait for async log

        List<String> lines = Files.readAllLines(Paths.get(TEST_LOG_FILE));
        assertTrue(lines.stream().anyMatch(line -> line.contains(testMessage)),
                "Log file should contain the test message.");
    }

    @Test
    void testLogAndPrintEntry() throws Exception {
        String testMessage = "Test log and print entry";

        LogHandler.logAndPrint(testMessage);

        TimeUnit.MILLISECONDS.sleep(300); // Wait for async log

        List<String> lines = Files.readAllLines(Paths.get(TEST_LOG_FILE));
        assertTrue(lines.stream().anyMatch(line -> line.contains(testMessage)),
                "Log file should contain the test logAndPrint message.");
    }

    @Test
    void testMultipleLogEntries() throws Exception {
        for (int i = 0; i < 5; i++) {
            LogHandler.log("Message " + i);
        }

        TimeUnit.MILLISECONDS.sleep(500); // Wait longer for async log

        List<String> lines = Files.readAllLines(Paths.get(TEST_LOG_FILE));
        for (int i = 0; i < 5; i++) {
            final int index = i;
            assertTrue(lines.stream().anyMatch(line -> line.contains("Message " + index)),
                    "Log file should contain Message " + index);
        }
    }

    @Test
    void testShutdownFlushesLogs() throws Exception {
        LogHandler.log("Message before shutdown");

        LogHandler.shutdown();

        TimeUnit.MILLISECONDS.sleep(300); // Give time to flush

        List<String> lines = Files.readAllLines(Paths.get(TEST_LOG_FILE));
        assertTrue(lines.stream().anyMatch(line -> line.contains("Message before shutdown")),
                "Log file should contain the shutdown message.");
    }
}
