package client.router;

import client.core.SessionState;
import client.handler.EmailLister;
import client.handler.EmailReader;
import client.handler.EmailSearcher;
import client.handler.EmailSender;
import utils.CommandFormatter;
import utils.ConsoleConstants;
import utils.ConsolePrinter;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Routes parsed commands to their appropriate handler classes.
 */
public class CommandRouter {

    private final SessionState session;
    private final PrintWriter out;

    private final EmailSender emailSender;
    private final EmailReader emailReader;
    private final EmailSearcher emailSearcher;
    private final EmailLister emailLister;

    public CommandRouter(SessionState session, PrintWriter out, Scanner scanner,
            EmailSender emailSender, EmailReader emailReader,
            EmailSearcher emailSearcher, EmailLister emailLister) {
        this.session = session;
        this.out = out;
        this.emailSender = emailSender;
        this.emailReader = emailReader;
        this.emailSearcher = emailSearcher;
        this.emailLister = emailLister;
    }

    public void handle(String input) {
        if (input == null || input.isBlank()) {
            return;
        }

        String[] tokens = input.trim().split(" ", 2);
        String command = tokens[0].toLowerCase();
        String args = tokens.length > 1 ? tokens[1] : "";

        switch (command) {
            case "1", "logout" -> out.println(CommandFormatter.logout(session.getEmail()));

            case "2", "send" -> emailSender.sendInteractive(session.getEmail());

            case "3", "list" -> emailLister.listEmails(session.getEmail(), args);

            case "4", "read" -> emailReader.readByIndex(args);

            case "5", "search" -> emailSearcher.search(session.getEmail(), args);

            case "6", "exit" -> {
                out.println(CommandFormatter.exit(session.getEmail()));
                ConsolePrinter.progressDots(ConsoleConstants.GOODBYE, 3, 300);
                session.reset();
            }

            case "help" -> ConsolePrinter.raw(ConsoleConstants.POST_LOGIN_COMMANDS);

            default -> ConsolePrinter.error("Unknown command. Type 'help' to see available options.");
        }
    }
}
