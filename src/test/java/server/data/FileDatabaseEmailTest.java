package server.data;

import model.Email;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileDatabaseEmailTest {

    private static final Path TEMP_EMAILS_DB = Path.of("src", "test", "resources", "test_emails.db");
    private static final Path TEMP_USERS_DB = Path.of("src", "test", "resources", "test_users.db");

    private FileDatabase fileDatabase;

    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(TEMP_EMAILS_DB.getParent());
            Files.deleteIfExists(TEMP_EMAILS_DB);
            Files.deleteIfExists(TEMP_USERS_DB);
            Files.createFile(TEMP_EMAILS_DB);
            Files.createFile(TEMP_USERS_DB);

            System.out.println("Test DBs prepared:");
            System.out.println("- " + TEMP_EMAILS_DB.toAbsolutePath());
            System.out.println("- " + TEMP_USERS_DB.toAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to set up test files: " + e.getMessage());
        }

        fileDatabase = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        fileDatabase.loadAll(); // Load any initial state (empty for this test)
    }

    @Test
    void testSaveAndRetrieveEmailFromMemory() {
        Email email = new Email();
        email.setId(UUID.randomUUID().toString());
        email.setTo("recipient@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Hello");
        email.setBody("Message content here");
        email.setTimestamp("2025-04-10T14:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        boolean saved = fileDatabase.saveEmail(email);
        assertTrue(saved, "Email should be saved successfully");

        List<Email> loaded = fileDatabase.getEmailsForUser("recipient@example.com", "received");
        assertEquals(1, loaded.size(), "One email should be retrieved for recipient");

        Email loadedEmail = loaded.get(0);
        assertEquals(email.getId(), loadedEmail.getId());
        assertEquals(email.getTo(), loadedEmail.getTo());
        assertEquals(email.getFrom(), loadedEmail.getFrom());
        assertEquals(email.getSubject(), loadedEmail.getSubject());
        assertEquals(email.getBody(), loadedEmail.getBody());
        assertEquals(email.getTimestamp(), loadedEmail.getTimestamp());
        assertTrue(loadedEmail.isVisible());
        assertFalse(loadedEmail.isEdited());
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(TEMP_EMAILS_DB);
            Files.deleteIfExists(TEMP_USERS_DB);
            System.out.println("Cleaned up test DB files.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
