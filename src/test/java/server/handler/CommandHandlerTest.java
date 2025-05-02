package server.handler;

import com.google.gson.Gson;
import model.Email;
import model.User;

import static server.protocol.ProtocolConstants.*;

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
        assertTrue(writer.toString().contains("UNKNOWN_COMMAND"));
    }

    @Test
    void testInvalidFormat() {
        handler.handle("NO_DELIMITER_INPUT", dummySocket, out);
        assertTrue(writer.toString().contains("INVALID_FORMAT"));
    }

    @Test
    void testSendEmailSuccess() {
        db.saveUser(new User("sender@example.com", "any"));
        db.saveUser(new User("recipient@example.com", "any"));
        sessions.startSession("sender@example.com", dummySocket); // required for authenticated command

        Email email = new Email();
        email.setId(UUID.randomUUID().toString());
        email.setTo("recipient@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Subject");
        email.setBody("Body");
        email.setTimestamp("2025-04-18T14:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        String json = gson.toJson(email);
        handler.handle("SEND_EMAIL%%" + json, dummySocket, out);

        assertTrue(writer.toString().contains("SEND_EMAIL_SUCCESS"));
    }

    @Test
    void testReadEmailUnauthorized() {
        db.saveUser(new User("alice@example.com", "pw"));
        db.saveUser(new User("bob@example.com", "pw"));
        db.saveUser(new User("intruder@example.com", "pw"));

        // NO session for intruder
        sessions.startSession("bob@example.com", dummySocket); // Optional: real recipient
        // sessions.startSession("intruder@example.com", dummySocket);

        Email email = new Email();
        email.setId("id123");
        email.setTo("bob@example.com");
        email.setFrom("alice@example.com");
        email.setSubject("Top Secret");
        email.setBody("Don't read this");
        email.setTimestamp("2025-04-18T14:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        db.saveEmail(email);

        String readRequest = "READ_EMAIL%%{" +
                "\"email\":\"intruder@example.com\",\"id\":\"id123\"}";

        handler.handle(readRequest, dummySocket, out);

        System.out.println("Response: " + writer.toString()); // just to check remove later if needed

        assertTrue(writer.toString().contains(RESP_UNAUTHORIZED));
    }

    @Test
    void testRegisterSuccess() {
        User user = new User("newuser@example.com", "password123");
        String json = gson.toJson(user);
        handler.handle("REGISTER%%" + json, dummySocket, out);
        assertTrue(writer.toString().contains("REGISTER_SUCCESS"));
    }

    @Test
    void testRegisterDuplicate() {
        db.saveUser(new User("newuser@example.com", "existing$hash"));
        User user = new User("newuser@example.com", "password123");
        String json = gson.toJson(user);
        handler.handle("REGISTER%%" + json, dummySocket, out);
        assertTrue(writer.toString().contains("REGISTER_FAIL"));
    }

    @Test
    void testLoginSuccess() {
        String email = "loginuser@example.com";
        String password = "password123";

        User user = new User(email, password);
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        writer.getBuffer().setLength(0);

        handler.handle("LOGIN%%" + gson.toJson(user), dummySocket, out);
        assertTrue(writer.toString().contains("LOGIN_SUCCESS"));
        assertTrue(sessions.isLoggedIn(email));
    }

    @Test
    void testLoginWrongPassword() {
        User user = new User("wrongpass@example.com", "correctpass");
        handler.handle("REGISTER%%" + gson.toJson(user), dummySocket, out);
        writer.getBuffer().setLength(0);

        User wrong = new User("wrongpass@example.com", "wrongpass");
        handler.handle("LOGIN%%" + gson.toJson(wrong), dummySocket, out);
        assertTrue(writer.toString().contains("LOGIN_FAIL"));
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

        assertTrue(writer.toString().contains("LOGOUT_SUCCESS"));
        assertFalse(sessions.isLoggedIn("logoutuser@example.com"));
    }

    // --- Fakes for testing ---

    static class FakeDatabase extends FileDatabase {
        private final Map<String, User> users = new HashMap<>();
        private final List<Email> emails = new ArrayList<>();

        public FakeDatabase() {
            super("test_users.db", "test_emails.db");
        }

        @Override
        public boolean saveUser(User user) {
            if (users.containsKey(user.getEmail()))
                return false;
            users.put(user.getEmail(), user);
            return true;
        }

        @Override
        public User getUser(String email) {
            return users.get(email);
        }

        @Override
        public boolean saveEmail(Email email) {
            emails.add(email);
            return true;
        }

        @Override
        public Email getEmailById(String id) {
            return emails.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Email> getEmailsForUser(String email, String type) {
            return emails.stream()
                    .filter(e -> "received".equalsIgnoreCase(type)
                            ? e.getTo().equalsIgnoreCase(email)
                            : e.getFrom().equalsIgnoreCase(email))
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
