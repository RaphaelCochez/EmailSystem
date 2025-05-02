package server.service;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import utils.LogHandler;

/**
 * SessionManager keeps track of active user sessions,
 * mapping email addresses to open client sockets.
 */
public class SessionManager {

    private final Map<String, Socket> activeSessions = new ConcurrentHashMap<>();

    /**
     * Starts a session for a given user email.
     *
     * @param email  the user's email address
     * @param socket the socket representing the session
     */
    public void startSession(String email, Socket socket) {
        String normalized = email.toLowerCase();
        activeSessions.put(normalized, socket);
        LogHandler.log("Session started for user: " + normalized);
    }

    /**
     * Ends the session for a given user email.
     *
     * @param email the user's email address
     */
    public void endSession(String email) {
        String normalized = email.toLowerCase();
        activeSessions.remove(normalized);
        LogHandler.log("Session ended for user: " + normalized);
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @param email the user's email
     * @return true if logged in
     */
    public boolean isLoggedIn(String email) {
        return activeSessions.containsKey(email.toLowerCase());
    }

    /**
     * Gets the socket associated with a user session.
     *
     * @param email the user's email
     * @return the socket, or null if no session
     */
    public Socket getSessionSocket(String email) {
        return activeSessions.get(email.toLowerCase());
    }

    /**
     * Clears all active sessions (useful during server shutdown).
     */
    public void clearAllSessions() {
        LogHandler.log("Clearing all sessions...");
        activeSessions.clear();
    }

    /**
     * Optional: Returns a list of active user emails.
     */
    public List<String> getActiveUsers() {
        return new ArrayList<>(activeSessions.keySet());
    }
}
