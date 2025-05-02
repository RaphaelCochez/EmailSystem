package server;

import server.data.FileDatabase;
import server.handler.ClientHandler;
import server.handler.CommandHandler;
import server.service.AuthService;
import server.service.EmailService;
import server.service.SessionManager;
import utils.Constants;
import utils.LogHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TCP server for multi-client email system.
 * Uses a thread pool to handle a maximum number of concurrent clients.
 */
public class EmailServer {

    private static final int PORT = Constants.SERVER_PORT;
    private static final int MAX_CLIENTS = Constants.MAX_CLIENTS;

    public static void main(String[] args) {
        LogHandler.log("Booting EmailServer on port " + PORT);

        // Load persisted data
        FileDatabase database = new FileDatabase(Constants.USERS_DB_PATH, Constants.EMAILS_DB_PATH);
        database.loadAll();

        // Initialize services
        SessionManager sessionManager = new SessionManager();
        EmailService emailService = new EmailService(database);
        AuthService authService = new AuthService(database, sessionManager);
        CommandHandler commandHandler = new CommandHandler(authService, emailService, sessionManager);

        // Create thread pool
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LogHandler.log("EmailServer is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, commandHandler));
            }

        } catch (IOException e) {
            LogHandler.log("Fatal server error: " + e.getMessage());
        } finally {
            LogHandler.log("Initiating shutdown sequence...");

            threadPool.shutdownNow();
            sessionManager.clearAllSessions();
            database.saveAll();
            LogHandler.shutdown();

            LogHandler.log("Server shutdown complete.");
        }
    }
}
