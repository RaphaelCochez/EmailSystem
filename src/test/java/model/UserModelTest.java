package model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {

    @Test
    public void testConstructorAndGetters() {
        User user = new User("test@example.com", "hashed$1234");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashed$1234", user.getPassword());
    }

    @Test
    public void testSetPassword() {
        User user = new User("test@example.com", "oldPassword");
        user.setPassword("newPassword");

        assertEquals("newPassword", user.getPassword());
    }

    @Test
    public void testEqualityAndHashing() {
        User user1 = new User("user@example.com", "pw1");
        User user2 = new User("USER@example.com", "pw2"); // same email, different case

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    public void testJsonSerializationAndDeserialization() {
        User original = new User("json@example.com", "somesaltedhash");
        Gson gson = new Gson();

        String json = gson.toJson(original);
        User parsed = gson.fromJson(json, User.class);

        assertEquals(original.getEmail(), parsed.getEmail());
        assertEquals(original.getPassword(), parsed.getPassword());
    }
}
