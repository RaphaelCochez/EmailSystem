# FileDatabase Test Documentation

This section describes the unit tests for the `FileDatabase.java` class. These tests verify core persistence functionality for user and email data using flat-file storage and GSON for JSON serialization.

## Testing Framework

All tests are written using **JUnit 5 (Jupiter)**.

### Annotations Used

- `@BeforeEach` — Sets up the environment before each test.
- `@AfterEach` — Cleans up resources after each test.
- `@Test` — Marks a method as a test case.

### Assertions

JUnit's `Assertions` class is used to validate expected behavior:

- `assertTrue` / `assertFalse` — Verifies boolean conditions.
- `assertEquals` — Checks value equality.
- `assertNotNull` — Ensures a value is not `null`.

All assertions are imported from:

```java
import static org.junit.jupiter.api.Assertions.*;
```

## Test Environment Setup

Each test uses a temporary `.db` file located in `src/test/resources`:
- `test_users.db`
- `test_emails.db`

These files are created and cleaned up before and after each test. The constructor for `FileDatabase` accepts custom file paths to support test isolation and reproducibility.

## Email Tests

### testSaveAndLoadEmail

This unit test verifies that the `FileDatabase` class can correctly persist an `Email` object to disk and read it back without data loss.

**What It Verifies:**
- Emails can be written to a flat file (`test_emails.db`) using GSON serialization.
- The file content is correctly deserialized into Java objects.
- No data is lost or altered during the write/read cycle.
- All fields (`id`, `to`, `from`, `subject`, `body`, `timestamp`, `visible`, `edited`) match exactly between the original and retrieved objects.

**Why It Matters:**
Reliable persistence is crucial to ensure users don't lose emails and that data integrity is preserved across sessions. This test isolates and validates that mechanism.

**Test Logic Summary:**
1. Set up clean temp databases for isolation.
2. Construct a full `Email` object.
3. Save it to file using `saveEmail(...)`.
4. Confirm file contains serialized data.
5. Load the data using `loadAllEmails()`.
6. Compare field-by-field to ensure equality.
7. Clean up test files.

**Excerpt:** ```src\test\java\server\data\FileDatabaseEmailTest.java```


```java
// Email creation and persistence
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

// Verification
List<Email> loaded = fileDatabase.loadAllEmails();
assertEquals(1, loaded.size(), "One email should be loaded");

Email loadedEmail = loaded.get(0);
assertEquals(email.getId(), loadedEmail.getId());
```

## User Tests

### testSaveAndRetrieveUser

This test ensures that a new `User` can be saved to the file system and retrieved accurately using their email address.

**What It Verifies:**
- A `User` object can be stored in the flat file `test_users.db` using JSON.
- The same user can be looked up by their email address using `getUser(...)`.
- All user data remains intact after deserialization.

**Why It Matters:**
This test guarantees the system can persist user credentials and recover them for login or session handling, which is essential for authentication functionality.

**Test Logic Summary:**
1. Set up temporary database files.
2. Create a `User` object with email and hashed password.
3. Save the user using `saveUser(...)` and assert success.
4. Retrieve the user with `getUser(...)` by email.
5. Assert that the result is not null and matches input data.

**Excerpt:** ```src\test\java\server\data\FileDatabaseUserTest.java```
```java
User user = new User("test@example.com", "hashedpassword123");
boolean saved = fileDatabase.saveUser(user);
assertTrue(saved, "User should be saved successfully");

User retrieved = fileDatabase.getUser("test@example.com");
assertNotNull(retrieved, "User should be found");
assertEquals("test@example.com", retrieved.getEmail());
assertEquals("hashedpassword123", retrieved.getPasswordHash());
```
## logs output sample
```
PS C:\Users\Raphael\Documents\Java\EmailSystem> mvn test
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by org.fusesource.jansi.internal.JansiLoader in an unnamed module (file:/C:/ProgramData/chocolatey/lib/maven/apache-maven-3.9.9/lib/jansi-2.4.1.jar)
WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
WARNING: Restricted methods will be blocked in a future release unless native access is enabled

WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/C:/ProgramData/chocolatey/lib/maven/apache-maven-3.9.9/lib/guava-33.2.1-jre.jar)
WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
[INFO] Scanning for projects...
[INFO] 
[INFO] --------------------< com.emailsystem:email-system >--------------------
[INFO] Building email-system 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ email-system ---
[INFO] skip non existing resourceDirectory C:\Users\Raphael\Documents\Java\EmailSystem\src\main\resources
[INFO]
[INFO] --- compiler:3.13.0:compile (default-compile) @ email-system ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 14 source files with javac [debug target 24] to target\classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ email-system ---
[INFO] Copying 0 resource from src\test\resources to target\test-classes
[INFO]
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ email-system ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 2 source files with javac [debug target 24] to target\test-classes
[INFO] 
[INFO] --- surefire:3.0.0-M9:test (default-test) @ email-system ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running server.data.FileDatabaseEmailTest
? Test DBs prepared:
- C:\Users\Raphael\Documents\Java\EmailSystem\src\test\resources\test_emails.db
- C:\Users\Raphael\Documents\Java\EmailSystem\src\test\resources\test_users.db
? Email DB content after save:
{"id":"3b5337de-c7eb-4045-8543-2e3842f88487","to":"recipient@example.com","from":"sender@example.com","subject":"Hello","body":"Message content here","timestamp":"2025-04-10T14:00:00Z","visible":true,"edited":false}
? Cleaned up test DB files.
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.155 s - in server.data.FileDatabaseEmailTest
[INFO] Running server.data.FileDatabaseUserTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s - in server.data.FileDatabaseUserTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.018 s - in server.data.FileDatabaseUserTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Results:
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.469 s
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.469 s
[INFO] Finished at: 2025-04-16T13:36:26+02:00
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.469 s
[INFO] Finished at: 2025-04-16T13:36:26+02:00
[INFO] Total time:  2.469 s
[INFO] Finished at: 2025-04-16T13:36:26+02:00
[INFO] Finished at: 2025-04-16T13:36:26+02:00
[INFO] ------------------------------------------------------------------------
PS C:\Users\Raphael\Documents\Java\EmailSystem>


```