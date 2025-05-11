package server.handler;

import lombok.extern.slf4j.Slf4j;
import utils.LogHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles communication with a single client in a dedicated thread.
 * Reads input from the client, logs all interactions (safely),
 * and delegates request handling to the CommandHandler.
 */
@Slf4j
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final CommandHandler commandHandler;

    public ClientHandler(Socket clientSocket, CommandHandler commandHandler) {
        this.clientSocket = clientSocket;
        this.commandHandler = commandHandler;
    }

    @Override
    public void run() {
        String clientAddress = clientSocket.getRemoteSocketAddress().toString();
        log.info("New client connected: {}", clientAddress);
        LogHandler.info("New client connected: " + clientAddress);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                String command = inputLine.contains("%%") ? inputLine.split("%%")[0] : "MALFORMED";
                log.debug("Received {} command from {}", command, clientAddress);
                LogHandler.info("Received " + command + " command from " + clientAddress);

                commandHandler.handle(inputLine, clientSocket, out);
            }

        } catch (IOException e) {
            log.error("Connection error with {}: {}", clientAddress, e.getMessage());
            LogHandler.error("Connection error with " + clientAddress + ": " + e.getMessage());

        } finally {
            try {
                clientSocket.close();
                log.info("Connection closed: {}", clientAddress);
                LogHandler.info("Connection closed: " + clientAddress);
            } catch (IOException e) {
                log.error("Error closing connection with {}: {}", clientAddress, e.getMessage());
                LogHandler.error("Error closing connection with " + clientAddress + ": " + e.getMessage());
            }
        }
    }
}
