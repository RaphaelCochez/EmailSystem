package utills;

public class Constants {

        // === Encryption ===
        public static final String HASH_ALGORITHM = "SHA-256";
        public static final String SALT_DELIMITER = "$";
        public static final int SALT_LENGTH = 16; // bytes

        // === Server Configuration ===
        public static final int SERVER_PORT = 8080;
        public static final int MAX_CLIENTS = 50;

        // === File Paths ===
        public static final String KEYSTORE_PATH = "resources/server.p12";
        public static final String KEYSTORE_PASSWORD = "d00285437"; // NOTE: for CA only
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
                        - SEARCH <keyword>
                        - EXIT
                        """;

        // === Time Format ===
        public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        // === Environment Flags ===
        public static final boolean DEBUG_MODE = true;
        public static final boolean IS_TEST_ENVIRONMENT = false;

        // === Future Use ===
        public static final boolean ENABLE_TLS = false;
        public static final String SESSION_TOKEN_HEADER = "Session-Token";
}
