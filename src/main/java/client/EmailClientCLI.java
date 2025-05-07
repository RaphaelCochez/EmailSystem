package client;

import utils.Constants;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.google.gson.Gson;

import model.Email;

public class EmailClientCLI {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;
    private volatile boolean loggedIn = false;
    private String sessionEmail;
    private final Scanner scanner = new Scanner(System.in);
    private List<Email> receivedEmails = new ArrayList<>();
    private final Gson gson = new Gson();

    public static void main(String[] args) {
        new EmailClientCLI().start();
    }

    public void start() {
        try (Socket clientSocket = new Socket("localhost", Constants.SERVER_PORT)) {
            this.socket = clientSocket;
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            ConsolePrinter.banner(CLIPrompts.WELCOME_BANNER);
            ConsolePrinter.info(CLIPrompts.CONNECTION_SUCCESS);

            startListener();
            while (true) {
                if (!loggedIn) {
                    showAuthMenu();
                } else {
                    mainInteractionLoop();
                }
            }

        } catch (IOException e) {
            ConsolePrinter.error("Failed to connect to server: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    processServerResponse(line);
                }
            } catch (IOException e) {
                ConsolePrinter.error(CLIPrompts.SERVER_DISCONNECTED);
                loggedIn = false;
            }
        }, "ServerListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void processServerResponse(String response) {
        // comment out to disable server responeses in the terminal
        ConsolePrinter.infoWithTimestamp("[SERVER] " + response);

