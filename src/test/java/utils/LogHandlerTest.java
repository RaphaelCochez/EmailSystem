package utils;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogHandlerTest {

    private static final String ORIGINAL_LOG_PATH = Constants.LOG_FILE_PATH;
    private static final String TEST_LOG_FILE = "logs/test-server.log";

    @BeforeAll
    void beforeAll() {
        // Override the log path just for this test
        setFinalStatic(Constants.class, "LOG_FILE_PATH", TEST_LOG_FILE);
        LogHandler.resetExecutorForTests();
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(Paths.get("logs"));
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
        Files.createFile(Paths.get(TEST_LOG_FILE));
    }

    @AfterAll
    void tearDown() {
        LogHandler.shutdown();
        setFinalStatic(Constants.class, "LOG_FILE_PATH", ORIGINAL_LOG_PATH);
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
            final int index = i; // this makes it usable inside the lambda
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

    // Magic: Use reflection to override final static field
    private static void setFinalStatic(Class<?> clazz, String fieldName, String newValue) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            var modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);

            field.set(null, newValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to override constant for test: " + e.getMessage(), e);
        }
    }
}
