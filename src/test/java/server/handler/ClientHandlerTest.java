package server.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    private List<String> handledCommands;
    private FakeSocket socket;
    private FakeCommandHandler handler;

    @BeforeEach
    void setUp() {
        handledCommands = new ArrayList<>();
    }

    @Test
    void testClientHandlerProcessesValidCommands() throws IOException {
        String simulatedInput = "REGISTER%%{\"email\":\"x@y.com\",\"password\":\"pass\"}\nEXIT%%{}\n";
        socket = new FakeSocket(simulatedInput);
        handler = new FakeCommandHandler(handledCommands);

        ClientHandler clientHandler = new ClientHandler(socket, handler);
        clientHandler.run();

        boolean registerMatched = handledCommands.stream().anyMatch(cmd -> {
            if (!cmd.startsWith("REGISTER%%"))
                return false;
            String jsonPart = cmd.substring("REGISTER%%".length());
            try {
                JsonElement parsed = JsonParser.parseString(jsonPart);
                return parsed.getAsJsonObject().get("email").getAsString().equals("x@y.com");
            } catch (Exception e) {
                return false;
            }
        });

        boolean exitMatched = handledCommands.stream().anyMatch(cmd -> cmd.contains("EXIT%%{}"));
        assertTrue(registerMatched, "Expected REGISTER command with matching JSON");
        assertTrue(exitMatched, "Expected EXIT command to be processed");

        String response = socket.getCapturedOutput();
        assertTrue(response.contains("EXIT_SUCCESS"));
    }

    @Test
    void testClientHandlerHandlesIOException() {
        Socket errorSocket = new FakeSocketWithError();
        handler = new FakeCommandHandler(handledCommands);

        ClientHandler clientHandler = new ClientHandler(errorSocket, handler);

        assertDoesNotThrow(clientHandler::run);
    }

    @Test
    void testMalformedJsonCommandHandledGracefully() {
        String malformedInput = "LOGIN%%{bad_json}\nEXIT%%{}\n";
        socket = new FakeSocket(malformedInput);
        handler = new FakeCommandHandler(handledCommands);

        ClientHandler clientHandler = new ClientHandler(socket, handler);

        assertDoesNotThrow(clientHandler::run);

        assertTrue(handledCommands.stream().anyMatch(cmd -> cmd.startsWith("LOGIN%%{bad_json}")));
        assertTrue(socket.getCapturedOutput().contains("EXIT_SUCCESS"));
    }

    @Test
    void testClientHandlerExitsCleanlyOnEOF() {
        socket = new FakeSocket(""); // EOF input
        handler = new FakeCommandHandler(handledCommands);

        ClientHandler clientHandler = new ClientHandler(socket, handler);

        assertDoesNotThrow(clientHandler::run);
        assertTrue(handledCommands.isEmpty(), "No commands should be handled on EOF");
    }

    @Test
    void testMultipleCommandsAreFlushedSeparately() {
        String input = "REGISTER%%{\"email\":\"a@b.com\",\"password\":\"pw\"}\n" +
                "LOGIN%%{\"email\":\"a@b.com\",\"password\":\"pw\"}\n" +
                "EXIT%%{}\n";
        socket = new FakeSocket(input);
        handler = new FakeCommandHandler(handledCommands);

        ClientHandler clientHandler = new ClientHandler(socket, handler);
        clientHandler.run();

        String[] responses = socket.getCapturedOutput().split("\n");

        assertTrue(responses.length >= 3, "Each command should produce its own response line");
        assertTrue(responses[responses.length - 1].contains("EXIT_SUCCESS"));
    }

    // === Fake Mocks ===

    static class FakeSocket extends Socket {
        private final ByteArrayInputStream input;
        private final ByteArrayOutputStream output;

        public FakeSocket(String inputData) {
            this.input = new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
            this.output = new ByteArrayOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return input;
        }

        @Override
        public OutputStream getOutputStream() {
            return output;
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return new SocketAddress() {
                @Override
                public String toString() {
                    return "FAKE_CLIENT";
                }
            };
        }

        public String getCapturedOutput() {
            return output.toString(StandardCharsets.UTF_8);
        }
    }

    static class FakeSocketWithError extends Socket {
        @Override
        public InputStream getInputStream() throws IOException {
            throw new IOException("Simulated stream error");
        }

        @Override
        public OutputStream getOutputStream() {
            return new ByteArrayOutputStream();
        }

        @Override
        public SocketAddress getRemoteSocketAddress() {
            return new SocketAddress() {
                @Override
                public String toString() {
                    return "ERROR_CLIENT";
                }
            };
        }
    }

    static class FakeCommandHandler extends CommandHandler {
        private final List<String> record;

        public FakeCommandHandler(List<String> record) {
            super(null, null, null);
            this.record = record;
        }

        @Override
        public void handle(String input, Socket clientSocket, PrintWriter out) {
            input = input.trim();
            record.add(input);
            out.println(input + "_RECEIVED");
            if (input.startsWith("EXIT")) {
                out.println("EXIT_SUCCESS");
            }
            out.flush();
        }
    }
}
