package client.handler;

import utils.CommandFormatter;
import utils.ConsolePrinter;

import java.io.PrintWriter;

/**
 * Handles listing of sent or received emails.
 */
public class EmailLister {

    private final PrintWriter out;

    public EmailLister(PrintWriter out) {
        this.out = out;
    }

    public void listEmails(String userEmail, String type) {
        if (!type.equalsIgnoreCase("sent") && !type.equalsIgnoreCase("received")) {
            ConsolePrinter.error("Invalid type. Use 'sent' or 'received'. Defaulting to 'received'.");
            type = "received";
        }

        out.println(CommandFormatter.retrieveEmails(userEmail, type));
        ConsolePrinter.info("Fetching " + type + " emails...");
    }
}
