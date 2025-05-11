package utils;

/**
 * Centralized CLI prompt strings and display messages.
 */
public class ConsoleConstants {

    // === Welcome & General ===
    public static final String WELCOME_BANNER = "Welcome to the Email System CLI";
    public static final String CONNECTION_SUCCESS = "Connected to Email Server.";
    public static final String GOODBYE = "Exiting. Goodbye!";
    public static final String PROMPT_TO_CONTINUE = "Press ENTER to continue...";
    public static final String INVALID_CHOICE = "Invalid input. Please enter 1, 2, or 3.";

    // === Login & Registration Prompts ===
    public static final String ENTER_EMAIL = "Enter your email: ";
    public static final String ENTER_PASSWORD = "Enter your password: ";
    public static final String LOGIN_SUCCESS_MSG = "Login successful. Session started.";
    public static final String LOGIN_FAILURE_MSG = "Login failed. Please try again.";
    public static final String REGISTER_SUCCESS_MSG = "Registration complete. You can now log in.";
    public static final String REGISTER_FAILURE_MSG = "Registration failed. Try again with a different email.";

    // === Menus ===
    public static final String MAIN_OPTIONS = """
            Choose an option:
            [1] Register
            [2] Login
            [3] Exit
            """;

    public static final String POST_LOGIN_COMMANDS = """
            You may now use the following commands:
            [1] LOGOUT
            [2] SEND <to> <subject> <body>
            [3] LIST [sent|received]
            [4] READ <emailId>
            [5] SEARCH <sent|received> <keyword>
            [6] EXIT
            """;

    // === Command Input ===
    public static final String PROMPT_COMMAND = "Enter command (type 'help' for options): ";
    public static final String LOGIN_WELCOME_PREFIX = "Login successful! Welcome, ";

    // === System/Runtime Messages ===
    public static final String SERVER_DISCONNECTED = "[CLIENT] Lost connection to the server.";
    public static final String SERVER_TERMINATION = "[CLIENT] Server requested termination.";
    public static final String LISTENER_SHUTDOWN = "[CLIENT] Listener shutting down.";
    public static final String SERVER_PREFIX = "[SERVER] ";

    // === Email Operation Messages ===
    public static final String EMAIL_SEND_SUCCESS_MSG = "Email sent successfully!";
    public static final String EMAIL_SEND_FAIL_MSG = "Failed to send email.";
    public static final String EMAIL_RETRIEVE_FAIL_MSG = "Email retrieval failed: missing payload.";
    public static final String EMAIL_LIST_EMPTY_MSG = "No emails found.";
    public static final String EMAIL_LIST_HEADER_MSG = "Displaying emails:";

    // === Session Messages ===
    public static final String LOGOUT_SUCCESS_MSG = "You have been logged out.";
    public static final String EXIT_SUCCESS_MSG = "Server confirmed disconnection.";

    // === Fallback / Debug ===
    public static final String UNHANDLED_RESPONSE_MSG = "Unhandled response: ";

    private ConsoleConstants() {
        // Utility class — prevent instantiation
    }
}
