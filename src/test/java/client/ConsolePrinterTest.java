package client;

import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsolePrinterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private String cleanCombinedOutput() {
        String all = outContent.toString() + errContent.toString();
        return all.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    @Test
    void testSuccess() {
        ConsolePrinter.success("Success message");
        assertTrue(cleanCombinedOutput().contains("Success message"));
    }

    @Test
    void testError() {
        ConsolePrinter.error("Error message");
        assertTrue(cleanCombinedOutput().contains("Error message"));
    }

    @Test
    void testInfo() {
        ConsolePrinter.info("Info message");
        assertTrue(cleanCombinedOutput().contains("Info message"));
    }

    @Test
    void testPrompt() {
        ConsolePrinter.prompt("Prompt message");
        assertTrue(cleanCombinedOutput().contains("Prompt message"));
    }

    @Test
    void testRaw() {
        ConsolePrinter.raw("Raw output");
        assertTrue(cleanCombinedOutput().contains("Raw output"));
    }

    @Test
    void testBanner() {
        ConsolePrinter.banner("Title");
        assertTrue(cleanCombinedOutput().contains("Title"));
    }

    @Test
    void testInfoWithTimestamp() {
        ConsolePrinter.infoWithTimestamp("Timed info");
        assertTrue(cleanCombinedOutput().contains("Timed info"));
    }

    @Test
    void testProgressDots() {
        ConsolePrinter.progressDots("Loading", 3, 10);
        assertTrue(cleanCombinedOutput().contains("Loading..."));
    }
}
