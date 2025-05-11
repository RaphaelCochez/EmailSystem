package server;

import lombok.extern.slf4j.Slf4j;
import server.data.FileDatabase;
import server.handler.ClientHandler;
import server.handler.CommandHandler;
import server.service.AuthService;
import server.service.EmailService;
import server.service.SessionManager;
import utils.LogHandler;
import utils.LogUtils;
import utils.ServerConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * TCP server for a multi-client email system.
 * Initializes core services, loads persisted data, and handles
 * incoming socket connections using a fixed thread pool.
 */
@Slf4j
public class EmailServer {

    private static final int PORT = ServerConstants.SERVER_PORT;
    private static final int MAX_CLIENTS = ServerConstants.MAX_CLIENTS;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);
    private ScheduledExecutorService monitorService;

    public static void main(String[] args) {
        LogHandler.log("Booting EmailServer on port " + PORT);
        new EmailServer().start();
    }

    public void start() {
        FileDatabase database = new FileDatabase(ServerConstants.USERS_DB_PATH, ServerConstants.EMAILS_DB_PATH);
        database.loadAll();

        SessionManager sessionManager = new SessionManager();
        EmailService emailService = new EmailService(database);
        AuthService authService = new AuthService(database, sessionManager);
        CommandHandler commandHandler = new CommandHandler(authService, emailService, sessionManager);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogHandler.log("Shutdown hook triggered: Saving state...");
            sessionManager.clearAllSessions();
            database.saveAll();
            shutdownMonitoring();
            LogHandler.shutdown();
            LogHandler.log("Shutdown hook completed. Goodbye.");
        }));

        startMonitoring(); // start thread monitor

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LogHandler.log("EmailServer is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, commandHandler));
            }

        } catch (IOException e) {
            LogHandler.error("Fatal server error: " + e.getMessage());
            LogUtils.printDebugStackTrace(e);
        } finally {
            LogHandler.log("Main thread exiting. Triggering shutdown...");
            threadPool.shutdownNow();
        }
    }

    /**
     * Periodically logs thread pool usage.
     */
    private void startMonitoring() {
        monitorService = Executors.newSingleThreadScheduledExecutor();
        monitorService.scheduleAtFixedRate(() -> {
            if (threadPool instanceof ThreadPoolExecutor executor) {
                int active = executor.getActiveCount();
                int queued = executor.getQueue().size();
                int poolSize = executor.getPoolSize();
                log.info("[Monitor] Threads: active={}, queued={}, pool={}", active, queued, poolSize);
                LogHandler.info(
                        String.format("Thread Monitor — active=%d, queued=%d, pool=%d", active, queued, poolSize));
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void shutdownMonitoring() {
        if (monitorService != null && !monitorService.isShutdown()) {
            monitorService.shutdown();
            try {
                if (!monitorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    monitorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                monitorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
