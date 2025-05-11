package client.core;

/**
 * Holds session-related information for the logged-in user.
 * Shared across client components to maintain consistent state.
 */
public class SessionState {

    private volatile boolean loggedIn = false;
    private volatile String sessionEmail = null;
    private volatile String loginFailReason = null;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getEmail() {
        return sessionEmail;
    }

    public void setEmail(String sessionEmail) {
        this.sessionEmail = sessionEmail;
    }

    public String getLoginFailReason() {
        return loginFailReason;
    }

    public void setLoginFailReason(String reason) {
        this.loginFailReason = reason;
    }

    public void reset() {
        this.loggedIn = false;
        this.sessionEmail = null;
        this.loginFailReason = null;
    }

    public boolean hasLoginFailedWithUserNotFound() {
        return "User not found".equalsIgnoreCase(loginFailReason);
    }
}
