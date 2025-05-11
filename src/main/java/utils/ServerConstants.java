package utils;

import java.util.Optional;

/**
 * Centralized constants used throughout the email server.
 * These include configuration settings, file paths, cryptographic settings,
 * environment-dependent variables, and server behavior flags.
 */
public class ServerConstants {

        // === Cryptographic Settings ===
        public static final String HASH_ALGORITHM = "SHA-256";
        public static final String SALT_DELIMITER = "$";
        public static final int SALT_LENGTH = 16; // in bytes

        // === Server Networking Configuration ===
        public static final int SERVER_PORT = parsePort(System.getenv("SERVER_PORT"), 18080);
        public static final int MAX_CLIENTS = 50;

        // === File System Paths ===
        public static final String USERS_DB_PATH = "src/main/resources/users.db";
        public static final String EMAILS_DB_PATH = "src/main/resources/emails.db";
        public static final String LOG_FILE_PATH = "logs/server.log";

        // === Keystore Configuration (used for TLS or future HTTPS support) ===
        public static final String KEYSTORE_PATH = "resources/server.p12";

        // WARNING: Fallback password for CA testing only – override using environment
        // variable KEYSTORE_PASSWORD
        private static final String DEFAULT_KEYSTORE_PASSWORD = "d00285437";
        public static final String KEYSTORE_PASSWORD = Optional.ofNullable(System.getenv("KEYSTORE_PASSWORD"))
                        .orElse(DEFAULT_KEYSTORE_PASSWORD);

        // === Required Directories for Application Startup ===
        public static final String[] REQUIRED_DIRECTORIES = {
                        "src/main/resources",
                        "src/test/resources",
                        "logs"
        };

        // === Date/Time Format for email timestamps ===
        public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        // === Server Behavior Flags ===
        public static final boolean DEBUG_MODE = true;
        public static final boolean IS_TEST_ENVIRONMENT = false;
        public static final boolean ENABLE_TLS = false;

        // === Reserved Headers (e.g., for future authentication token support) ===
        public static final String SESSION_TOKEN_HEADER = "Session-Token";

        /**
         * Parses an environment-provided port or falls back to a default.
         *
         * @param envValue The environment variable value
         * @param fallback The default value if not present or invalid
         * @return A valid port number
         */
        private static int parsePort(String envValue, int fallback) {
                try {
                        return envValue != null ? Integer.parseInt(envValue) : fallback;
                } catch (NumberFormatException e) {
                        System.err.println("[WARN] Invalid SERVER_PORT env var: " + envValue + ". Using fallback: "
                                        + fallback);
                        return fallback;
                }
        }

        // === Socket Behavior Flags ===
        public static final int SOCKET_TIMEOUT_MS = 300_000; // 5 minutes

        // Prevent instantiation
        private ServerConstants() {
        }
}
