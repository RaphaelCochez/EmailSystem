package client;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommandFormaterTest {

    @Test
    void testRegisterFormat() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "user@example.com");
        data.put("password", "secret123");

        String result = CommandFormatter.format("REGISTER", data);
        assertTrue(result.startsWith("REGISTER%%{"));
        assertTrue(result.contains("\"email\":\"user@example.com\""));
        assertTrue(result.contains("\"password\":\"secret123\""));
    }

    @Test
    void testLoginFormat() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "user@example.com");
        data.put("password", "mypassword");

        String result = CommandFormatter.format("LOGIN", data);
        assertTrue(result.startsWith("LOGIN%%{"));
        assertTrue(result.contains("\"email\":\"user@example.com\""));
        assertTrue(result.contains("\"password\":\"mypassword\""));
    }

    @Test
    void testSendEmailFormat() {
        Map<String, String> data = new HashMap<>();
        data.put("to", "friend@example.com");
        data.put("from", "me@example.com");
        data.put("subject", "Hello");
        data.put("body", "How are you?");

        String result = CommandFormatter.format("SEND_EMAIL", data);
        assertTrue(result.startsWith("SEND_EMAIL%%{"));
        assertTrue(result.contains("\"to\":\"friend@example.com\""));
        assertTrue(result.contains("\"from\":\"me@example.com\""));
        assertTrue(result.contains("\"subject\":\"Hello\""));
        assertTrue(result.contains("\"body\":\"How are you?\""));
    }

    @Test
    void testReadEmailFormat() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "user@example.com");
        data.put("id", "1234");

        String result = CommandFormatter.format("READ_EMAIL", data);
        assertTrue(result.startsWith("READ_EMAIL%%{"));
        assertTrue(result.contains("\"id\":\"1234\""));
    }

    @Test
    void testSearchEmailFormat() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "user@example.com");
        data.put("type", "inbox");
        data.put("keyword", "project");

        String result = CommandFormatter.format("SEARCH_EMAIL", data);
        assertTrue(result.startsWith("SEARCH_EMAIL%%{"));
        assertTrue(result.contains("\"keyword\":\"project\""));
    }
}
