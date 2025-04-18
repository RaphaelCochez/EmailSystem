package server.service;

import model.Email;
import server.data.FileDatabase;

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
     * @return true if sent successfully, false if validation fails or recipient not
     *         found
     */
    public boolean sendEmail(Email email) {
        if (email == null ||
                email.getTo() == null ||
                email.getFrom() == null ||
                email.getSubject() == null ||
                email.getBody() == null ||
                email.getTimestamp() == null) {
            return false;
        }

        if (fileDatabase.getUser(email.getTo()) == null) {
            return false;
        }

        if (email.getId() == null || email.getId().isEmpty()) {
            email.setId(UUID.randomUUID().toString());
        }

        return fileDatabase.saveEmail(email);
    }

    /**
     * Retrieves emails received by a user.
     */
    public List<Email> getReceivedEmails(String userEmail) {
        return fileDatabase.getEmailsForUser(userEmail, "received");
    }

    /**
     * Retrieves emails sent by a user.
     */
    public List<Email> getSentEmails(String userEmail) {
        return fileDatabase.getEmailsForUser(userEmail, "sent");
    }

    /**
     * Searches either received or sent emails by keyword.
     */
    public List<Email> searchEmails(String userEmail, String type, String keyword) {
        List<Email> base = fileDatabase.getEmailsForUser(userEmail, type);
        if (base == null)
            return new ArrayList<>();

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
        if (email == null)
            return null;

        if (userEmail.equalsIgnoreCase(email.getTo()) || userEmail.equalsIgnoreCase(email.getFrom())) {
            return email;
        }

        return null; // Access denied
    }
}
