package server.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import model.Email;
import model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
            System.err.println("Failed to initialize database files: " + e.getMessage());
        }
    }

    private void ensureFileExists(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    public void saveAll() {
        synchronized (userLock) {
            try (BufferedWriter writer = Files.newBufferedWriter(usersPath)) {
                for (User user : userMap.values()) {
                    writer.write(gson.toJson(user));
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Failed to save users: " + e.getMessage());
            }
        }

        synchronized (emailLock) {
            try (BufferedWriter writer = Files.newBufferedWriter(emailsPath)) {
                for (Email email : emailList) {
                    writer.write(gson.toJson(email));
                    writer.newLine();
                }
            } catch (IOException e) {
                System.err.println("Failed to save emails: " + e.getMessage());
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
                        System.err.println("Skipped malformed user entry.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to load users: " + e.getMessage());
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
                        System.err.println("Skipped malformed email entry.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to load emails: " + e.getMessage());
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
                boolean matchesSenderRecipient = (sent && e.getFrom().equalsIgnoreCase(email)) ||
                        (!sent && e.getTo().equalsIgnoreCase(email));
                boolean matchesContent = e.getSubject().toLowerCase().contains(keyword.toLowerCase()) ||
                        e.getBody().toLowerCase().contains(keyword.toLowerCase());
                if (matchesSenderRecipient && matchesContent) {
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
            return userMap.containsKey(email); // Check if the user exists in the map
        }
    }

    public void addEmail(Email email) {
        synchronized (emailLock) {
            emailList.add(email); // Add the email to the list
        }
    }

}
