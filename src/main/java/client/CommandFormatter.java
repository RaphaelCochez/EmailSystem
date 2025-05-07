package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Formats CLI input into proper protocol messages: COMMAND%%{payload as JSON}
 */
public class CommandFormatter {

    private static final Gson gson = new Gson();

    public static String format(String command, Map<String, String> payload) {
        String json = gson.toJson(payload);
        return command.toUpperCase() + "%%" + json;
    }

    public static String register(String email, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return format("REGISTER", payload);
    }

    public static String login(String email, String password) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", password);
        return format("LOGIN", payload);
    }

    public static String logout(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        return format("LOGOUT", payload);
    }

    public static String sendEmail(String from, String to, String subject, String body, String timestamp) {
        Map<String, String> payload = new HashMap<>();
        payload.put("from", from);
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("body", body);
        payload.put("timestamp", timestamp);
        return format("SEND_EMAIL", payload);
    }

    public static String retrieveEmails(String email, String type) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("type", type);
        return format("RETRIEVE_EMAILS", payload);
    }

    public static String search(String email, String type, String keyword) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("type", type);
        payload.put("keyword", keyword);
        return format("SEARCH_EMAIL", payload);
    }

    public static String readEmail(String email, String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("id", id);
        return format("READ_EMAIL", payload);
    }

    public static String exit(String email) {
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        return "EXIT%%" + json;
    }

}
