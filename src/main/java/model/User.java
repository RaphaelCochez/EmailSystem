package model;

import java.util.Objects;

public class User {
    private String email;
    private String password; // hashed or raw depending on context

    public User() {
    } // Required for Gson

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User user = (User) o;
        return email != null && email.equalsIgnoreCase(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email == null ? null : email.toLowerCase());
    }
}
