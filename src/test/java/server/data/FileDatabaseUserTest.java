package server.data;

import model.User;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileDatabaseUserTest {

    private static final Path TEMP_USERS_DB = Path.of("src", "test", "resources", "test_users.db");
    private static final Path TEMP_EMAILS_DB = Path.of("src", "test", "resources", "test_emails.db");
    private FileDatabase fileDatabase;

    @BeforeEach
    void setUp() {
        try {
            Files.createDirectories(TEMP_USERS_DB.getParent());
            Files.deleteIfExists(TEMP_USERS_DB);
            Files.deleteIfExists(TEMP_EMAILS_DB);
            Files.createFile(TEMP_USERS_DB);
            Files.createFile(TEMP_EMAILS_DB);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Setup failed: " + e.getMessage());
        }

        fileDatabase = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        fileDatabase.loadAll(); // Load initial state (if any)
    }

    @Test
    void testSaveAndRetrieveUser() {
        User user = new User("test@example.com", "hashedpassword123");

        boolean saved = fileDatabase.saveUser(user);
        assertTrue(saved, "User should be saved");

        User loaded = fileDatabase.getUser("test@example.com");
        assertNotNull(loaded, "User should be found");
        assertEquals("test@example.com", loaded.getEmail());
        assertEquals("hashedpassword123", loaded.getPassword());
    }

    @Test
    void testDuplicateUserIsNotSaved() {
        User user = new User("test@example.com", "hash");

        boolean firstSave = fileDatabase.saveUser(user);
        assertTrue(firstSave, "First user should be saved");

        boolean secondSave = fileDatabase.saveUser(user);
        assertFalse(secondSave, "Duplicate user should not be saved");
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(TEMP_USERS_DB);
            Files.deleteIfExists(TEMP_EMAILS_DB);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
