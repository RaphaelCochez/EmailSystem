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
    private Email email;

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
        fileDatabase.loadAll();
    }

    @Test
    void testSaveRetrieveAndReloadEmail() {
        email = new Email();
        email.setId(UUID.randomUUID().toString());
        email.setTo("recipient@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Hello");
        email.setBody("Message content here");
        email.setTimestamp("2025-04-10T14:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        // Save to memory
        fileDatabase.addEmail(email); // <-- was saveEmail(email)

        // Load from memory
        List<Email> inMemory = fileDatabase.getEmailsForUser("recipient@example.com", false); // false = received
        assertEquals(1, inMemory.size(), "One email should be retrieved for recipient (memory)");

        Email loaded = inMemory.get(0);
        assertEmailEquals(email, loaded);

        // Persist to disk
        fileDatabase.saveAll();

        // Reload from disk
        FileDatabase reloadedDb = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        reloadedDb.loadAll();

        List<Email> reloaded = reloadedDb.getEmailsForUser("recipient@example.com", false); // false = received
        assertEquals(1, reloaded.size(), "Reloaded DB should retrieve the email from disk");

        Email reloadedEmail = reloaded.get(0);
        assertEmailEquals(email, reloadedEmail);
    }

    private void assertEmailEquals(Email expected, Email actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTo(), actual.getTo());
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getSubject(), actual.getSubject());
        assertEquals(expected.getBody(), actual.getBody());
        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.isVisible(), actual.isVisible());
        assertEquals(expected.isEdited(), actual.isEdited());
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
