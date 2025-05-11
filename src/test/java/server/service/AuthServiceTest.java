package server.service;

import com.google.gson.Gson;
import model.Email;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.data.FileDatabase;
import utils.ProtocolConstants;
import utils.SecurityUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.*;

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
        assertEquals(ProtocolConstants.RESPONSE_REGISTER_SUCCESS, output);
        assertNotNull(fakeDatabase.getUser(EMAIL));
    }

    @Test
    void testHandleRegisterDuplicateUser() {
        // Pre-hash manually to simulate stored user
        String salt = SecurityUtils.generateSalt();
        String hashed = SecurityUtils.hashPassword(PASSWORD, salt);
        fakeDatabase.saveUser(new User(EMAIL, salt + "$" + hashed));

        User newUser = new User(EMAIL, PASSWORD);
        authService.handleRegister(gson.toJson(newUser), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith(ProtocolConstants.RESPONSE_REGISTER_FAIL));
        assertTrue(output.contains("Email already registered"));
    }

    @Test
    void testHandleRegisterMissingFields() {
        String payload = "{\"email\": \"incomplete@example.com\"}";
        authService.handleRegister(payload, printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith(ProtocolConstants.RESPONSE_REGISTER_FAIL));
        assertTrue(output.contains("Missing required"));
    }

    @Test
    void testHandleRegisterInvalidJson() {
        authService.handleRegister("not_json", printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith(ProtocolConstants.RESPONSE_REGISTER_FAIL));
        assertTrue(output.contains("Invalid JSON"));
    }

    @Test
    void testHandleLoginSuccess() {
        String email = "login@example.com";
        String rawPassword = PASSWORD;
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(rawPassword, salt);

        fakeDatabase.saveUser(new User(email, salt + "$" + hash));
        responseWriter.getBuffer().setLength(0);

        User loginUser = new User(email, rawPassword);
        authService.handleLogin(gson.toJson(loginUser), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals(ProtocolConstants.RESPONSE_LOGIN_SUCCESS, output);
        assertTrue(fakeSessionManager.isLoggedIn(email));
    }

    @Test
    void testHandleLoginInvalidPassword() {
        String email = "badpass@example.com";
        String salt = SecurityUtils.generateSalt();
        String hash = SecurityUtils.hashPassword(PASSWORD, salt);
        fakeDatabase.saveUser(new User(email, salt + "$" + hash));

        User wrong = new User(email, WRONG_PASSWORD);
        responseWriter.getBuffer().setLength(0);
        authService.handleLogin(gson.toJson(wrong), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith(ProtocolConstants.RESPONSE_LOGIN_FAIL));
        assertTrue(output.contains("Invalid credentials"));
    }

    @Test
    void testHandleLoginNonexistentUser() {
        User ghost = new User("ghost@example.com", "boo");
        authService.handleLogin(gson.toJson(ghost), new Socket(), printWriter);

        String output = responseWriter.toString().trim();
        assertTrue(output.startsWith(ProtocolConstants.RESPONSE_LOGIN_FAIL));
        assertTrue(output.contains("User not found"));
    }

    @Test
    void testHandleLogoutSuccess() {
        String email = "logout@example.com";
        fakeSessionManager.startSession(email, new Socket());

        responseWriter.getBuffer().setLength(0);
        authService.handleLogout(gson.toJson(new User(email, "")), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals(ProtocolConstants.RESPONSE_LOGOUT_SUCCESS, output);
        assertFalse(fakeSessionManager.isLoggedIn(email));
    }

    @Test
    void testHandleLogoutNotLoggedIn() {
        User ghost = new User("ghost@example.com", "nope");
        authService.handleLogout(gson.toJson(ghost), printWriter);

        String output = responseWriter.toString().trim();
        assertEquals(ProtocolConstants.RESPONSE_LOGOUT_SUCCESS, output);
    }

    // --- Fakes ---

    static class FakeDatabase extends FileDatabase {
        private final Map<String, User> users = new HashMap<>();
        private final List<Email> emails = new ArrayList<>();

        public FakeDatabase() {
            super("src/test/resources/test_users.db", "src/test/resources/test_emails.db");
        }

        @Override
        public boolean saveUser(User user) {
            if (users.containsKey(user.getEmail())) {
                return false;
            }
            users.put(user.getEmail(), user);
            return true;
        }

        @Override
        public User getUser(String email) {
            return users.get(email);
        }

        @Override
        public boolean userExists(String email) {
            return users.containsKey(email);
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
