package server.handler;

import model.Email;
import server.service.AuthService;
import server.service.EmailService;
import server.service.SessionManager;
import utils.LogHandler;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static server.protocol.ProtocolConstants.*;

/**
 * Handles command parsing and delegation to appropriate services.
 */
public class CommandHandler {

    private final AuthService authService;
    private final EmailService emailService;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public CommandHandler(AuthService authService, EmailService emailService,
            SessionManager sessionManager) {
        this.authService = authService;
        this.emailService = emailService;
        this.sessionManager = sessionManager;
    }

    public void handle(String input, Socket clientSocket, PrintWriter out) {
        if (input == null || !input.contains(DELIMITER)) {
            LogHandler.log("Invalid input format from client " + clientSocket.getRemoteSocketAddress() + ": " + input);
            out.println("INVALID_FORMAT" + DELIMITER + "Missing delimiter");
            return;
        }

        String[] parts = input.split(DELIMITER, 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        try {
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();

            switch (command) {
                case CMD_REGISTER:
                    authService.handleRegister(payload, out);
                    break;
                case CMD_LOGIN:
                    authService.handleLogin(payload, clientSocket, out);
                    break;
                case CMD_SEND_EMAIL:
                    Email email = gson.fromJson(json, Email.class);
                    if (emailService.sendEmail(email)) {
                        out.println(RESP_SEND_EMAIL_SUCCESS);
                    } else {
                        LogHandler.log("SEND_EMAIL failed for user: " + email.getFrom());
                        out.println(RESP_SEND_EMAIL_FAIL + DELIMITER + "Validation or recipient failure");
                    }
                    break;
                case CMD_RETRIEVE_EMAILS:
                    String type = json.get("type").getAsString();
                    String userEmail = json.get("email").getAsString();
                    List<Email> emails = "received".equalsIgnoreCase(type)
                            ? emailService.getReceivedEmails(userEmail)
                            : emailService.getSentEmails(userEmail);
                    out.println(RESP_RETRIEVE_EMAILS_SUCCESS + DELIMITER + gson.toJson(emails));
                    break;
                case CMD_SEARCH_EMAIL:
                    List<Email> searchResults = emailService.searchEmails(
                            json.get("email").getAsString(),
                            json.get("type").getAsString(),
                            json.get("keyword").getAsString());
                    if (searchResults.isEmpty()) {
                        LogHandler.log("SEARCH_EMAIL: no results for user " + json.get("email").getAsString());
                        out.println(RESP_SEARCH_EMAIL_FAIL + DELIMITER + "No emails found matching keyword");
                    } else {
                        out.println(RESP_SEARCH_EMAIL_SUCCESS + DELIMITER + gson.toJson(searchResults));
                    }
                    break;
                case CMD_READ_EMAIL:
                    String reader = json.get("email").getAsString();
                    String id = json.get("id").getAsString();
                    Email result = emailService.getEmailById(reader, id);
                    if (result != null) {
                        out.println(RESP_READ_EMAIL_SUCCESS + DELIMITER + gson.toJson(result));
                    } else {
                        LogHandler.log("READ_EMAIL denied for user " + reader + " on email ID " + id);
                        out.println(RESP_READ_EMAIL_FAIL + DELIMITER + "Email not found or access denied");
                    }
                    break;
                case CMD_LOGOUT:
                    String logoutEmail = json.get("email").getAsString();
                    sessionManager.endSession(logoutEmail);
                    LogHandler.log("User logged out: " + logoutEmail);
                    out.println(RESP_LOGOUT_SUCCESS);
                    break;
                case CMD_EXIT:
                    LogHandler.log("EXIT command received from client: " + clientSocket.getRemoteSocketAddress());
                    out.println(RESP_EXIT_SUCCESS);
                    break;
                default:
                    LogHandler.log("Unknown command received from client: " + command);
                    out.println("UNKNOWN_COMMAND" + DELIMITER + "Command not recognized");
                    break;
            }
        } catch (Exception e) {
            LogHandler.log("Command [" + command + "] failed for socket " + clientSocket.getRemoteSocketAddress() + ": "
                    + e.getMessage());
            out.println(command + "_FAIL" + DELIMITER + e.getMessage());
        }
    }
}
