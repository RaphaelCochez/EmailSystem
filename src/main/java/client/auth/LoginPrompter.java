package client.auth;

import utils.ConsoleConstants;
import utils.ConsolePrinter;

import java.util.Scanner;

/**
 * Handles prompting the user for login credentials.
 */
public class LoginPrompter {

    private final Scanner scanner;

    public LoginPrompter(Scanner scanner) {
        this.scanner = scanner;
    }

    public Credentials prompt() {
        ConsolePrinter.prompt(ConsoleConstants.ENTER_EMAIL);
        String email = scanner.nextLine().trim();

        ConsolePrinter.prompt(ConsoleConstants.ENTER_PASSWORD);
        String password = scanner.nextLine().trim();

        return new Credentials(email, password);
    }

    /**
     * Immutable container for login credentials.
     */
    public static class Credentials {
        public final String email;
        public final String password;

        public Credentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
