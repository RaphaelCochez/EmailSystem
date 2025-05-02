package server.service;

import model.Email;
import server.data.FileDatabase;
import utills.LogHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * EmailService handles all email-related operations including
 * sending, retrieving, searching, and reading emails.
 */
public class EmailService {

    private final FileDatabase fileDatabase;

    public EmailService(FileDatabase fileDatabase) {
        this.fileDatabase = fileDatabase;
    }

    /**
     * Sends an email if recipient exists and all required fields are valid.
     *
     * @param email the Email object to send
     * @return true if sent successfully, false otherwise
     */
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

        boolean success = fileDatabase.saveEmail(email);
        if (success) {
            LogHandler.log(
                    "Email sent from " + email.getFrom() + " to " + email.getTo() + " [ID: " + email.getId() + "]");
        } else {
            LogHandler.log("Failed to save email to file database.");
        }

        return success;
    }

    /**
     * Retrieves emails received by a user.
     */
    public List<Email> getReceivedEmails(String userEmail) {
        String normalized = userEmail.toLowerCase(Locale.ROOT);
        return fileDatabase.getEmailsForUser(normalized, "received");
    }

    /**
     * Retrieves emails sent by a user.
     */
    public List<Email> getSentEmails(String userEmail) {
        String normalized = userEmail.toLowerCase(Locale.ROOT);
        return fileDatabase.getEmailsForUser(normalized, "sent");
    }

    /**
     * Searches either received or sent emails by keyword.
     */
    public List<Email> searchEmails(String userEmail, String type, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            LogHandler.log("Email search aborted: Empty keyword.");
            return new ArrayList<>();
        }

        List<Email> base = fileDatabase.getEmailsForUser(userEmail.toLowerCase(Locale.ROOT), type);
        if (base == null) {
            LogHandler.log("Email search failed: Invalid user or email type - " + userEmail + " / " + type);
            return new ArrayList<>();
        }

        final String keywordLower = keyword.toLowerCase(Locale.ROOT);
        return base.stream()
                .filter(email -> email.getSubject().toLowerCase(Locale.ROOT).contains(keywordLower)
                        || ("received".equalsIgnoreCase(type)
                                && email.getFrom().toLowerCase(Locale.ROOT).contains(keywordLower))
                        || ("sent".equalsIgnoreCase(type)
                                && email.getTo().toLowerCase(Locale.ROOT).contains(keywordLower)))
                .collect(Collectors.toList());
    }

    /**
     * Returns a specific email by ID if it belongs to the requesting user.
     */
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
        return null; // Access denied
    }
}
