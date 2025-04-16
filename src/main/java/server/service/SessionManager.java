package server.service;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, Socket> activeSessions = new ConcurrentHashMap<>();

    public void startSession(String email, Socket socket) {
        activeSessions.put(email.toLowerCase(), socket);
    }

    public void endSession(String email) {
        activeSessions.remove(email.toLowerCase());
    }

    public boolean isLoggedIn(String email) {
        return activeSessions.containsKey(email.toLowerCase());
    }

    public Socket getSessionSocket(String email) {
        return activeSessions.get(email.toLowerCase());
    }

    public void clearAllSessions() {
        activeSessions.clear();
    }
}
