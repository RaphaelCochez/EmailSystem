package server.handler;

import utils.LogHandler;
import utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a single client over a socket connection.
 * Thread-safe by design: each handler runs in its own thread with isolated
 * socket I/O.
 *
 * @ThreadSafe
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final CommandHandler commandHandler;

    public ClientHandler(Socket socket, CommandHandler commandHandler) {
        this.socket = socket;
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        String clientInfo = socket.getRemoteSocketAddress().toString();
        LogHandler.log("Client connected: " + clientInfo);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String input;
            while ((input = in.readLine()) != null) {
                if (Constants.DEBUG_MODE) {
                    LogHandler.log("Received command from " + clientInfo + ": " + input);
                }
                commandHandler.handle(input, socket, out);
            }

        } catch (IOException e) {
            LogHandler.log("Connection error with client " + clientInfo + ": " + e.getMessage());

        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LogHandler.log("Failed to close socket for client " + clientInfo + ": " + e.getMessage());
            }
            LogHandler.log("Client disconnected: " + clientInfo);
        }
    }
}
