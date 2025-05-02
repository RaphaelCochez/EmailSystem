package server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Email;
import model.User;
import utils.LogHandler;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileDatabase manages loading and saving of user/email data at
 * startup/shutdown only.
 * During runtime, all state is stored and manipulated in memory.
 */
public class FileDatabase {

    private final String usersPath;
    private final String emailsPath;
    private final Object userLock = new Object();
    private final Object emailLock = new Object();
    private final Gson gson = new GsonBuilder().create();

    private final Map<String, User> userMap = new HashMap<>();
    private final List<Email> emailList = new ArrayList<>();

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
            LogHandler.log("Error ensuring file exists: " + e.getMessage());
        }
    }

    /** Load all user and email data into memory (startup only). */
    public void loadAll() {
        synchronized (userLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(usersPath))) {
                reader.lines()
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> gson.fromJson(line, User.class))
                        .filter(Objects::nonNull)
                        .forEach(user -> userMap.put(user.getEmail().toLowerCase(), user));
            } catch (IOException e) {
                LogHandler.log("Error loading users: " + e.getMessage());
            }
        }

        synchronized (emailLock) {
            try (BufferedReader reader = new BufferedReader(new FileReader(emailsPath))) {
                reader.lines()
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> gson.fromJson(line, Email.class))
                        .filter(Objects::nonNull)
                        .forEach(emailList::add);
            } catch (IOException e) {
                LogHandler.log("Error loading emails: " + e.getMessage());
            }
        }
    }

    /** Persist all user and email data to disk (shutdown only). */
    public void saveAll() {
        synchronized (userLock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersPath, false))) {
                for (User user : userMap.values()) {
                    writer.write(gson.toJson(user));
                    writer.newLine();
                }
            } catch (IOException e) {
                LogHandler.log("Error saving users: " + e.getMessage());
            }
        }

        synchronized (emailLock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(emailsPath, false))) {
                for (Email email : emailList) {
                    writer.write(gson.toJson(email));
                    writer.newLine();
                }
            } catch (IOException e) {
                LogHandler.log("Error saving emails: " + e.getMessage());
            }
        }
    }

    public boolean saveUser(User user) {
        synchronized (userLock) {
            String key = user.getEmail().toLowerCase();
            if (userMap.containsKey(key)) {
                return false;
            }
            userMap.put(key, user);
            return true;
        }
    }

    public User getUser(String email) {
        synchronized (userLock) {
            return userMap.get(email.toLowerCase());
        }
    }

    public boolean saveEmail(Email email) {
        synchronized (emailLock) {
            if (email.getId() == null || email.getId().isEmpty()) {
                email.setId(UUID.randomUUID().toString());
            }
            emailList.add(email);
            return true;
        }
    }

    public Email getEmailById(String id) {
        synchronized (emailLock) {
            return emailList.stream()
                    .filter(email -> email.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<Email> getEmailsForUser(String email, String type) {
        synchronized (emailLock) {
            return emailList.stream()
                    .filter(e -> "sent".equalsIgnoreCase(type)
                            ? e.getFrom().equalsIgnoreCase(email)
                            : e.getTo().equalsIgnoreCase(email))
                    .collect(Collectors.toList());
        }
    }
}
