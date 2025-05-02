package client;

import org.junit.jupiter.api.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ServerListenerTest {

    private ByteArrayOutputStream systemOut;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        systemOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testReadsRegularMessage() throws Exception {
        String input = "Hello from server\nEXIT_SUCCESS\n";
        BufferedReader reader = new BufferedReader(new StringReader(input));

        ServerListener listener = new ServerListener(reader);
        Thread thread = new Thread(listener);
        thread.start();

        thread.join(500); // Wait max 500ms for the thread to finish

        String output = systemOut.toString();
        assertTrue(output.contains("Hello from server"));
        assertTrue(output.contains("Server requested termination."));
    }

    @Test
    void testReadsExitMessageTriggersShutdown() throws Exception {
        String input = "EXIT_SUCCESS\n";
        BufferedReader reader = new BufferedReader(new StringReader(input));

        ServerListener listener = new ServerListener(reader);
        Thread thread = new Thread(listener);
        thread.start();

        thread.join(500);

        String output = systemOut.toString();
        assertTrue(output.contains("EXIT_SUCCESS"));
        assertTrue(output.contains("Listener shutting down."));
    }

    @Test
    void testIOExceptionHandledGracefully() throws Exception {
        BufferedReader brokenReader = new BufferedReader(new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated stream failure");
            }

            @Override
            public void close() {
            }
        });

        ServerListener listener = new ServerListener(brokenReader);
        Thread thread = new Thread(listener);
        thread.start();

        thread.join(500);

        String output = systemOut.toString();
        assertTrue(output.contains("Lost connection to server"));
        assertTrue(output.contains("Listener shutting down"));
    }
}
