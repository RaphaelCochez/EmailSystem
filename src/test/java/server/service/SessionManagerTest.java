package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class SessionManagerTest {

    private SessionManager sessionManager;
    private final String testEmail = "test@example.com";
    private final Socket dummySocket = new Socket();

    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    void testStartSession() {
        sessionManager.startSession(testEmail, dummySocket);
        assertTrue(sessionManager.isLoggedIn(testEmail), "User should be logged in after starting session");
    }

    @Test
    void testGetSessionSocket() {
        sessionManager.startSession(testEmail, dummySocket);
        Socket retrievedSocket = sessionManager.getSessionSocket(testEmail);
        assertEquals(dummySocket, retrievedSocket, "Retrieved socket should match the one used during session start");
    }

    @Test
    void testEndSession() {
        sessionManager.startSession(testEmail, dummySocket);
        sessionManager.endSession(testEmail);
        assertFalse(sessionManager.isLoggedIn(testEmail), "User should not be logged in after ending session");
        assertNull(sessionManager.getSessionSocket(testEmail), "Session socket should be null after logout");
    }

    @Test
    void testIsLoggedInFalseByDefault() {
        assertFalse(sessionManager.isLoggedIn("not@loggedin.com"), "User should not be logged in by default");
    }

    @Test
    void testClearAllSessions() {
        sessionManager.startSession("user1@example.com", new Socket());
        sessionManager.startSession("user2@example.com", new Socket());

        sessionManager.clearAllSessions();

        assertFalse(sessionManager.isLoggedIn("user1@example.com"));
        assertFalse(sessionManager.isLoggedIn("user2@example.com"));
    }

}
