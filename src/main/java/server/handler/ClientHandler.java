package server.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import utils.LogHandler;

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
                commandHandler.handle(input, socket, out);
            }
        } catch (IOException e) {
            LogHandler.log("Connection error with client " + clientInfo + ": " + e.getMessage());
        } finally {
            LogHandler.log("Client disconnected: " + clientInfo);
        }
    }
}
