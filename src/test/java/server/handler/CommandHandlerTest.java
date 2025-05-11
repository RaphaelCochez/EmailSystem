package server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.Email;
import model.User;

import static utils.ProtocolConstants.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.data.FileDatabase;
import server.service.AuthService;
import server.service.EmailService;
import server.service.SessionManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest {

    private CommandHandler handler;
    private StringWriter writer;
    private PrintWriter out;
    private Socket dummySocket;
    private FakeDatabase db;
    private FakeSessionManager sessions;
    private EmailService emailService;
    private AuthService authService;
    private Gson gson;

    @BeforeEach
    void setUp() {
        writer = new StringWriter();
        out = new PrintWriter(writer, true);
        dummySocket = new Socket();
        db = new FakeDatabase();
        sessions = new FakeSessionManager();
        emailService = new EmailService(db);
        authService = new AuthService(db, sessions);
        handler = new CommandHandler(authService, emailService, sessions);
        gson = new Gson();
    }

    @Test
    void testUnknownCommand() {
        handler.handle("UNKNOWN%%{}", dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_UNKNOWN));
    }

    @Test
    void testInvalidFormat() {
        handler.handle("NO_DELIMITER_INPUT", dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_INVALID_FORMAT));
    }

    @Test
    void testSendEmailFailsIfNotLoggedIn() {
        db.saveUser(new User("recipient@example.com", "any"));

        Email email = new Email();
        email.setTo("recipient@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Subject");
        email.setBody("Body");
        email.setTimestamp("2025-04-18T14:00:00Z");
        email.setVisible(true);

        handler.handle("SEND_EMAIL%%" + gson.toJson(email), dummySocket, out);

        assertTrue(writer.toString().contains(RESPONSE_UNAUTHORIZED));
    }

    @Test
    void testSendEmailSuccess() {
        db.saveUser(new User("sender@example.com", "any"));
        db.saveUser(new User("recipient@example.com", "any"));
        sessions.startSession("sender@example.com", dummySocket);

        Email email = new Email();
        email.setTo("recipient@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Subject");
        email.setBody("Body");
        email.setTimestamp("2025-04-18T14:00:00Z");
        email.setVisible(true);

        handler.handle("SEND_EMAIL%%" + gson.toJson(email), dummySocket, out);

        assertTrue(writer.toString().contains(RESPONSE_SEND_EMAIL_SUCCESS));
    }

    @Test
    void testReadEmailUnauthorized() {
        db.saveUser(new User("alice@example.com", "pw"));
        db.saveUser(new User("bob@example.com", "pw"));
        db.saveUser(new User("intruder@example.com", "pw"));

        Email email = new Email();
        email.setId("id123");
        email.setTo("bob@example.com");
        email.setFrom("alice@example.com");
        email.setSubject("Top Secret");
        email.setBody("Don't read this");
        email.setTimestamp("2025-04-18T14:00:00Z");
        email.setVisible(true);

        db.saveEmail(email);

        String json = "{\"email\":\"intruder@example.com\",\"id\":\"id123\"}";
        handler.handle("READ_EMAIL%%" + json, dummySocket, out);

        assertTrue(writer.toString().contains(RESPONSE_UNAUTHORIZED));
    }

    @Test
    void testRegisterSuccess() {
        User user = new User("newuser@example.com", "password123");
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_REGISTER_SUCCESS));
    }

    @Test
    void testRegisterDuplicate() {
        db.saveUser(new User("newuser@example.com", "existing$hash"));
        User user = new User("newuser@example.com", "password123");
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_REGISTER_FAIL));
    }

    @Test
    void testLoginSuccess() {
        String email = "loginuser@example.com";
        String password = "password123";

        User user = new User(email, password);
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        writer.getBuffer().setLength(0);

        handler.handle("LOGIN%%" + gson.toJson(user), dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_LOGIN_SUCCESS));
        assertTrue(sessions.isLoggedIn(email));
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User("wrongpass@example.com", "correctpass");
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        writer.getBuffer().setLength(0);

        User wrong = new User("wrongpass@example.com", "wrongpass");
        handler.handle("LOGIN%%" + gson.toJson(wrong), dummySocket, out);
        assertTrue(writer.toString().contains(RESPONSE_LOGIN_FAIL));
        assertTrue(writer.toString().contains("Invalid credentials"));
    }

    @Test
    void testLogoutSuccess() {
        User user = new User("logoutuser@example.com", "password123");
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        handler.handle("LOGIN%%" + gson.toJson(user), dummySocket, out);
        writer.getBuffer().setLength(0);

        String logoutJson = "{\"email\":\"logoutuser@example.com\"}";
        handler.handle("LOGOUT%%" + logoutJson, dummySocket, out);

        assertTrue(writer.toString().contains(RESPONSE_LOGOUT_SUCCESS));
        assertFalse(sessions.isLoggedIn("logoutuser@example.com"));
    }

    @Test
    void testRetrieveReceivedEmailsSuccess() {
        String email = "bob@example.com";
        db.saveUser(new User(email, "pw")); // Ensure user exists
        sessions.startSession(email, dummySocket); // Start session for user

        Email emailObj = new Email();
        emailObj.setId("msg123");
        emailObj.setTo(email); // Means it's a received email
        emailObj.setFrom("alice@example.com");
        emailObj.setSubject("Welcome");
        emailObj.setBody("Hello Bob!");
        emailObj.setTimestamp("2025-05-11T14:00:00Z");
        emailObj.setVisible(true);
        db.saveEmail(emailObj); // Store in mock database

        // Corrected: check received (false = received)
        assertEquals(1, db.getEmailsForUser(email, false).size());

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("type", "received");

        writer.getBuffer().setLength(0); // Clear previous output
        handler.handle("RETRIEVE_EMAILS%%" + payload.toString(), dummySocket, out);

        System.out.println("RESPONSE: " + writer); // Optional debug

        assertTrue(writer.toString().contains(RESPONSE_RETRIEVE_EMAILS_SUCCESS));
        assertTrue(writer.toString().contains("Welcome"));
    }

    @Test
    void testSearchEmailsReturnsMatch() {
        String email = "alice@example.com";
        sessions.startSession(email, dummySocket);
        db.saveUser(new User(email, "pw"));

        Email emailObj = new Email();
        emailObj.setId("search001");
        emailObj.setTo("bob@example.com");
        emailObj.setFrom(email);
        emailObj.setSubject("Project Alpha");
        emailObj.setBody("Keyword: update");
        emailObj.setTimestamp("2025-05-11T15:00:00Z");
        emailObj.setVisible(true);
        db.saveEmail(emailObj);

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email);
        payload.addProperty("type", "sent");
        payload.addProperty("keyword", "update");

        writer.getBuffer().setLength(0);
        handler.handle("SEARCH_EMAIL%%" + payload.toString(), dummySocket, out);

        assertTrue(writer.toString().contains(RESPONSE_SEARCH_EMAIL_SUCCESS));
        assertTrue(writer.toString().contains("Project Alpha"));
    }

    // === Fake Test Utilities ===

    static class FakeDatabase extends FileDatabase {
        private final Map<String, User> users = new HashMap<>();
        private final List<Email> emails = new ArrayList<>();

        public FakeDatabase() {
            super("src/test/resources/test_users.db", "src/test/resources/test_emails.db");
        }

        @Override
        public boolean saveUser(User user) {
            String key = user.getEmail().toLowerCase();
            if (users.containsKey(key))
                return false;
            users.put(key, user);
            return true;
        }

        @Override
        public boolean userExists(String email) {
            return users.containsKey(email.toLowerCase());
        }

        @Override
        public User getUser(String email) {
            return users.get(email.toLowerCase());
        }

        @Override
        public boolean saveEmail(Email email) {
            emails.add(email);
            return true;
        }

        @Override
        public Email getEmailById(String id) {
            return emails.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Email> getEmailsForUser(String email, boolean received) {
            return emails.stream()
                    .filter(e -> received
                            ? e.getFrom().equalsIgnoreCase(email) // Sent emails
                            : e.getTo().equalsIgnoreCase(email)) // Received emails
                    .filter(Email::isVisible)
                    .toList();
        }

        @Override
        public List<Email> getReceivedEmails(String email) {
            return getEmailsForUser(email, false); // false = received
        }

        @Override
        public List<Email> getSentEmails(String email) {
            return getEmailsForUser(email, true); // true = sent
        }

        @Override
        public List<Email> searchEmails(String email, boolean sent, String keyword) {
            String lower = keyword.toLowerCase();
            return emails.stream()
                    .filter(e -> (sent
                            ? e.getFrom().equalsIgnoreCase(email)
                            : e.getTo().equalsIgnoreCase(email)))
                    .filter(e -> e.getSubject().toLowerCase().contains(lower)
                            || e.getBody().toLowerCase().contains(lower))
                    .filter(Email::isVisible)
                    .toList();
        }
    }

    static class FakeSessionManager extends SessionManager {
        private final Map<String, Socket> sessionMap = new HashMap<>();

        @Override
        public void startSession(String email, Socket socket) {
            sessionMap.put(email, socket);
        }

        @Override
        public void endSession(String email) {
            sessionMap.remove(email);
        }

        @Override
        public boolean isLoggedIn(String email) {
            return sessionMap.containsKey(email);
        }

        @Override
        public Socket getSessionSocket(String email) {
            return sessionMap.get(email);
        }
    }
}
