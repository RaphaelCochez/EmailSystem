package server.protocol;

/**
 * Defines all protocol-related constants used for client-server communication.
 * Includes command identifiers, response keywords, delimiters, and example JSON
 * payloads.
 */
public class ProtocolConstants {

    // === Delimiter ===
    public static final String DELIMITER = "%%";

    // === Command Identifiers ===
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_SEND_EMAIL = "SEND_EMAIL";
    public static final String CMD_RETRIEVE_EMAILS = "RETRIEVE_EMAILS";
    public static final String CMD_READ_EMAIL = "READ_EMAIL";
    public static final String CMD_SEARCH_EMAIL = "SEARCH_EMAIL";
    public static final String CMD_EXIT = "EXIT";

    // === Success Response Keywords ===
    public static final String RESP_REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String RESP_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String RESP_LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
    public static final String RESP_SEND_EMAIL_SUCCESS = "SEND_EMAIL_SUCCESS";
    public static final String RESP_RETRIEVE_EMAILS_SUCCESS = "RETRIEVE_EMAILS_SUCCESS";
    public static final String RESP_READ_EMAIL_SUCCESS = "READ_EMAIL_SUCCESS";
    public static final String RESP_SEARCH_EMAIL_SUCCESS = "SEARCH_EMAIL_SUCCESS";
    public static final String RESP_EXIT_SUCCESS = "EXIT_SUCCESS";

    // === Error Response Keywords ===
    public static final String RESP_REGISTER_FAIL = "REGISTER_FAIL";
    public static final String RESP_LOGIN_FAIL = "LOGIN_FAIL";
    public static final String RESP_LOGOUT_FAIL = "LOGOUT_FAIL";
    public static final String RESP_SEND_EMAIL_FAIL = "SEND_EMAIL_FAIL";
    public static final String RESP_RETRIEVE_EMAILS_FAIL = "RETRIEVE_EMAILS_FAIL";
    public static final String RESP_READ_EMAIL_FAIL = "READ_EMAIL_FAIL";
    public static final String RESP_SEARCH_EMAIL_FAIL = "SEARCH_EMAIL_FAIL";

    // === Example JSON Payloads (for development reference) ===
    public static final String JSON_REGISTER_EXAMPLE = "{\"email\":\"<user@example.com>\",\"password\":\"<password>\"}";

    public static final String JSON_LOGIN_EXAMPLE = "{\"email\":\"<user@example.com>\",\"password\":\"<password>\"}";

    public static final String JSON_LOGOUT_EXAMPLE = "{\"email\":\"<user@example.com>\"}";

    public static final String JSON_SEND_EMAIL_EXAMPLE = "{\"id\":\"<UUID>\",\"to\":\"<recipient@example.com>\",\"from\":\"<sender@example.com>\","
            +
            "\"subject\":\"<subject>\",\"body\":\"<body>\",\"timestamp\":\"<timestamp>\"," +
            "\"visible\":true,\"edited\":false}";

    public static final String JSON_RETRIEVE_EMAILS_EXAMPLE = "{\"email\":\"<user@example.com>\",\"type\":\"sent|received\"}";

    public static final String JSON_READ_EMAIL_EXAMPLE = "{\"email\":\"<user@example.com>\",\"id\":\"<emailId>\"}";

    public static final String JSON_SEARCH_EMAIL_EXAMPLE = "{\"email\":\"<user@example.com>\",\"type\":\"sent|received\",\"keyword\":\"<searchTerm>\"}";

    // Prevent instantiation
    private ProtocolConstants() {
    }
}
