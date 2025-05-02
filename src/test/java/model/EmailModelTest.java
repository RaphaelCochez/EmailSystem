package model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import utils.Constants;

import static org.junit.jupiter.api.Assertions.*;

public class EmailModelTest {

    @Test
    public void testConstructorAndGetters() {
        Email email = new Email("123", "to@example.com", "from@example.com", "Subject", "Body",
                "2025-05-01T10:00:00Z", true, false);

        assertEquals("123", email.getId());
        assertEquals("to@example.com", email.getTo());
        assertEquals("from@example.com", email.getFrom());
        assertEquals("Subject", email.getSubject());
        assertEquals("Body", email.getBody());
        assertEquals("2025-05-01T10:00:00Z", email.getTimestamp());
        assertTrue(email.isVisible());
        assertFalse(email.isEdited());
    }

    @Test
    public void testTimestampValidationFails() {
        Email email = new Email();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            email.setTimestamp("invalid-date");
        });

        assertTrue(exception.getMessage().contains(Constants.TIMESTAMP_FORMAT));
    }

    @Test
    public void testVisibilityAndEditedFlags() {
        Email email = new Email();
        email.setVisible(true);
        email.setEdited(true);

        assertTrue(email.isVisible());
        assertTrue(email.isEdited());
    }

    @Test
    public void testJsonSerializationAndDeserialization() {
        Email original = new Email("abc", "alice@example.com", "bob@example.com",
                "Hey", "Test body", "2025-05-02T12:00:00Z", true, false);

        Gson gson = new Gson();
        String json = gson.toJson(original);

        Email parsed = gson.fromJson(json, Email.class);

        assertEquals(original.getId(), parsed.getId());
        assertEquals(original.getTo(), parsed.getTo());
        assertEquals(original.getFrom(), parsed.getFrom());
        assertEquals(original.getSubject(), parsed.getSubject());
        assertEquals(original.getBody(), parsed.getBody());
        assertEquals(original.getTimestamp(), parsed.getTimestamp());
        assertEquals(original.isVisible(), parsed.isVisible());
        assertEquals(original.isEdited(), parsed.isEdited());
    }
}
