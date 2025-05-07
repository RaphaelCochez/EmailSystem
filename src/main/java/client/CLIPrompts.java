package client;

/**
 * Centralized CLI prompt strings and formatting.
 */
public class CLIPrompts {

    public static final String WELCOME_BANNER = "Welcome to the Email System CLI";
    public static final String CONNECTION_SUCCESS = "Connected to Email Server.";
    public static final String MAIN_OPTIONS = """
            Choose an option:
            [1] Register
            [2] Login
            [3] Exit
            """;

    public static final String INVALID_CHOICE = "Invalid input. Please enter 1, 2, or 3.";
    public static final String ENTER_EMAIL = "Enter your email: ";
    public static final String ENTER_PASSWORD = "Enter your password: ";
    public static final String LOGIN_SUCCESS = "Login successful. Session started.";
    public static final String LOGIN_FAILURE = "Login failed. Please try again.";
    public static final String REGISTER_SUCCESS = "Registration complete. You can now log in.";
    public static final String REGISTER_FAILURE = "Registration failed. Try again with a different email.";

    public static final String PROMPT_COMMAND = "Enter command (type 'help' for options): ";
    public static final String GOODBYE = "Exiting. Goodbye!";
    public static final String PROMPT_TO_CONTINUE = "Press ENTER to continue...";
    public static final String SERVER_DISCONNECTED = "[CLIENT] Lost connection to the server.";

    public static final String LOGIN_WELCOME_PREFIX = "Login successful! Welcome, ";
    public static final String POST_LOGIN_COMMANDS = """
            You may now use the following commands:
            [1] LOGOUT
            [2] SEND <to> <subject> <body>
            [3] LIST [sent|inbox]
            [4] READ <emailId>
            [5] SEARCH <keyword>
            [6] EXIT
            """;

    private CLIPrompts() {
        // Utility class; prevent instantiation
    }
}
