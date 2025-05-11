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
        } catch (IOException e) {
            e.printStackTrace();
            fail("Failed to set up test files: " + e.getMessage());
        }

        fileDatabase = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        fileDatabase.loadAll();
    }

    @Test
    void testSaveRetrieveAndReloadEmail() {
        email = createSampleEmail("recipient@example.com", "sender@example.com", "Hello");

        fileDatabase.addEmail(email);

        List<Email> inMemory = fileDatabase.getEmailsForUser("recipient@example.com", false);
        assertEquals(1, inMemory.size(), "One email should be retrieved for recipient (memory)");
        assertEmailEquals(email, inMemory.get(0));

        fileDatabase.saveAll();

        FileDatabase reloadedDb = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        reloadedDb.loadAll();

        List<Email> reloaded = reloadedDb.getEmailsForUser("recipient@example.com", false);
        assertEquals(1, reloaded.size(), "Reloaded DB should retrieve the email from disk");
        assertEmailEquals(email, reloaded.get(0));
    }

    @Test
    void testGetEmailByIdReturnsCorrectEmail() {
        email = createSampleEmail("to@x.com", "from@y.com", "ID Check");
        email.setId("test-id-123");

        fileDatabase.addEmail(email);

        Email result = fileDatabase.getEmailById("test-id-123");
        assertNotNull(result);
        assertEquals("ID Check", result.getSubject());
    }

    @Test
    void testSearchEmailsReturnsMatchingResult() {
        email = createSampleEmail("alice@example.com", "bob@example.com", "Weekly Update");
        email.setBody("Progress is great.");
        email.setId("search-id");

        fileDatabase.addEmail(email);

        List<Email> result = fileDatabase.searchEmails("alice@example.com", false, "progress");
        assertEquals(1, result.size());
        assertEquals("search-id", result.get(0).getId());
    }

    @Test
    void testGetEmailsForUnknownUserReturnsEmpty() {
        List<Email> result = fileDatabase.getEmailsForUser("ghost@nowhere.com", false);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "Expected empty list for nonexistent user");
    }

    private Email createSampleEmail(String to, String from, String subject) {
        Email e = new Email();
        e.setId(UUID.randomUUID().toString());
        e.setTo(to);
        e.setFrom(from);
        e.setSubject(subject);
        e.setBody("This is the email body.");
        e.setTimestamp("2025-04-10T14:00:00Z");
        e.setVisible(true);
        e.setEdited(false);
        return e;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
