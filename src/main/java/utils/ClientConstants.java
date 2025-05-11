package utils;

/**
 * Constants specific to the client-side configuration and behavior.
 */
public class ClientConstants {

    // === Server Connection Configuration ===
    public static final String SERVER_HOST = getEnv("SERVER_HOST", "localhost"); // Default to localhost
    public static final int SERVER_PORT = parsePort(System.getenv("SERVER_PORT"), 18080); // Default to 18080

    // === Internal helpers ===
    private static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    private static int parsePort(String envValue, int fallback) {
        try {
            return envValue != null ? Integer.parseInt(envValue) : fallback;
        } catch (NumberFormatException e) {
            System.err.println("[WARN] Invalid SERVER_PORT env var: " + envValue + ". Using fallback: " + fallback);
            return fallback;
        }
    }

    // === CLI Retry Limits ===
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int MAX_REGISTER_ATTEMPTS = 3;

    // === Timeouts and Delays ===
    public static final int RECONNECT_DELAY_MS = 2000;
    public static final int COMMAND_RESPONSE_TIMEOUT_MS = 5000;

    // === Client-side flags ===
    public static final boolean ENABLE_LOCAL_CACHE = false; // If you implement caching
    public static final boolean DEBUG_MODE = true; // For verbose client logs

    // === Session Defaults ===
    public static final String DEFAULT_COMMAND_PROMPT = "cli>";
    public static final String DEFAULT_USER_EMAIL = ""; // Empty when not logged in

    private ClientConstants() {
        // Prevent instantiation
    }
}
