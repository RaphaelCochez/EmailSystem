package server.service;

import model.Email;
import model.User;
import org.junit.jupiter.api.*;
import server.data.FileDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EmailServiceTest {

    private static final Path TEMP_USERS_DB = Path.of("src", "test", "resources", "test_users.db");
    private static final Path TEMP_EMAILS_DB = Path.of("src", "test", "resources", "test_emails.db");

    private FileDatabase fileDatabase;
    private EmailService emailService;

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(TEMP_USERS_DB.getParent());
        Files.deleteIfExists(TEMP_USERS_DB);
        Files.deleteIfExists(TEMP_EMAILS_DB);
        Files.createFile(TEMP_USERS_DB);
        Files.createFile(TEMP_EMAILS_DB);

        fileDatabase = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        fileDatabase.loadAll();
        emailService = new EmailService(fileDatabase);
    }

    @Test
    void testSendEmailToRegisteredUser() {
        fileDatabase.saveUser(new User("receiver@example.com", "hashedPass"));

        Email email = new Email();
        email.setTo("receiver@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Subject");
        email.setBody("Body");
        email.setTimestamp("2025-04-10T10:00:00Z");

        boolean result = emailService.sendEmail(email);
        assertTrue(result);
    }

    @Test
    void testSendEmailToUnregisteredUserFails() {
        Email email = new Email();
        email.setTo("notfound@example.com");
        email.setFrom("sender@example.com");
        email.setSubject("Subject");
        email.setBody("Body");
        email.setTimestamp("2025-04-10T10:00:00Z");

        boolean result = emailService.sendEmail(email);
        assertFalse(result);
    }

    @Test
    void testGetReceivedEmails() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId(UUID.randomUUID().toString());
        email.setTo("alice@example.com");
        email.setFrom("bob@example.com");
        email.setSubject("Hello");
        email.setBody("Hi Alice");
        email.setTimestamp("2025-04-10T11:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        List<Email> received = emailService.getReceivedEmails("alice@example.com");
        assertEquals(1, received.size());
        assertEquals("Hello", received.get(0).getSubject());
    }

    @Test
    void testGetSentEmails() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId(UUID.randomUUID().toString());
        email.setTo("bob@example.com");
        email.setFrom("alice@example.com");
        email.setSubject("Update");
        email.setBody("Status update");
        email.setTimestamp("2025-04-10T12:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        List<Email> sent = emailService.getSentEmails("alice@example.com");
        assertEquals(1, sent.size());
        assertEquals("Update", sent.get(0).getSubject());
    }

    @Test
    void testSearchEmailsReceived() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId("e123");
        email.setTo("alice@example.com");
        email.setFrom("bob@example.com");
        email.setSubject("Project Update");
        email.setBody("Here's an update");
        email.setTimestamp("2025-04-10T13:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        List<Email> result = emailService.searchEmails("alice@example.com", "received", "project");
        assertEquals(1, result.size());
        assertEquals("e123", result.get(0).getId());
    }

    @Test
    void testSearchEmailsSent() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId("s123");
        email.setTo("bob@example.com");
        email.setFrom("alice@example.com");
        email.setSubject("Follow up");
        email.setBody("Checking in");
        email.setTimestamp("2025-04-10T14:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        List<Email> result = emailService.searchEmails("alice@example.com", "sent", "bob");
        assertEquals(1, result.size());
        assertEquals("s123", result.get(0).getId());
    }

    @Test
    void testGetEmailByIdSuccess() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId("abc123");
        email.setTo("alice@example.com");
        email.setFrom("bob@example.com");
        email.setSubject("Meeting");
        email.setBody("Let's meet at 10");
        email.setTimestamp("2025-04-10T15:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        Email retrieved = emailService.getEmailById("alice@example.com", "abc123");
        assertNotNull(retrieved);
        assertEquals("Meeting", retrieved.getSubject());
    }

    @Test
    void testGetEmailByIdUnauthorized() {
        fileDatabase.saveUser(new User("alice@example.com", "hash"));

        Email email = new Email();
        email.setId("blocked123");
        email.setTo("charlie@example.com");
        email.setFrom("bob@example.com");
        email.setSubject("Confidential");
        email.setBody("Private");
        email.setTimestamp("2025-04-10T16:00:00Z");
        email.setVisible(true);
        email.setEdited(false);

        fileDatabase.addEmail(email);

        Email result = emailService.getEmailById("alice@example.com", "blocked123");
        assertNull(result);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(TEMP_USERS_DB);
        Files.deleteIfExists(TEMP_EMAILS_DB);
    }
}
