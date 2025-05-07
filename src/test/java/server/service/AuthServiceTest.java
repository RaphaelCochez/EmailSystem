package server.service;

import com.google.gson.Gson;

import model.Email;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.data.FileDatabase;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "Secret123";
    private static final String WRONG_PASSWORD = "WrongPassword";

    private AuthService authService;
    private FakeDatabase fakeDatabase;
    private FakeSessionManager fakeSessionManager;
    private StringWriter responseWriter;
    private PrintWriter printWriter;
    private Gson gson;

    @BeforeEach
    void setUp() {
        fakeDatabase = new FakeDatabase();
        fakeSessionManager = new FakeSessionManager();
        authService = new AuthService(fakeDatabase, fakeSessionManager);

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter, true);
        gson = new Gson();
    }

    @Test
    void testHandleRegisterSuccess() {
        User newUser = new User(EMAIL, PASSWORD);
        authService.handleRegister(gson.toJson(newUser), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals("REGISTER_SUCCESS", output);
        assertNotNull(fakeDatabase.getUser(EMAIL));
    }

    @Test
    void testHandleRegisterDuplicateUser() {
        User existing = new User(EMAIL, "existing$hash");
        fakeDatabase.saveUser(existing);

        User newUser = new User(EMAIL, PASSWORD);
        authService.handleRegister(gson.toJson(newUser), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith("REGISTER_FAIL"));
    }

    @Test
    void testHandleRegisterMissingFields() {
        String payload = "{\"email\": \"incomplete@example.com\"}";
        authService.handleRegister(payload, printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith("REGISTER_FAIL"));
        assertTrue(output.contains("Missing required"));
    }

    @Test
    void testHandleRegisterInvalidJson() {
        authService.handleRegister("not_json", printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith("REGISTER_FAIL"));
        assertTrue(output.contains("Invalid JSON"));
    }

    @Test
    void testHandleLoginSuccess() {
        String email = "login@example.com";
        String rawPassword = "Secret123";

        User registrationUser = new User(email, rawPassword);
        authService.handleRegister(gson.toJson(registrationUser), printWriter);
        responseWriter.getBuffer().setLength(0);

        User loginUser = new User(email, rawPassword);
        authService.handleLogin(gson.toJson(loginUser), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals("LOGIN_SUCCESS", output);
        assertTrue(fakeSessionManager.isLoggedIn(email));
    }

    @Test
    void testHandleLoginInvalidPassword() {
        User user = new User("badpass@example.com", PASSWORD);
        authService.handleRegister(gson.toJson(user), printWriter);
        responseWriter.getBuffer().setLength(0);

        User wrong = new User("badpass@example.com", WRONG_PASSWORD);
        authService.handleLogin(gson.toJson(wrong), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith("LOGIN_FAIL"));
        assertTrue(output.contains("Invalid credentials"));
    }

    @Test
    void testHandleLoginNonexistentUser() {
        User ghost = new User("ghost@example.com", "boo");
        authService.handleLogin(gson.toJson(ghost), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith("LOGIN_FAIL"));
        assertTrue(output.contains("User not found"));
    }

    @Test
    void testHandleLogoutSuccess() {
        User user = new User("logout@example.com", PASSWORD);
        authService.handleRegister(gson.toJson(user), printWriter);
        authService.handleLogin(gson.toJson(user), new Socket(), printWriter);

        responseWriter.getBuffer().setLength(0);
        authService.handleLogout(gson.toJson(user), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals("LOGOUT_SUCCESS", output);
        assertFalse(fakeSessionManager.isLoggedIn(user.getEmail()));
    }

    @Test
    void testHandleLogoutNotLoggedIn() {
        User ghost = new User("ghost@example.com", "nope");
        authService.handleLogout(gson.toJson(ghost), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals("LOGOUT_SUCCESS", output); // <- Expect success regardless
    }

    // --- Fakes below ---

    // FakeDatabase class for testing purposes
    static class FakeDatabase extends FileDatabase {
        private final Map<String, User> users = new HashMap<>();
        private final List<Email> emails = new ArrayList<>();

        public FakeDatabase() {
            super("test_users.db", "test_emails.db");
        }

        // Override saveUser method
        @Override
        public boolean saveUser(User user) {
            if (users.containsKey(user.getEmail())) {
                return false;
            }
            users.put(user.getEmail(), user);
            return true;
        }

        // Override getUser method
        @Override
        public User getUser(String email) {
            return users.get(email);
        }

        // Override saveEmail method
        @Override
        public boolean saveEmail(Email email) {
            emails.add(email);
            return true;
        }

        // Override getEmailById method
        @Override
        public Email getEmailById(String id) {
            return emails.stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        }
    }

    class FakeSessionManager extends SessionManager {
        private final Map<String, Socket> sessions = new HashMap<>();

        @Override
        public void startSession(String email, Socket socket) {
            sessions.put(email, socket);
        }

        @Override
        public void endSession(String email) {
            sessions.remove(email);
        }

        @Override
        public boolean isLoggedIn(String email) {
            return sessions.containsKey(email);
        }

        @Override
        public Socket getSessionSocket(String email) {
            return sessions.get(email);
        }
    }
}
