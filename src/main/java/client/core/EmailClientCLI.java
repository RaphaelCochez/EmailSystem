package client.core;

import client.auth.AuthController;
import client.handler.*;
import client.router.CommandRouter;
import model.Email;
import utils.ConsoleConstants;
import utils.ConsolePrinter;
import utils.ServerConstants;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the email client CLI.
 */
public class EmailClientCLI {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Scanner scanner = new Scanner(System.in);

    private Thread listenerThread;

    private final SessionState session = new SessionState();
    private final List<Email> receivedEmails = new ArrayList<>();

    public static void main(String[] args) {
        new EmailClientCLI().start();
    }

    public void start() {
        try (Socket clientSocket = new Socket("localhost", ServerConstants.SERVER_PORT)) {
            this.socket = clientSocket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            ConsolePrinter.banner(ConsoleConstants.WELCOME_BANNER);
            ConsolePrinter.info(ConsoleConstants.CONNECTION_SUCCESS);

            ServerResponseHandler responseHandler = new ServerResponseHandler(session, receivedEmails);
            CommandRouter router = new CommandRouter(
                    session,
                    out,
                    scanner,
                    new EmailSender(out, scanner),
                    new EmailReader(scanner, receivedEmails),
                    new EmailSearcher(out),
                    new EmailLister(out));
            AuthController authController = new AuthController(out, scanner, session);

            startListener(responseHandler);

            while (true) {
                if (!session.isLoggedIn()) {
                    showAuthMenu(authController);
                } else {
                    mainInteractionLoop(router);
                }
            }

        } catch (IOException e) {
            ConsolePrinter.error("Failed to connect to server: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void startListener(ServerResponseHandler handler) {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    handler.handle(line);
                }
            } catch (IOException e) {
                ConsolePrinter.error(ConsoleConstants.SERVER_DISCONNECTED);
                session.reset();
            }
        }, "ServerListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void showAuthMenu(AuthController authController) {
        while (!session.isLoggedIn()) {
            ConsolePrinter.divider();
            ConsolePrinter.raw(ConsoleConstants.MAIN_OPTIONS);
            ConsolePrinter.prompt("Select: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "1", "register" -> authController.registerUser();
                case "2", "login" -> authController.loginUser();
                case "3", "exit" -> exitClient();
                default -> ConsolePrinter.error(ConsoleConstants.INVALID_CHOICE);
            }
        }
    }

    private void mainInteractionLoop(CommandRouter router) {
        while (session.isLoggedIn()) {
            ConsolePrinter.prompt(ConsoleConstants.PROMPT_COMMAND);
            String input = scanner.nextLine().trim();
            router.handle(input);
        }
    }

    private void exitClient() {
        out.println(utils.CommandFormatter.exit(session.getEmail()));
        ConsolePrinter.progressDots(ConsoleConstants.GOODBYE, 3, 300);
        cleanup();
        System.exit(0);
    }

    private void cleanup() {
        try {
            if (listenerThread != null && listenerThread.isAlive())
                listenerThread.interrupt();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            ConsolePrinter.error("Cleanup error: " + e.getMessage());
        }
    }
}
