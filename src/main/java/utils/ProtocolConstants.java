package utils;

/**
 * Defines all protocol command strings and server-client response codes.
 * These constants are used for formatting and parsing messages over the TCP
 * protocol.
 * This class is non-instantiable and should be referenced statically.
 */
public final class ProtocolConstants {

    // === General Protocol ===
    public static final String DELIMITER = "%%";

    // === Client-to-Server Commands ===
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_EXIT = "EXIT";
    public static final String CMD_SEND_EMAIL = "SEND_EMAIL";
    public static final String CMD_RETRIEVE_EMAILS = "RETRIEVE_EMAILS";
    public static final String CMD_READ_EMAIL = "READ_EMAIL";
    public static final String CMD_SEARCH_EMAIL = "SEARCH_EMAIL";

    // === Server-to-Client Success Responses ===
    public static final String RESPONSE_REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String RESPONSE_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String RESPONSE_LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
    public static final String RESPONSE_EXIT_SUCCESS = "EXIT_SUCCESS";
    public static final String RESPONSE_SEND_EMAIL_SUCCESS = "SEND_EMAIL_SUCCESS";
    public static final String RESPONSE_RETRIEVE_EMAILS_SUCCESS = "RETRIEVE_EMAILS_SUCCESS";
    public static final String RESPONSE_READ_EMAIL_SUCCESS = "READ_EMAIL_SUCCESS";
    public static final String RESPONSE_SEARCH_EMAIL_SUCCESS = "SEARCH_EMAIL_SUCCESS";

    // === Server-to-Client Failure Responses ===
    public static final String RESPONSE_REGISTER_FAIL = "REGISTER_FAIL";
    public static final String RESPONSE_LOGIN_FAIL = "LOGIN_FAIL";
    public static final String RESPONSE_LOGOUT_FAIL = "LOGOUT_FAIL";
    public static final String RESPONSE_SEND_EMAIL_FAIL = "SEND_EMAIL_FAIL";
    public static final String RESPONSE_RETRIEVE_EMAILS_FAIL = "RETRIEVE_EMAILS_FAIL";
    public static final String RESPONSE_READ_EMAIL_FAIL = "READ_EMAIL_FAIL";
    public static final String RESPONSE_SEARCH_EMAIL_FAIL = "SEARCH_EMAIL_FAIL";

    // === Server Errors / Special Cases ===
    public static final String RESPONSE_INVALID_FORMAT = "INVALID_FORMAT"; // Malformed JSON, missing fields, etc.
    public static final String RESPONSE_UNAUTHORIZED = "UNAUTHORIZED"; // Action requires login
    public static final String RESPONSE_UNKNOWN = "UNKNOWN_COMMAND"; // Unrecognized command

    // === Reserved for Future Protocol Commands (not implemented) ===
    // public static final String CMD_EDIT_EMAIL = "EDIT_EMAIL";
    // public static final String CMD_DELETE_EMAIL = "DELETE_EMAIL";
    // public static final String CMD_MARK_AS_READ = "MARK_AS_READ";
    // public static final String CMD_MOVE_EMAIL = "MOVE_EMAIL";

    // === Prevent instantiation ===
    private ProtocolConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }
}
