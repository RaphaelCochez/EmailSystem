package client.auth;

import client.auth.LoginPrompter.Credentials;
import client.core.SessionState;
import utils.CommandFormatter;
import utils.ConsolePrinter;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Coordinates login and registration logic for the CLI.
 */
public class AuthController {

    private final PrintWriter out;
    private final Scanner scanner;
    private final SessionState session;

    public AuthController(PrintWriter out, Scanner scanner, SessionState session) {
        this.out = out;
        this.scanner = scanner;
        this.session = session;
    }

    public void loginUser() {
        Credentials creds = new LoginPrompter(scanner).prompt();
        session.setEmail(creds.email);
        session.setLoginFailReason(null);

        out.println(CommandFormatter.login(creds.email, creds.password));

        while (!session.isLoggedIn() && session.getLoginFailReason() == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (session.isLoggedIn())
            return;

        if (session.hasLoginFailedWithUserNotFound()) {
            ConsolePrinter.prompt("User not found. Would you like to register instead? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("y")) {
                registerUser();
            }
        }

        session.reset();
    }

    public void registerUser() {
        Credentials creds = new RegisterPrompter(scanner).prompt();
        out.println(CommandFormatter.register(creds.email, creds.password));
    }
}
