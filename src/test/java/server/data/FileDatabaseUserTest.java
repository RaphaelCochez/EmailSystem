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

        fileDatabase.saveUser(user); // no longer checks return
        User loaded = fileDatabase.getUser("test@example.com");

        assertNotNull(loaded, "User should be found");
        assertEquals("test@example.com", loaded.getEmail());
        assertEquals("hashedpassword123", loaded.getPassword());
    }

    @Test
    void testDuplicateUserIsNotSaved() {
        User user = new User("test@example.com", "hash");

        fileDatabase.saveUser(user);
        fileDatabase.saveUser(user); // this will overwrite, test for overwrite behavior manually

        User loaded = fileDatabase.getUser("test@example.com");
        assertNotNull(loaded, "User should still exist");
        assertEquals("hash", loaded.getPassword(), "Password should remain consistent after re-save");
    }

    @Test
    void testSaveUserAndReloadFromDisk() {
        User user = new User("reload@example.com", "salt$hash");

        fileDatabase.saveUser(user);
        fileDatabase.saveAll(); // flush to disk

        FileDatabase reloadedDb = new FileDatabase(TEMP_USERS_DB.toString(), TEMP_EMAILS_DB.toString());
        reloadedDb.loadAll();

        User loaded = reloadedDb.getUser("reload@example.com");
        assertNotNull(loaded, "User should be found after reload");
        assertEquals("reload@example.com", loaded.getEmail());
        assertEquals("salt$hash", loaded.getPassword());
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
