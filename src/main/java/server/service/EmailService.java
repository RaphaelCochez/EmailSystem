package server.service;

import lombok.extern.slf4j.Slf4j;
import model.Email;
import server.data.FileDatabase;
import utils.LogHandler;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for handling email-related logic.
 * Supports composing, retrieving, searching, and reading emails.
 * Interacts with FileDatabase for persistence and enforces security checks.
 */
@Slf4j
public class EmailService {

    private final FileDatabase database;

    public EmailService(FileDatabase database) {
        this.database = database;
    }

    /**
     * Persists a new email to the database.
     * 
     * @param email the email object to be saved
     * @return true if saved successfully, false if validation failed
     */
    public boolean sendEmail(Email email) {
        if (email.getTo() == null || email.getFrom() == null ||
                email.getSubject() == null || email.getBody() == null) {
            log.warn("SEND_EMAIL validation failed: missing required fields");
            LogHandler.warn("SEND_EMAIL validation failed: missing required fields");
            return false;
        }

        if (!database.userExists(email.getTo())) {
            log.warn("SEND_EMAIL failed: recipient {} does not exist", email.getTo());
            LogHandler.warn("SEND_EMAIL failed: recipient does not exist: " + email.getTo());
            return false;
        }

        if (email.getId() == null || email.getId().isBlank()) {
            email.setId(UUID.randomUUID().toString());
        }

        boolean success = database.saveEmail(email);
        if (success) {
            log.info("Email sent from {} to {} with subject '{}'", email.getFrom(), email.getTo(), email.getSubject());
            LogHandler.info("Email sent: from=" + email.getFrom() + ", to=" + email.getTo());
        } else {
            log.error("Failed to save email to database for from={} to={}", email.getFrom(), email.getTo());
            LogHandler.error("Failed to save email: " + email.getId());
        }
        return success;
    }

    /**
     * Retrieves all emails received by a given user.
     * 
     * @param email recipient's email address
     * @return list of received emails
     */
    public List<Email> getReceivedEmails(String email) {
        log.debug("Fetching received emails for {}", email);
        return database.getEmailsForUser(email.toLowerCase(), false);
    }

    /**
     * Retrieves all emails sent by a given user.
     * 
     * @param email sender's email address
     * @return list of sent emails
     */
    public List<Email> getSentEmails(String email) {
        log.debug("Fetching sent emails for {}", email);
        return database.getEmailsForUser(email.toLowerCase(), true);
    }

    /**
     * Searches user's emails (sent or received) for a keyword match.
     * 
     * @param email   target user's email
     * @param type    "sent" or "received"
     * @param keyword the search keyword
     * @return list of matching emails
     */
    public List<Email> searchEmails(String email, String type, String keyword) {
        log.debug("Searching emails for {} (type={} keyword={})", email, type, keyword);
        return database.searchEmails(email.toLowerCase(), "sent".equalsIgnoreCase(type), keyword);
    }

    /**
     * Retrieves an email by ID and checks if user has permission.
     * 
     * @param userEmail user performing the request
     * @param emailId   ID of the email to retrieve
     * @return email if accessible, else null
     */
    public Email getEmailById(String userEmail, String emailId) {
        Email email = database.getEmailById(emailId);
        if (email == null) {
            log.warn("READ_EMAIL failed: email ID not found: {}", emailId);
            LogHandler.warn("READ_EMAIL failed: not found - ID: " + emailId);
            return null;
        }
        if (!email.getTo().equalsIgnoreCase(userEmail) && !email.getFrom().equalsIgnoreCase(userEmail)) {
            log.warn("READ_EMAIL denied for user {} on email ID {}", userEmail, emailId);
            LogHandler.warn("READ_EMAIL denied: " + userEmail + " cannot access " + emailId);
            return null;
        }
        log.info("Email ID {} retrieved by {}", emailId, userEmail);
        LogHandler.info("READ_EMAIL success: " + emailId);
        return email;
    }
}
