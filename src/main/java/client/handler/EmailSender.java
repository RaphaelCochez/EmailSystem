package client.handler;

import com.google.gson.JsonObject;
import utils.ConsolePrinter;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;

/**
 * Handles the interactive sending of emails.
 */
public class EmailSender {

    private final PrintWriter out;
    private final Scanner scanner;

    public EmailSender(PrintWriter out, Scanner scanner) {
        this.out = out;
        this.scanner = scanner;
    }

    public void sendInteractive(String fromEmail) {
        ConsolePrinter.prompt("Enter the recipient email (to): ");
        String to = scanner.nextLine().trim();

        ConsolePrinter.prompt("Enter the subject: ");
        String subject = scanner.nextLine().trim();

        ConsolePrinter.prompt("Enter the body of the email: ");
        String body = scanner.nextLine().trim();

        String id = UUID.randomUUID().toString();
        String timestamp = getCurrentTimestamp();

        JsonObject emailPayload = new JsonObject();
        emailPayload.addProperty("id", id);
        emailPayload.addProperty("to", to);
        emailPayload.addProperty("from", fromEmail);
        emailPayload.addProperty("subject", subject);
        emailPayload.addProperty("body", body);
        emailPayload.addProperty("timestamp", timestamp);
        emailPayload.addProperty("visible", true);
        emailPayload.addProperty("edited", false);

        out.println("SEND_EMAIL%%" + emailPayload.toString());
        ConsolePrinter.success("Email staged successfully to " + to);
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }
}
