package utils;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogHandlerTest {

    private static final String TEST_LOG_FILE = Constants.LOG_FILE_PATH;

    @BeforeAll
    void beforeAll() {
        LogHandler.resetExecutorForTests();
    }

    @BeforeEach
    void setUp() throws IOException {
        LogHandler.resetExecutorForTests(); // restart thread!
        Files.createDirectories(Paths.get("logs"));
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
        Files.createFile(Paths.get(TEST_LOG_FILE));
    }

    @AfterEach
    void tearDown() {
        LogHandler.shutdown(); // clean shutdown
    }

    @Test
    void testLogFileCreation() {
        assertTrue(Files.exists(Paths.get(TEST_LOG_FILE)), "Log file should exist after setup.");
    }

    @Test
    void testSimpleLogEntry() throws Exception {
        String testMessage = "Test simple log entry";
        LogHandler.log(testMessage);
        TimeUnit.MILLISECONDS.sleep(300);
        assertTrue(Files.readString(Paths.get(TEST_LOG_FILE)).contains(testMessage));
    }

    @Test
    void testLogAndPrintEntry() throws Exception {
        String testMessage = "Test log and print entry";
        LogHandler.logAndPrint(testMessage);
        TimeUnit.MILLISECONDS.sleep(300);
        assertTrue(Files.readString(Paths.get(TEST_LOG_FILE)).contains(testMessage));
    }

    @Test
    void testMultipleLogEntries() throws Exception {
        for (int i = 0; i < 5; i++) {
            LogHandler.log("Message " + i);
        }
        TimeUnit.MILLISECONDS.sleep(500);
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
        TimeUnit.MILLISECONDS.sleep(300);
        assertTrue(Files.readString(Paths.get(TEST_LOG_FILE)).contains("Message before shutdown"));
    }
}
