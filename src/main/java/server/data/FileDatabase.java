package server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import model.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FileDatabase handles in-memory storage and persistent I/O for user and email
 * data.
 * <p>
 * Thread-safe using explicit locking on user and email maps.
 * Each entry is stored as a JSON line in a flat file database (Line-Delimited
 * JSON).
 * This implementation ensures data consistency across server restarts and
 * handles concurrent access.
 */
@Slf4j
public class FileDatabase {

    private final Path usersPath;
    private final Path emailsPath;

    private final Map<String, User> userMap = new ConcurrentHashMap<>();
    private final List<Email> emailList = new CopyOnWriteArrayList<>();

    private final Gson gson = new GsonBuilder().create();

    private final Object userLock = new Object();
    private final Object emailLock = new Object();

    public FileDatabase(String usersFilePath, String emailsFilePath) {
        this.usersPath = Paths.get(usersFilePath);
        this.emailsPath = Paths.get(emailsFilePath);

        try {
            ensureFileExists(usersPath);
            ensureFileExists(emailsPath);
        } catch (IOException e) {
            log.error("Failed to initialize database files: {}", e.getMessage());
        }
    }

    private void ensureFileExists(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * Saves all users and emails to disk using atomic file replacement.
     * This approach prevents partial writes and ensures file integrity.
     */
    public void saveAll() {
        synchronized (userLock) {
            Path tempUserPath = usersPath.resolveSibling("." + usersPath.getFileName() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempUserPath)) {
                for (User user : userMap.values()) {
                    writer.write(gson.toJson(user));
                    writer.newLine();
                }
                Files.move(tempUserPath, usersPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
                log.info("User data saved successfully.");
            } catch (IOException e) {
                log.error("Failed to save users: {}", e.getMessage());
            }
        }

        synchronized (emailLock) {
            Path tempEmailPath = emailsPath.resolveSibling("." + emailsPath.getFileName() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tempEmailPath)) {
                for (Email email : emailList) {
                    writer.write(gson.toJson(email));
                    writer.newLine();
                }
                Files.move(tempEmailPath, emailsPath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
                log.info("Email data saved successfully.");
            } catch (IOException e) {
                log.error("Failed to save emails: {}", e.getMessage());
            }
        }
    }

    public void loadAll() {
        synchronized (userLock) {
            try (BufferedReader reader = Files.newBufferedReader(usersPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        User user = gson.fromJson(line, User.class);
                        if (user != null && user.getEmail() != null) {
                            userMap.put(user.getEmail(), user);
                        }
                    } catch (JsonSyntaxException e) {
                        log.warn("Skipped malformed user entry.");
                    }
                }
            } catch (IOException e) {
                log.error("Failed to load users: {}", e.getMessage());
            }
        }

        synchronized (emailLock) {
            try (BufferedReader reader = Files.newBufferedReader(emailsPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        Email email = gson.fromJson(line, Email.class);
                        if (email != null) {
                            emailList.add(email);
                        }
                    } catch (JsonSyntaxException e) {
                        log.warn("Skipped malformed email entry.");
                    }
                }
            } catch (IOException e) {
                log.error("Failed to load emails: {}", e.getMessage());
            }
        }
    }

    public boolean saveUser(User user) {
        synchronized (userLock) {
            if (userMap.containsKey(user.getEmail())) {
                return false;
            }
            userMap.put(user.getEmail(), user);
            return true;
        }
    }

    public User getUser(String email) {
        synchronized (userLock) {
            return userMap.get(email);
        }
    }

    public boolean saveEmail(Email email) {
        synchronized (emailLock) {
            emailList.add(email);
            return true;
        }
    }

    public List<Email> getEmailsForUser(String email, boolean sent) {
        synchronized (emailLock) {
            List<Email> result = new ArrayList<>();
            for (Email e : emailList) {
                if (sent && e.getFrom().equalsIgnoreCase(email)) {
                    result.add(e);
                } else if (!sent && e.getTo().equalsIgnoreCase(email)) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    public Email getEmailById(String emailId) {
        synchronized (emailLock) {
            for (Email email : emailList) {
                if (email.getId().equals(emailId)) {
                    return email;
                }
            }
            return null;
        }
    }

    public List<Email> searchEmails(String email, boolean sent, String keyword) {
        synchronized (emailLock) {
            List<Email> result = new ArrayList<>();
            for (Email e : emailList) {
                boolean matchesUser = sent
                        ? e.getFrom().equalsIgnoreCase(email)
                        : e.getTo().equalsIgnoreCase(email);

                boolean matchesKeyword = e.getSubject().toLowerCase().contains(keyword.toLowerCase())
                        || e.getBody().toLowerCase().contains(keyword.toLowerCase());

                if (matchesUser && matchesKeyword) {
                    result.add(e);
                }
            }
            return result;
        }
    }

    public int getUserCount() {
        synchronized (userLock) {
            return userMap.size();
        }
    }

    public int getEmailCount() {
        synchronized (emailLock) {
            return emailList.size();
        }
    }

    public boolean userExists(String email) {
        synchronized (userLock) {
            return userMap.containsKey(email);
        }
    }

    public void addEmail(Email email) {
        synchronized (emailLock) {
            emailList.add(email);
        }
    }

    public List<Email> getReceivedEmails(String email) {
        return getEmailsForUser(email, false).stream()
                .filter(Email::isVisible)
                .toList();
    }

    public List<Email> getSentEmails(String email) {
        return getEmailsForUser(email, false).stream()
                .filter(Email::isVisible)
                .toList();
    }

}
