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
        fileDatabase.loadAll();
    }

    @Test
    void testSaveAndRetrieveUser() {
        User user = new User("test@example.com", "hashedpassword123");

        boolean saved = fileDatabase.saveUser(user);
        assertTrue(saved, "User should be saved successfully");

        User loaded = fileDatabase.getUser("test@example.com");
        assertNotNull(loaded, "User should be found");
        assertEquals("test@example.com", loaded.getEmail());
        assertEquals("hashedpassword123", loaded.getPassword());
    }

    @Test
    void testDuplicateUserIsNotSaved() {
        User user = new User("test@example.com", "hash");

        boolean firstSave = fileDatabase.saveUser(user);
        boolean secondSave = fileDatabase.saveUser(user); // expected to be rejected

        assertTrue(firstSave, "First save should succeed");
        assertFalse(secondSave, "Second save should be rejected due to duplicate email");

        User loaded = fileDatabase.getUser("test@example.com");
        assertNotNull(loaded, "User should still exist");
        assertEquals("hash", loaded.getPassword(), "Original password should remain unchanged");
    }

    @Test
    void testSaveUserAndReloadFromDisk() {
        User user = new User("reload@example.com", "salt$hash");

        boolean saved = fileDatabase.saveUser(user);
        assertTrue(saved, "User should be saved before disk write");

        fileDatabase.saveAll(); // flush to disk

        FileDatabase reloadedDb = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        reloadedDb.loadAll();

        User loaded = reloadedDb.getUser("reload@example.com");
        assertNotNull(loaded, "User should be found after reload");
        assertEquals("reload@example.com", loaded.getEmail());
        assertEquals("salt$hash", loaded.getPassword());
    }

    @Test
    void testUserCountReflectsSavedUsers() {
        assertEquals(0, fileDatabase.getUserCount(), "Initial user count should be 0");

        fileDatabase.saveUser(new User("a@b.com", "hash1"));
        fileDatabase.saveUser(new User("b@c.com", "hash2"));

        assertEquals(2, fileDatabase.getUserCount(), "User count should reflect successful saves");
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
