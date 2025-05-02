package server.service;

import com.google.gson.Gson;
import model.User;
import server.data.FileDatabase;
import server.protocol.ProtocolConstants;
import utils.SecurityUtils;
import utils.LogHandler;

import java.io.PrintWriter;
import java.net.Socket;

public class AuthService {

    private final FileDatabase database;
    private final SessionManager sessionManager;
    private final Gson gson = new Gson();

    public AuthService(FileDatabase database, SessionManager sessionManager) {
        this.database = database;
        this.sessionManager = sessionManager;
    }

    public void handleRegister(String payload, PrintWriter out) {
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null || request.getPassword() == null) {
                out.println(ProtocolConstants.RESP_REGISTER_FAIL + "%%Missing required fields: email or password");
                return;
            }

            String salt = SecurityUtils.generateSalt();
            String hashed = SecurityUtils.hashPassword(request.getPassword(), salt);
            String saltedHash = salt + "$" + hashed;

            User securedUser = new User(request.getEmail(), saltedHash);

            if (database.saveUser(securedUser)) {
                out.println(ProtocolConstants.RESP_REGISTER_SUCCESS);
                LogHandler.log("User registered: " + request.getEmail());
            } else {
                out.println(ProtocolConstants.RESP_REGISTER_FAIL + "%%Email already registered");
                LogHandler.log("Registration failed: Email already registered - " + request.getEmail());
            }

        } catch (Exception e) {
            out.println(ProtocolConstants.RESP_REGISTER_FAIL + "%%Invalid JSON or internal error");
            LogHandler.log("Registration error: " + e.getMessage());
        }
    }

    public void handleLogin(String payload, Socket clientSocket, PrintWriter out) {
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null || request.getPassword() == null) {
                out.println(ProtocolConstants.RESP_LOGIN_FAIL + "%%Missing required fields: email or password");
                return;
            }

            User storedUser = database.getUser(request.getEmail());
            if (storedUser == null) {
                out.println(ProtocolConstants.RESP_LOGIN_FAIL + "%%User not found");
                LogHandler.log("Login failed: User not found - " + request.getEmail());
                return;
            }

            String[] parts = storedUser.getPassword().split("\\$");
            if (parts.length != 2) {
                out.println(ProtocolConstants.RESP_LOGIN_FAIL + "%%Corrupted password entry");
                LogHandler.log("Login failed: Corrupted password format for user - " + request.getEmail());
                return;
            }

            String salt = parts[0];
            String storedHash = parts[1];
            String rawPassword = request.getPassword();

            boolean match = SecurityUtils.verifyPassword(rawPassword, storedHash, salt);

            if (match) {
                sessionManager.startSession(request.getEmail(), clientSocket);
                out.println(ProtocolConstants.RESP_LOGIN_SUCCESS);
                LogHandler.log("User logged in: " + request.getEmail());
            } else {
                out.println(ProtocolConstants.RESP_LOGIN_FAIL + "%%Invalid credentials");
                LogHandler.log("Login failed: Invalid credentials for " + request.getEmail());
            }

        } catch (Exception e) {
            out.println(ProtocolConstants.RESP_LOGIN_FAIL + "%%Invalid JSON or internal error");
            LogHandler.log("Login error: " + e.getMessage());
        }
    }

    public void handleLogout(String payload, PrintWriter out) {
        try {
            User request = gson.fromJson(payload, User.class);

            if (request.getEmail() == null) {
                out.println(ProtocolConstants.RESP_LOGOUT_FAIL + "%%Missing required field: email");
                return;
            }

            sessionManager.endSession(request.getEmail());
            out.println(ProtocolConstants.RESP_LOGOUT_SUCCESS);
            LogHandler.log("User logged out: " + request.getEmail());

        } catch (Exception e) {
            out.println(ProtocolConstants.RESP_LOGOUT_FAIL + "%%Invalid JSON or internal error");
            LogHandler.log("Logout error: " + e.getMessage());
        }
    }
}
