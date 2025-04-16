package server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import model.Email;
import model.User;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileDatabase provides thread-safe file-based persistence for user and email
 * data.
 * Data is stored in JSON-line format in flat files under /resources.
 */
public class FileDatabase {

    private static final String USERS_PATH = "src/main/resources/users.db";
    private static final String EMAILS_PATH = "src/main/resources/emails.db";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Object userLock = new Object();
    private final Object emailLock = new Object();

    // ------------------------- USER METHODS -------------------------

    /**
     * Saves a new user to users.db if the email is not already taken.
     *
     * @param user the user to save
     * @return true if saved successfully; false if user already exists or I/O error
     *         occurs
     */
    public boolean saveUser(final User user) {
        synchronized (userLock) {
            if (getUser(user.getEmail()) != null)
                return false;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_PATH, true))) {
                writer.write(gson.toJson(user));
                writer.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Retrieves a user from users.db by email.
     *
     * @param email the email to search for
     * @return the User if found; null otherwise
     */
    public User getUser(final String email) {
        synchronized (userLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(USERS_PATH))) {
                return reader.lines()
                        .map(line -> gson.fromJson(line, User.class))
                        .filter(user -> user.getEmail().equalsIgnoreCase(email))
                        .findFirst()
                        .orElse(null);
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    // ------------------------ EMAIL METHODS -------------------------

    /**
     * Saves an email to emails.db. Generates a unique ID if missing.
     *
     * @param email the email to save
     * @return true if written successfully; false on error
     */
    public boolean saveEmail(final Email email) {
        synchronized (emailLock) {
            if (email.getId() == null || email.getId().isEmpty()) {
                email.setId(UUID.randomUUID().toString());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMAILS_PATH, true))) {
                writer.write(gson.toJson(email));
                writer.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Loads all emails from emails.db into memory.
     *
     * @return a List of Email objects
     */
    public List<Email> loadAllEmails() {
        synchronized (emailLock) {
            List<Email> emails = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(EMAILS_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    emails.add(gson.fromJson(line, Email.class));
                }
            } catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
            return emails;
        }
    }

    /**
     * Returns emails for a specific user by type (sent or received).
     *
     * @param email the user’s email address
     * @param type  "sent" or "received"
     * @return a list of matching Email objects
     */
    public List<Email> getEmailsForUser(final String email, final String type) {
        return loadAllEmails().stream()
                .filter(e -> "sent".equalsIgnoreCase(type)
                        ? e.getFrom().equalsIgnoreCase(email)
                        : e.getTo().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific email by its ID.
     *
     * @param id the unique ID of the email
     * @return the Email object if found; null otherwise
     */
    public Email getEmailById(final String id) {
        return loadAllEmails().stream()
                .filter(email -> email.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
