package server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import server.service.AuthService;
import server.service.EmailService;
import server.service.SessionManager;
import utils.LogHandler;
import utils.ProtocolConstants;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * Handles incoming client commands via the protocol interface.
 * Delegates to relevant services and ensures session validation.
 *
 * Supports JSON-based payloads with robust error and logging mechanisms.
 */
@Slf4j
public class CommandHandler {

    private final AuthService authService;
    private final EmailService emailService;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public CommandHandler(AuthService authService, EmailService emailService, SessionManager sessionManager) {
        this.authService = authService;
        this.emailService = emailService;
        this.sessionManager = sessionManager;
    }

    /**
     * Parses a command string, validates the session, delegates to service, and
     * handles output.
     *
     * @param input        Raw client input string (COMMAND%%JSON_PAYLOAD)
     * @param clientSocket Client socket instance
     * @param out          Output stream for response
     */
    public void handle(String input, Socket clientSocket, PrintWriter out) {
        if (input == null || !input.contains(ProtocolConstants.DELIMITER)) {
            String warning = "Malformed command from client " + clientSocket.getRemoteSocketAddress() + ": " + input;
            log.warn(warning);
            LogHandler.log(warning);
            out.println(ProtocolConstants.RESPONSE_INVALID_FORMAT + ProtocolConstants.DELIMITER + "Missing delimiter");
            return;
        }

        String[] parts = input.split(ProtocolConstants.DELIMITER, 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        try {
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();

            switch (command) {

                case ProtocolConstants.CMD_REGISTER -> authService.handleRegister(payload, out);

                case ProtocolConstants.CMD_LOGIN -> authService.handleLogin(payload, clientSocket, out);

                case ProtocolConstants.CMD_SEND_EMAIL -> {
                    Email email = gson.fromJson(json, Email.class);
                    if (!sessionManager.isLoggedIn(email.getFrom())) {
                        out.println(ProtocolConstants.RESPONSE_UNAUTHORIZED + ProtocolConstants.DELIMITER
                                + "User not logged in");
                        return;
                    }
                    boolean success = emailService.sendEmail(email);
                    if (success) {
                        out.println(ProtocolConstants.RESPONSE_SEND_EMAIL_SUCCESS);
                    } else {
                        log.warn("SEND_EMAIL failed for user: {}", email.getFrom());
                        LogHandler.log("SEND_EMAIL failed for user: " + email.getFrom());
                        out.println(ProtocolConstants.RESPONSE_SEND_EMAIL_FAIL + ProtocolConstants.DELIMITER
                                + "Validation or recipient failure");
                    }
                }

                case ProtocolConstants.CMD_RETRIEVE_EMAILS -> {
                    String type = json.has("type") ? json.get("type").getAsString() : null;
                    String userEmail = json.has("email") ? json.get("email").getAsString() : null;
                    if (type == null || userEmail == null) {
                        out.println(ProtocolConstants.RESPONSE_RETRIEVE_EMAILS_FAIL + ProtocolConstants.DELIMITER
                                + "Missing fields: 'type' or 'email'");
                        return;
                    }
                    if (!sessionManager.isLoggedIn(userEmail)) {
                        out.println(ProtocolConstants.RESPONSE_UNAUTHORIZED + ProtocolConstants.DELIMITER
                                + "User not logged in");
                        return;
                    }
                    List<Email> emails = "received".equalsIgnoreCase(type)
                            ? emailService.getReceivedEmails(userEmail)
                            : emailService.getSentEmails(userEmail);
                    out.println(ProtocolConstants.RESPONSE_RETRIEVE_EMAILS_SUCCESS + ProtocolConstants.DELIMITER
                            + gson.toJson(emails));
                }

                case ProtocolConstants.CMD_SEARCH_EMAIL -> {
                    String userEmail = json.has("email") ? json.get("email").getAsString() : null;
                    String type = json.has("type") ? json.get("type").getAsString() : null;
                    String keyword = json.has("keyword") ? json.get("keyword").getAsString() : null;

                    if (userEmail == null || type == null || keyword == null) {
                        out.println(ProtocolConstants.RESPONSE_SEARCH_EMAIL_FAIL + ProtocolConstants.DELIMITER
                                + "Missing fields");
                        return;
                    }

                    if (!sessionManager.isLoggedIn(userEmail)) {
                        out.println(ProtocolConstants.RESPONSE_UNAUTHORIZED + ProtocolConstants.DELIMITER
                                + "User not logged in");
                        return;
                    }

                    List<Email> results = emailService.searchEmails(userEmail, type, keyword);
                    if (results.isEmpty()) {
                        String msg = "SEARCH_EMAIL returned 0 results for user " + userEmail;
                        log.info(msg);
                        LogHandler.log(msg);
                        out.println(ProtocolConstants.RESPONSE_SEARCH_EMAIL_FAIL + ProtocolConstants.DELIMITER
                                + "No emails found matching keyword");
                    } else {
                        out.println(ProtocolConstants.RESPONSE_SEARCH_EMAIL_SUCCESS + ProtocolConstants.DELIMITER
                                + gson.toJson(results));
                    }
                }

                case ProtocolConstants.CMD_READ_EMAIL -> {
                    String user = json.has("email") ? json.get("email").getAsString() : null;
                    String id = json.has("id") ? json.get("id").getAsString() : null;

                    if (user == null || id == null) {
                        out.println(ProtocolConstants.RESPONSE_READ_EMAIL_FAIL + ProtocolConstants.DELIMITER
                                + "Missing 'email' or 'id'");
                        return;
                    }
                    if (!sessionManager.isLoggedIn(user)) {
                        out.println(ProtocolConstants.RESPONSE_UNAUTHORIZED + ProtocolConstants.DELIMITER
                                + "User not logged in");
                        return;
                    }

                    Email email = emailService.getEmailById(user, id);
                    if (email != null) {
                        out.println(ProtocolConstants.RESPONSE_READ_EMAIL_SUCCESS + ProtocolConstants.DELIMITER
                                + gson.toJson(email));
                    } else {
                        log.warn("READ_EMAIL denied for {} on email ID {}", user, id);
                        LogHandler.log("READ_EMAIL denied for " + user + " on email ID " + id);
                        out.println(ProtocolConstants.RESPONSE_READ_EMAIL_FAIL + ProtocolConstants.DELIMITER
                                + "Email not found or access denied");
                    }
                }

                case ProtocolConstants.CMD_LOGOUT -> {
                    String email = json.has("email") ? json.get("email").getAsString() : null;
                    if (email == null) {
                        out.println(ProtocolConstants.RESPONSE_LOGOUT_FAIL + ProtocolConstants.DELIMITER
                                + "Missing 'email'");
                        return;
                    }
                    sessionManager.endSession(email);
                    log.info("User logged out: {}", email);
                    LogHandler.log("User logged out: " + email);
                    out.println(ProtocolConstants.RESPONSE_LOGOUT_SUCCESS);
                }

                case ProtocolConstants.CMD_EXIT -> {
                    String email = json.has("email") ? json.get("email").getAsString() : null;

                    if (email != null && sessionManager.isLoggedIn(email)) {
                        sessionManager.endSession(email);
                        log.info("Session ended on EXIT for user: {}", email);
                        LogHandler.log("Session ended on EXIT for user: " + email);
                    } else {
                        String exitMsg = "EXIT from unauthenticated client: " + clientSocket.getRemoteSocketAddress();
                        log.warn(exitMsg);
                        LogHandler.log(exitMsg);
                    }

                    out.println(ProtocolConstants.RESPONSE_EXIT_SUCCESS);
                }

                default -> {
                    log.warn("Unknown command: {}", command);
                    LogHandler.log("Unknown command received: " + command);
                    out.println(ProtocolConstants.RESPONSE_UNKNOWN + ProtocolConstants.DELIMITER
                            + "Command not recognized");
                }
            }

        } catch (Exception e) {
            log.error("Command failed [{}]: {}", input, e.getMessage());
            LogHandler.log("Command [" + input + "] failed: " + e.getMessage());
            out.println(command + "_FAIL" + ProtocolConstants.DELIMITER + "Malformed JSON or internal error");
        }
    }
}