        switch (response.split("%%")[0]) {
            case "LOGIN_SUCCESS" -> {
                ConsolePrinter.success(CLIPrompts.LOGIN_WELCOME_PREFIX + sessionEmail);
                ConsolePrinter.divider();
                ConsolePrinter.raw(CLIPrompts.POST_LOGIN_COMMANDS);
                loggedIn = true;
            }

            case "LOGIN_FAIL" -> {
                sessionEmail = null;
                ConsolePrinter.error(CLIPrompts.LOGIN_FAILURE);
            }
            case "REGISTER_SUCCESS" -> ConsolePrinter.success(CLIPrompts.REGISTER_SUCCESS);
            case "REGISTER_FAIL" -> ConsolePrinter.error(CLIPrompts.REGISTER_FAILURE);
            case "SEND_EMAIL_SUCCESS" -> {
                ConsolePrinter.success("Email sent successfully!");
            }
            case "SEND_EMAIL_FAIL" -> {
                ConsolePrinter.error("Failed to send email.");
            }
            case "LOGOUT_SUCCESS" -> {
                ConsolePrinter.info("You have been logged out.");
                loggedIn = false;
                sessionEmail = null;
            }
            case "EXIT_SUCCESS" -> {
                ConsolePrinter.info("Server confirmed disconnection.");
                loggedIn = false;
            }
            case "RETRIEVE_EMAILS_SUCCESS" -> {
                String emailJson = response.split("%%")[1]; // Extract the JSON part
                List<Email> emails = gson.fromJson(emailJson, new TypeToken<List<Email>>() {
                }.getType());

                // Clear the previous list of emails and add the newly fetched emails
                receivedEmails.clear();
                receivedEmails.addAll(emails);

                // Display the updated email list
                displayEmailList();
            }
        }
    }

    private void displayEmailList() {
        // If no emails found
        if (receivedEmails.isEmpty()) {
            ConsolePrinter.info("No emails found.");
        } else {
            // Display the list of emails
            ConsolePrinter.info("Displaying emails:");
            for (int i = 0; i < receivedEmails.size(); i++) {
                Email email = receivedEmails.get(i);
                ConsolePrinter.info((i + 1) + ". Subject: " + email.getSubject() + ", From: " + email.getFrom());
                ConsolePrinter.info("   Timestamp: " + email.getTimestamp());
            }
        }
    }

    private void showAuthMenu() {
        while (!loggedIn) {
            ConsolePrinter.divider();
            ConsolePrinter.raw(CLIPrompts.MAIN_OPTIONS);
            ConsolePrinter.prompt("Select: ");
            String choice = scanner.nextLine().trim().toLowerCase();

            switch (choice) {
                case "1", "register" -> registerUser();
                case "2", "login" -> loginUser();
                case "3", "exit" -> exitClient();
                default -> ConsolePrinter.error(CLIPrompts.INVALID_CHOICE);
            }
        }
    }

    private void registerUser() {
        ConsolePrinter.prompt(CLIPrompts.ENTER_EMAIL);
        String email = scanner.nextLine().trim();
        ConsolePrinter.prompt(CLIPrompts.ENTER_PASSWORD);
        String password = scanner.nextLine().trim();
        out.println(CommandFormatter.register(email, password));
    }

    private void loginUser() {
        ConsolePrinter.prompt(CLIPrompts.ENTER_EMAIL);
        sessionEmail = scanner.nextLine().trim();
        ConsolePrinter.prompt(CLIPrompts.ENTER_PASSWORD);
        String password = scanner.nextLine().trim();
        out.println(CommandFormatter.login(sessionEmail, password));

        // Wait for login response to be processed by listener
        while (!loggedIn) {
            try {
                Thread.sleep(100); // wait until listener sets loggedIn
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void mainInteractionLoop() {
        while (loggedIn) {
            ConsolePrinter.prompt(CLIPrompts.PROMPT_COMMAND);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                handleCommand(input);
            }
        }

        // When loggedIn becomes false, go back to auth menu
        showAuthMenu();
    }

    private void handleCommand(String input) {
        String[] tokens = input.split(" ", 2);
        String command = tokens[0].toLowerCase();

        switch (command) {
            case "1", "logout" -> out.println(CommandFormatter.logout(sessionEmail));
            case "2", "send" -> handleSend(tokens.length > 1 ? tokens[1] : "");
            case "3", "list" -> handleList(tokens.length > 1 ? tokens[1] : "received");
            case "4", "read" -> handleRead(tokens.length > 1 ? tokens[1] : "");
            case "5", "search" -> handleSearch(tokens.length > 1 ? tokens[1] : "");
            case "6", "exit" -> {
                out.println(CommandFormatter.exit(sessionEmail != null ? sessionEmail : ""));
                ConsolePrinter.progressDots(CLIPrompts.GOODBYE, 3, 300);
                loggedIn = false;
            }
            case "help" -> ConsolePrinter.raw(CLIPrompts.POST_LOGIN_COMMANDS);
            default -> ConsolePrinter.error("Unknown command. Type 'help' to see available options.");
        }
    }

    public void handleSend(String command) {
        // Start interactive prompts
        ConsolePrinter.prompt("Enter the recipient email (to): ");
        String to = scanner.nextLine().trim();

        ConsolePrinter.prompt("Enter the subject: ");
        String subject = scanner.nextLine().trim();

        ConsolePrinter.prompt("Enter the body of the email: ");
        String body = scanner.nextLine().trim();

        String from = sessionEmail; // Use the logged-in user's email
        String id = UUID.randomUUID().toString(); // Generate a unique ID for the email
        String timestamp = getCurrentTimestamp(); // Get current timestamp

        // Build the JSON payload for the email
        JsonObject emailPayload = new JsonObject();
        emailPayload.addProperty("id", id);
        emailPayload.addProperty("to", to);
        emailPayload.addProperty("from", from);
        emailPayload.addProperty("subject", subject);
        emailPayload.addProperty("body", body);
        emailPayload.addProperty("timestamp", timestamp);
        emailPayload.addProperty("visible", true);
        emailPayload.addProperty("edited", false);

        // Send the email command to the server
        out.println("SEND_EMAIL%%" + emailPayload.toString());

        // Provide feedbackS
        ConsolePrinter.success("Email staged successfully to " + to);
    }

    private void handleList(String type) {
        if (!type.equals("sent") && !type.equals("received")) {
            type = "received"; // Default to "received" if type is invalid
        }

        // Send the request to the server to get emails
        out.println(CommandFormatter.retrieveEmails(sessionEmail, type));

        // Inform the user that the request is being processed
        ConsolePrinter.info("Fetching " + type + " emails...");
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    private void handleRead(String input) {
        if (input.equals("")) {
            // If no index is provided, prompt the user for the email to read.
            ConsolePrinter.prompt("What email do you want to read by index? (e.g. 1 for the first email): ");
            String indexStr = scanner.nextLine().trim();

            // Handle reading the email by index
            try {
                int index = Integer.parseInt(indexStr) - 1; // Adjust to 0-based index

                // Check if index is within bounds
                if (index >= 0 && index < receivedEmails.size()) {
                    Email email = receivedEmails.get(index);

                    // Display the email
                    ConsolePrinter.info("Reading Email " + (index + 1) + ":");
                    ConsolePrinter.info("Subject: " + email.getSubject());
                    ConsolePrinter.info("From: " + email.getFrom());
                    ConsolePrinter.info("Timestamp: " + email.getTimestamp());
                    ConsolePrinter.info("Body: " + email.getBody());
                } else {
                    ConsolePrinter.error("Invalid index. No email found.");
                }
            } catch (NumberFormatException e) {
                ConsolePrinter.error("Invalid input. Please enter a valid number.");
            }
            showAuthMenu();
        } else {
            // If an index is passed directly, proceed with the previous logic
            handleRead(input);
        }
    }

    private void handleSearch(String input) {
        String[] parts = input.split(" ", 2);
        if (parts.length < 2) {
            ConsolePrinter.error("Usage: SEARCH <sent|received> <keyword>");
            return;
        }
        String type = parts[0], keyword = parts[1];
        out.println(CommandFormatter.search(sessionEmail, type, keyword));
    }

    private void exitClient() {
        out.println(CommandFormatter.exit(sessionEmail != null ? sessionEmail : ""));
        ConsolePrinter.progressDots(CLIPrompts.GOODBYE, 3, 300);
        cleanup(); // Ensuring cleanup before exit
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