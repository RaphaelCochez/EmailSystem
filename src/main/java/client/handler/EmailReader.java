package client.handler;

import model.Email;
import utils.ConsolePrinter;

import java.util.List;
import java.util.Scanner;

/**
 * Handles reading a specific email from the local cache by index.
 */
public class EmailReader {

    private final Scanner scanner;
    private final List<Email> receivedEmails;

    public EmailReader(Scanner scanner, List<Email> receivedEmails) {
        this.scanner = scanner;
        this.receivedEmails = receivedEmails;
    }

    public void readByIndex(String input) {
        String indexStr = input;

        // If no input was passed, prompt user interactively
        if (indexStr == null || indexStr.isBlank()) {
            ConsolePrinter.prompt("What email do you want to read by index? (e.g. 1 for the first email): ");
            indexStr = scanner.nextLine().trim();
        }

        try {
            int index = Integer.parseInt(indexStr) - 1;

            if (index < 0 || index >= receivedEmails.size()) {
                ConsolePrinter.error("Invalid index. No email found.");
                return;
            }

            Email email = receivedEmails.get(index);
            ConsolePrinter.divider();
            ConsolePrinter.info("Subject: " + email.getSubject());
            ConsolePrinter.info("From: " + email.getFrom());
            ConsolePrinter.info("Timestamp: " + email.getTimestamp());
            ConsolePrinter.info("Body:");
            ConsolePrinter.raw(email.getBody());
            ConsolePrinter.divider();

        } catch (NumberFormatException e) {
            ConsolePrinter.error("Invalid input. Please enter a number.");
        }
    }
}
