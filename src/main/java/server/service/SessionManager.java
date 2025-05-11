package server.service;

import lombok.extern.slf4j.Slf4j;
import utils.LogHandler;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages active user sessions for authenticated clients.
 * Maintains a thread-safe map of email → socket.
 * Supports session start, termination, lookup, and reverse socket lookup.
 */
@Slf4j
public class SessionManager {

    private final Map<String, Socket> sessionMap = new ConcurrentHashMap<>();

    /**
     * Starts a new session for a user.
     *
     * @param email  User's email
     * @param socket Associated client socket
     */
    public void startSession(String email, Socket socket) {
        sessionMap.put(email, socket);
        log.info("Session started for: {}", email);
        LogHandler.info("Session started for: " + email);
    }

    /**
     * Ends the session for the given user.
     *
     * @param email User's email
     */
    public void endSession(String email) {
        sessionMap.remove(email);
        log.info("Session ended for: {}", email);
        LogHandler.info("Session ended for: " + email);
    }

    /**
     * Checks if a session exists for the given user.
     */
    public boolean isLoggedIn(String email) {
        return sessionMap.containsKey(email);
    }

    /**
     * Gets the socket associated with a logged-in user.
     */
    public Socket getSessionSocket(String email) {
        return sessionMap.get(email);
    }

    /**
     * Finds the user email associated with a socket.
     * Used during forced cleanup (e.g., disconnect).
     */
    public String getEmailForSocket(Socket socket) {
        return sessionMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(socket))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Clears all active sessions (e.g., during shutdown).
     */
    public void clearAllSessions() {
        sessionMap.clear();
        log.info("All sessions cleared");
        LogHandler.info("All sessions cleared");
    }
}
