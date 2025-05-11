package utils;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static utils.ProtocolConstants.*;

/**
 * CommandFormatter is a utility class responsible for creating
 * protocol-compliant
 * command strings in the format: COMMAND%%{JSON payload}.
 * 
 * This class is used by the client to format outgoing requests to the server.
 */
public class CommandFormatter {

    private static final Gson gson = new Gson();

    private CommandFormatter() {
        // Utility class — prevent instantiation
    }

    /**
     * Formats a generic command and payload into protocol format.
     *
     * @param command Protocol command (e.g., REGISTER, LOGIN)
     * @param payload Key-value data to serialize as JSON
     * @return A formatted string like "COMMAND%%{json}"
     */
    public static String format(String command, Map<String, String> payload) {
        String json = gson.toJson(payload);
        return command.toUpperCase() + DELIMITER + json;
    }

    /**
     * Constructs a REGISTER command with user credentials.
     */
    public static String register(String email, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return format(CMD_REGISTER, payload);
    }

    /**
     * Constructs a LOGIN command with user credentials.
     */
    public static String login(String email, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return format(CMD_LOGIN, payload);
    }

    /**
     * Constructs a LOGOUT command for the given user.
     */
    public static String logout(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        return format(CMD_LOGOUT, payload);
    }

    /**
     * Constructs a SEND_EMAIL command with all required email fields.
     */
    public static String sendEmail(String from, String to, String subject, String body, String timestamp) {
        Map<String, String> payload = new HashMap<>();
        payload.put("from", from);
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("body", body);
        payload.put("timestamp", timestamp);
        return format(CMD_SEND_EMAIL, payload);
    }

    /**
     * Constructs a RETRIEVE_EMAILS command to fetch sent or received emails.
     */
    public static String retrieveEmails(String email, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("type", type); // expected values: "sent" or "received"
        return format(CMD_RETRIEVE_EMAILS, payload);
    }

    /**
     * Constructs a SEARCH_EMAIL command to find emails by keyword and type.
     */
    public static String search(String email, String type, String keyword) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("type", type);
        payload.put("keyword", keyword);
        return format(CMD_SEARCH_EMAIL, payload);
    }

    /**
     * Constructs a READ_EMAIL command to fetch a specific email by ID.
     */
    public static String readEmail(String email, String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("id", id);
        return format(CMD_READ_EMAIL, payload);
    }

    /**
     * Constructs an EXIT command to gracefully terminate the client session.
     */
    public static String exit(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        return format(CMD_EXIT, payload);
    }

    // === Future Extension Placeholders ===
    // public static String deleteEmail(String id) { ... }
    // public static String markAsRead(String emailId) { ... }
}
