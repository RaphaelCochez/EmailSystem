package utills;

public class Constants {

        // Define your file paths and passwords
        public static final String KEYSTORE_PATH = "resources/server.p12"; // Keystore path
        public static final String KEYSTORE_PASSWORD = "d00285437"; // Keystore password
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

        // === Session Token Handling ===
        public static final String SESSION_TOKEN_HEADER = "Session-Token";

        // === Misc ===
        public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        // --- Environment Flags ---
        public static final boolean DEBUG_MODE = true; // Enables verbose logging
        public static final boolean IS_TEST_ENVIRONMENT = false; // Set true during integration tests
        public static final boolean ENABLE_TLS = false; // Future use for encrypted socket communication

}
