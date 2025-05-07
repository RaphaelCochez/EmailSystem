package server.service;

import model.Email;
import server.data.FileDatabase;
import utils.LogHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class EmailService {

    private final FileDatabase fileDatabase;

    public EmailService(FileDatabase fileDatabase) {
        this.fileDatabase = fileDatabase;
    }

    public boolean sendEmail(Email email) {
        if (email == null ||
                email.getTo() == null ||
                email.getFrom() == null ||
                email.getSubject() == null ||
                email.getBody() == null ||
                email.getTimestamp() == null) {
            LogHandler.log("Failed to send email: Missing required fields.");
            return false;
        }

        if (fileDatabase.getUser(email.getTo()) == null) {
            LogHandler.log("Failed to send email: Recipient not found - " + email.getTo());
            return false;
        }

        if (email.getId() == null || email.getId().isEmpty()) {
            email.setId(UUID.randomUUID().toString());
        }

        fileDatabase.addEmail(email); // renamed from saveEmail to match FileDatabase
        LogHandler.log("Email sent from " + email.getFrom() + " to " + email.getTo() + " [ID: " + email.getId() + "]");
        return true;
    }

    public List<Email> getReceivedEmails(String userEmail) {
        String normalized = userEmail.toLowerCase(Locale.ROOT);
        return fileDatabase.getEmailsForUser(normalized, false); // false = inbox
    }

    public List<Email> getSentEmails(String userEmail) {
        String normalized = userEmail.toLowerCase(Locale.ROOT);
        return fileDatabase.getEmailsForUser(normalized, true); // true = sent
    }

    public List<Email> searchEmails(String userEmail, String type, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            LogHandler.log("Email search aborted: Empty keyword.");
            return new ArrayList<>();
        }

        boolean isSent = "sent".equalsIgnoreCase(type);
        List<Email> base = fileDatabase.getEmailsForUser(userEmail.toLowerCase(Locale.ROOT), isSent);

        if (base == null) {
            LogHandler.log("Email search failed: Invalid user or email type - " + userEmail + " / " + type);
            return new ArrayList<>();
        }

        final String keywordLower = keyword.toLowerCase(Locale.ROOT);
        return base.stream()
                .filter(email -> email.getSubject().toLowerCase(Locale.ROOT).contains(keywordLower)
                        || (!isSent && email.getFrom().toLowerCase(Locale.ROOT).contains(keywordLower))
                        || (isSent && email.getTo().toLowerCase(Locale.ROOT).contains(keywordLower)))
                .collect(Collectors.toList());
    }

    public Email getEmailById(String userEmail, String id) {
        Email email = fileDatabase.getEmailById(id);
        if (email == null) {
            LogHandler.log("Email read failed: Email ID not found - " + id);
            return null;
        }

        if (userEmail.equalsIgnoreCase(email.getTo()) || userEmail.equalsIgnoreCase(email.getFrom())) {
            return email;
        }

        LogHandler.log("Email read blocked: Access denied to user " + userEmail + " for email ID " + id);
        return null;
    }
}
