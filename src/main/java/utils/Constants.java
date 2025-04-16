package utils;

public class Constants {

    // --- Encryption ---
    public static final String HASH_ALGORITHM = "SHA-256";
    public static final String SALT_DELIMITER = "$";
    public static final int SALT_LENGTH = 16; // bytes

    // === Server Configuration ===
    public static final int SERVER_PORT = 8080;
    public static final int MAX_CLIENTS = 50;

    // === File Paths ===
    public static final String USERS_DB_PATH = "src/main/resources/users.db";
    public static final String EMAILS_DB_PATH = "src/main/resources/emails.db";
    public static final String LOG_FILE_PATH = "logs/server.log";

    // === Required Directories (optional bootstrapping) ===
    public static final String[] REQUIRED_DIRECTORIES = {
            "src/main/resources",
            "src/test/resources",
            "logs"
    };

    // === Command Strings ===
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_LOGOUT = "LOGOUT";
    public static final String CMD_SEND = "SEND";
    public static final String CMD_LIST = "LIST";
    public static final String CMD_READ = "READ";
    public static final String CMD_DELETE = "DELETE";
    public static final String CMD_SEARCH = "SEARCH";
    public static final String CMD_EXIT = "EXIT";

    // === CLI Syntax Templates ===
    public static final String SYNTAX_REGISTER = "REGISTER <email> <password>";
    public static final String SYNTAX_LOGIN = "LOGIN <email> <password>";
    public static final String SYNTAX_LOGOUT = "LOGOUT";
    public static final String SYNTAX_SEND = "SEND <to> <subject> <body>";
    public static final String SYNTAX_LIST = "LIST [sent|inbox]";
    public static final String SYNTAX_READ = "READ <emailId>";
    public static final String SYNTAX_DELETE = "DELETE <emailId>";
    public static final String SYNTAX_SEARCH = "SEARCH <keyword>";
    public static final String SYNTAX_EXIT = "EXIT";

    // === Help Message for CLI ===
    public static final String CLIENT_HELP_TEXT = """
            Available commands:
            - REGISTER <email> <password>
            - LOGIN <email> <password>
            - LOGOUT
            - SEND <to> <subject> <body>
            - LIST [sent|inbox]
            - READ <emailId>
            - DELETE <emailId>
            - SEARCH <keyword>
            - EXIT
            """;

    // === Response Messages ===
    public static final String RESP_OK = "OK";
    public static final String RESP_ERROR = "ERROR";
    public static final String RESP_INVALID_COMMAND = "ERROR: Invalid command.";
    public static final String RESP_AUTH_FAILED = "ERROR: Authentication failed.";
    public static final String RESP_USER_EXISTS = "ERROR: User already exists.";
    public static final String RESP_USER_NOT_FOUND = "ERROR: User not found.";
    public static final String RESP_EMAIL_NOT_FOUND = "ERROR: Email not found.";
    public static final String RESP_SESSION_EXPIRED = "ERROR: Session expired.";

    // === Session Token Handling ===
    public static final String SESSION_TOKEN_HEADER = "Session-Token";

    // === Misc ===
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    // --- Environment Flags ---
    public static final boolean DEBUG_MODE = true; // Enables verbose logging
    public static final boolean IS_TEST_ENVIRONMENT = false; // Set true during integration tests
    public static final boolean ENABLE_TLS = false; // Future use for encrypted socket communication

}
