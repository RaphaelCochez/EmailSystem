package server.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import model.User;
import server.data.FileDatabase;
import utils.LogHandler;
import utils.ProtocolConstants;
import utils.SecurityUtils;
import utils.ServerConstants;

import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles authentication operations: register, login, and logout.
 * Combines SLF4J structured logging with persistent LogHandler file logging.
 */
@Slf4j
public class AuthService {

    private final FileDatabase database;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public AuthService(FileDatabase database, SessionManager sessionManager) {
        this.database = database;
        this.sessionManager = sessionManager;
    }

    /**
     * Handles user registration: validation, hashing, and persistence.
     */
    public void handleRegister(String payload, PrintWriter out) {
        long start = System.nanoTime();
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null || request.getPassword() == null) {
                out.println(ProtocolConstants.RESPONSE_REGISTER_FAIL + "%%Missing required fields: email or password");
                return;
            }

            if (database.userExists(request.getEmail())) {
                out.println(ProtocolConstants.RESPONSE_REGISTER_FAIL + "%%Email already registered");
                log.warn("Registration failed: Email already registered - {}", request.getEmail());
                LogHandler.warn("Registration failed: Email already registered - " + request.getEmail());
                return;
            }

            String salt = SecurityUtils.generateSalt();
            String hashed = SecurityUtils.hashPassword(request.getPassword(), salt);
            User securedUser = new User(request.getEmail(), salt + "$" + hashed);

            database.saveUser(securedUser);
            out.println(ProtocolConstants.RESPONSE_REGISTER_SUCCESS);
            log.info("User registered: {}", request.getEmail());
            LogHandler.info("User registered: " + request.getEmail());

        } catch (Exception e) {
            out.println(ProtocolConstants.RESPONSE_REGISTER_FAIL + "%%Invalid JSON or internal error");
            log.error("Registration error: {}", e.getMessage());
            LogHandler.error("Registration error: " + e.getMessage());
            if (ServerConstants.DEBUG_MODE)
                e.printStackTrace();
        } finally {
            long end = System.nanoTime();
            log.debug("handleRegister completed in {} µs", (end - start) / 1000);
        }
    }

    /**
     * Verifies login credentials, starts a session if successful.
     */
    public void handleLogin(String payload, Socket clientSocket, PrintWriter out) {
        long start = System.nanoTime();
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null || request.getPassword() == null) {
                out.println(ProtocolConstants.RESPONSE_LOGIN_FAIL + "%%Missing required fields: email or password");
                return;
            }

            User storedUser = database.getUser(request.getEmail());
            if (storedUser == null) {
                out.println(ProtocolConstants.RESPONSE_LOGIN_FAIL + "%%User not found");
                log.warn("Login failed: User not found - {}", request.getEmail());
                LogHandler.warn("Login failed: User not found - " + request.getEmail());
                return;
            }

            String[] parts = storedUser.getPassword().split("\\$");
            if (parts.length != 2) {
                out.println(ProtocolConstants.RESPONSE_LOGIN_FAIL + "%%Corrupted password entry");
                log.error("Login failed: Corrupted password for user - {}", request.getEmail());
                LogHandler.error("Login failed: Corrupted password for user - " + request.getEmail());
                return;
            }

            boolean match = SecurityUtils.verifyPassword(request.getPassword(), parts[1], parts[0]);

            if (match) {
                sessionManager.startSession(request.getEmail(), clientSocket);
                out.println(ProtocolConstants.RESPONSE_LOGIN_SUCCESS);
                log.info("User logged in: {}", request.getEmail());
                LogHandler.info("User logged in: " + request.getEmail());
            } else {
                out.println(ProtocolConstants.RESPONSE_LOGIN_FAIL + "%%Invalid credentials");
                log.warn("Login failed: Invalid credentials for {}", request.getEmail());
                LogHandler.warn("Login failed: Invalid credentials for " + request.getEmail());
            }

        } catch (Exception e) {
            out.println(ProtocolConstants.RESPONSE_LOGIN_FAIL + "%%Invalid JSON or internal error");
            log.error("Login error: {}", e.getMessage());
            LogHandler.error("Login error: " + e.getMessage());
            if (ServerConstants.DEBUG_MODE)
                e.printStackTrace();
        } finally {
            long end = System.nanoTime();
            log.debug("handleLogin completed in {} µs", (end - start) / 1000);
        }
    }

    /**
     * Ends a user session if valid.
     */
    public void handleLogout(String payload, PrintWriter out) {
        long start = System.nanoTime();
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null) {
                out.println(ProtocolConstants.RESPONSE_LOGOUT_FAIL + "%%Missing required field: email");
                return;
            }

            sessionManager.endSession(request.getEmail());
            out.println(ProtocolConstants.RESPONSE_LOGOUT_SUCCESS);
            log.info("User logged out: {}", request.getEmail());
            LogHandler.info("User logged out: " + request.getEmail());

        } catch (Exception e) {
            out.println(ProtocolConstants.RESPONSE_LOGOUT_FAIL + "%%Invalid JSON or internal error");
            log.error("Logout error: {}", e.getMessage());
            LogHandler.error("Logout error: " + e.getMessage());
            if (ServerConstants.DEBUG_MODE)
                e.printStackTrace();
        } finally {
            long end = System.nanoTime();
            log.debug("handleLogout completed in {} µs", (end - start) / 1000);
        }
    }
}
