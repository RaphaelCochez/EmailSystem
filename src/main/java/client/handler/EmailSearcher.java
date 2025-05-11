package client.handler;

import utils.CommandFormatter;
import utils.ConsolePrinter;

import java.io.PrintWriter;

/**
 * Handles keyword-based email searching by type.
 */
public class EmailSearcher {

    private final PrintWriter out;

    public EmailSearcher(PrintWriter out) {
        this.out = out;
    }

    public void search(String userEmail, String input) {
        if (input == null || input.isBlank()) {
            ConsolePrinter.error("Usage: SEARCH <sent|received> <keyword>");
            return;
        }

        String[] parts = input.trim().split(" ", 2);
        if (parts.length < 2) {
            ConsolePrinter.error("Usage: SEARCH <sent|received> <keyword>");
            return;
        }

        String type = parts[0].toLowerCase();
        String keyword = parts[1].trim();

        if (!type.equals("sent") && !type.equals("received")) {
            ConsolePrinter.error("Invalid type. Use 'sent' or 'received'.");
            return;
        }

        out.println(CommandFormatter.search(userEmail, type, keyword));
        ConsolePrinter.info("Searching " + type + " emails for: \"" + keyword + "\"");
    }
}
