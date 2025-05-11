package client.auth;

import utils.ConsoleConstants;
import utils.ConsolePrinter;

import java.util.Scanner;

/**
 * Handles prompting the user for registration credentials.
 */
public class RegisterPrompter {

    private final Scanner scanner;

    public RegisterPrompter(Scanner scanner) {
        this.scanner = scanner;
    }

    public LoginPrompter.Credentials prompt() {
        String email;
        String password;

        while (true) {
            ConsolePrinter.prompt(ConsoleConstants.ENTER_EMAIL);
            email = scanner.nextLine().trim();
            if (email.isEmpty()) {
                ConsolePrinter.error("Email cannot be empty.");
                continue;
            }
            if (!AuthValidator.isValidEmailFormat(email)) {
                ConsolePrinter.error("Invalid email format. Use e.g. user@example.com.");
                continue;
            }
            break;
        }

        while (true) {
            ConsolePrinter.prompt(ConsoleConstants.ENTER_PASSWORD);
            password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                ConsolePrinter.error("Password cannot be empty.");
                continue;
            }
            break;
        }

        return new LoginPrompter.Credentials(email, password);
    }
}
