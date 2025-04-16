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
 * FileDatabase provides thread-safe persistence for users and emails.
 */
public class FileDatabase {

    private final String usersPath;
    private final String emailsPath;
    private final Object userLock = new Object();
    private final Object emailLock = new Object();
    private final Gson gson = new GsonBuilder().create();

    /**
     * Constructor for FileDatabase.
     *
     * @param usersPath  path to users.db file
     * @param emailsPath path to emails.db file
     */
    public FileDatabase(String usersPath, String emailsPath) {
        this.usersPath = usersPath;
        this.emailsPath = emailsPath;
        ensureFileExists(usersPath);
        ensureFileExists(emailsPath);
    }

    private void ensureFileExists(String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean saveUser(User user) {
        synchronized (userLock) {
            if (getUser(user.getEmail()) != null) {
                return false;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersPath, true))) {
                writer.write(gson.toJson(user));
                writer.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public User getUser(String email) {
        synchronized (userLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usersPath))) {
                return reader.lines()
                        .filter(line -> !line.trim().isEmpty()) // Skip empty lines
                        .map(line -> {
                            try {
                                return gson.fromJson(line, User.class);
                            } catch (JsonSyntaxException e) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .filter(user -> user.getEmail().equalsIgnoreCase(email))
                        .findFirst()
                        .orElse(null);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public boolean saveEmail(Email email) {
        synchronized (emailLock) {
            if (email.getId() == null || email.getId().isEmpty()) {
                email.setId(UUID.randomUUID().toString());
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(emailsPath, true))) {
                writer.write(gson.toJson(email));
                writer.newLine();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public List<Email> loadAllEmails() {
        synchronized (emailLock) {
            List<Email> emails = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(emailsPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue;
                    try {
                        emails.add(gson.fromJson(line, Email.class));
                    } catch (JsonSyntaxException ignored) {
                        // Skip invalid lines
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return emails;
        }
    }

    public Email getEmailById(String id) {
        return loadAllEmails().stream()
                .filter(email -> email.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<Email> getEmailsForUser(String email, String type) {
        return loadAllEmails().stream()
                .filter(e -> type.equalsIgnoreCase("sent")
                        ? e.getFrom().equalsIgnoreCase(email)
                        : e.getTo().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }
}
