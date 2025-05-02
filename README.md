# Email System – README

## Docs Formatter Tool
For clean Markdown formatting, use: https://www.rich-text-to-markdown.com/

---

## Project Structure

```plaintext
/EmailSystem
├── /src
│   ├── /client
│   │   ├── EmailClientCLI.java          ← CLI client main logic (UI, user commands)
│   │   ├── ServerListener.java          ← Listens for server responses
│   │   └── CommandFormatter.java        ← Formats user commands into protocol format
│   │
│   ├── /server
│   │   ├── EmailServer.java             ← Entry point for the server (thread-per-client)
│   │   ├── /handler
│   │   │   ├── ClientHandler.java       ← Handles each client connection in a thread
│   │   │   └── CommandHandler.java      ← Routes and executes commands from clients
│   │   ├── /protocol
│   │   │   └── ProtocolConstants.java   ← Command keywords and response constants
│   │   ├── /service
│   │   │   ├── AuthService.java         ← Login, registration, logout logic
│   │   │   ├── EmailService.java        ← Inbox, sent mail, search, read logic
│   │   │   └── SessionManager.java      ← Tracks active sessions and user states
│   │   ├── /data
│   │   │   └── FileDatabase.java        ← Manages JSON-based file persistence
│
│   ├── /model
│   │   ├── User.java                    ← Data class for user credentials
│   │   └── Email.java                   ← Data class for email structure
│
│   ├── /utils
│   │   ├── Constants.java               ← Central config for paths, flags, CLI, protocol
│   │   ├── LogHandler.java              ← Async logging handler with shutdown support
│   │   └── SecurityUtils.java           ← Salted password hashing and verification
│
├── /resources
│   ├── users.db                         ← Flat file for users
│   └── emails.db                        ← Flat file for emails
│
├── /test
│   ├── /java
│   │   ├── /model                       ← Model unit tests
│   │   ├── /server/data                 ← FileDatabase tests
│   │   ├── /server/service              ← Auth, Email, Session tests
│   │   ├── /server/handler              ← ClientHandler, CommandHandler tests
│   │   └── /utils                       ← Logging and security utility tests
│   └── /resources
│       ├── test_users.db                ← Isolated test data
│       └── test_emails.db
│
├── /logs
│   └── server.log                       ← Log output (optional)
│
└── README.md
```


## Key structure

| **Folder**   | **Purpose**                                                              |
|--------------|---------------------------------------------------------------------------|
| `client/`    | CLI-related logic (user I/O, command formatting)                          |
| `server/`    | Server-side socket, threads, services                                     |
| `model/`     | Data classes for `User`, `Email`, etc.                                    |
| `utils/`     | Reusable tools (e.g., encryption, JSON, constants)                        |
| `database/`  | Flat-file DB — stores users and emails                                    |
| `logs/`      | Optional but good for debugging (`@Slf4j` output)                         |

## Design Rationale
The server follows a modular, layered architecture to enable clean unit testing, code isolation, and maintainable logic separation.

Each class has a single responsibility:

* `AuthService` handles security/auth

* `EmailService` handles message logic

* `SessionManager` manages per-user state

* `FileDatabase` handles I/O only at startup/shutdown

* `CommandHandler` is the glue between input and services

# Testing Framework

This project uses **JUnit 5 (Jupiter)** for unit testing. The testing approach ensures that core functionality such as authentication, data persistence, session management, and utility functions are thoroughly validated in isolation.

Also, tests use isolated test_users.db and test_emails.db files.

**Tests cover:**

* Data model integrity

* File persistence

* Authentication and password hashing

* Session management

* Command routing

* Logging robustness


### Test Structure

Unit tests are located under `src/test/java`, mirroring the structure of the main application code. Temporary database files (`test_users.db`, `test_emails.db`) are stored in `src/test/resources` and used during test execution to avoid interfering with production data.

### Test Focus

- File-based persistence tests validate read/write logic for users and emails.
- Authentication tests ensure password hashing and verification are consistent and secure.
- Session management tests verify active user session tracking and cleanup during logout or disconnection.
- Security utility tests validate cryptographic functions such as SHA-256 hashing.

### Running Tests

All tests are configured to run using Maven:

```bash
mvn test
```

Run a clean test session from scratch:

```bash
mvn clean test
```

Run tests with debug output for detailed information:

```bash
mvn test --debug
```

### Test Dependencies

Ensure your `pom.xml` includes the following dependency for JUnit 5:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.0</version>
  <scope>test</scope>
</dependency>
```

---

## Key Testing Requirements (as per CA2 Rubric)

To meet the expectations outlined in the CA2 project brief, the testing framework and approach must validate the following critical components of the system.

### Core Logic and Data Integrity

**Files:**

```
src/test/java/model/EmailModelTest.java  
src/test/java/model/EmailModelTest.md  
src/test/java/model/UserModelTest.java  
src/test/java/model/UserModelTest.md  
```

- All model-layer classes must be covered by unit tests.
- Includes logic for `Email`, `User`, session state, filtering, and persistence.
- Test both valid and invalid scenarios, including edge cases.

### Persistent Storage

**Files:**

```
src/test/java/server/data/FileDatabaseEmailTest.java
src/test/java/server/data/FileDatabaseEmailTest.md    
src/test/java/server/data/FileDatabaseUserTest.java  
src/test/java/server/data/FileDatabaseUserTest.md  
```

- Verify reading and writing JSON entries.
- Ensure file creation and format validity.
- Isolate test and production data by using temporary `.db` files.

### Authentication and Security

**Files:**

```
src/test/java/server/service/AuthServiceTest.java  
src/test/java/server/service/AuthServiceTest.md  
src/test/java/utills/SecurityUtillsTest.java  
src/test/java/utills/SecurityUtillsTest.md  
```

- Validate consistent hash output for known inputs.
- Ensure password verification logic behaves as expected.
- Handle malformed salt/hash input formats gracefully.

### Session Management

**Files:**

```
src/test/java/server/service/AuthServiceTest.java  
src/test/java/server/service/AuthServiceTest.md  
src/test/java/server/service/SessionManagerTest.java  
src/test/java/server/service/SessionManagerTest.md  
```

- Confirm accurate tracking of login/logout state.
- Test session invalidation on disconnection.
- Handle concurrent sessions and avoid state conflicts.

### Exception Handling and Robustness

- Simulate forced socket closures.
- Simulate abrupt client terminations.
- Handle corrupted or malformed data gracefully.

### Protocol Compliance *(optional integration testing)*
**Files:**
```
src/test/java/server/handler/CommandHandlerTest.java  
src/test/java/server/handler/ClientHandlerTest.java  
src/test/java/server/handler/CommandHandlerTest.md  
src/test/java/server/handler/ClientHandlerTest.md  
```
- Simulates complete user workflows: register → login → send → read → logout.

- Validates that each command invokes correct service logic and returns expected responses.

- Ensures graceful handling of malformed JSON, invalid credentials, unknown commands, and unauthorized access.

- Confirms socket input is processed and flushed correctly in ClientHandler.

- Verifies compliance with custom TCP command protocol format (COMMAND%%{JSON}).

---

### Unit test notes

- All testing components directly support CA2 requirements for robust backend logic and protocol adherence.
- Temporary `.db` files are reset before each test to ensure repeatable results.
- The framework ensures stability under failure conditions such as I/O exceptions and dropped connections.

### Thread Safety

- All core services (`SessionManager`, `FileDatabase`, `LogHandler`) are implemented with thread safety in mind.
- Session state is stored in `ConcurrentHashMap`, ensuring safe concurrent access.
- Disk I/O operations are synchronized by internal locks (`userLock`, `emailLock`) in `FileDatabase`.
- Logging uses a dedicated single-threaded executor to prevent blocking and ensure flush-safe writes.

While the system was not stress-tested under simultaneous client threads, all per-thread logic was isolated and verified in tests. Race conditions were mitigated by design, and unit tests confirmed stable operation across simulated concurrent command flows.


# Code Planning: Email CLI Client

This section outlines the components and flow of the multithreaded CLI client. The client communicates with a thread-per-client server using a custom TCP/JSON protocol.

---

## 1. CLI Workflow

### Pre-Login Menu
Users can access the following commands before authentication:
- `register` — Create a new account.
- `login` — Authenticate an existing user.
- `exit` — Terminate the program.

### Post-Login Menu
After successful login, the user has access to full email functionality:
- `send` — Send an email.
- `inbox` — View received emails (metadata only).
- `sent` — View sent emails (metadata only).
- `search` — Search through inbox or sent by keyword.
- `read` — View full content of a specific email by ID.
- `logout` — End the session and return to the login screen.
- `exit` — Terminate the client connection and close the program.

---

## 2. Communication Architecture

### Socket Connection
- Establish a persistent TCP connection to the server on startup.

### Multithreaded Design
- **Main Thread** — Handles user input and sends protocol commands.
- **Listener Thread** (`ServerListener`) — Continuously listens for server responses and prints them.

## 3. Authentication Logic

### `handleRegister()`
- Collects email + password.
- Sends `REGISTER%%{...}` request.
- Awaits and displays server response.

### `handleLogin()`
- Collects email + password.
- Sends `LOGIN%%{...}` request.
- On success, stores session email in client state.

### Session Gatekeeping
- The main menu should only allow access to email commands **after login is successful**.

---

## 4. Email Command Handlers

Each handler builds a JSON payload, formats it with the correct command, and sends it:

- `handleSend()` — Prompts for to/from/subject/body and sends `SEND_EMAIL%%{...}`
- `handleInbox()` — Sends `RETRIEVE_EMAILS%%{...}` with `"type":"received"`
- `handleSent()` — Sends `RETRIEVE_EMAILS%%{...}` with `"type":"sent"`
- `handleSearch()` — Prompts for keyword and `"type"`, sends `SEARCH_EMAIL%%{...}`
- `handleRead()` — Prompts for email ID, sends `READ_EMAIL%%{...}`

---

## 5. Session Termination

- `handleLogout()` — Sends `LOGOUT%%{...}` and resets user state.
- `handleExit()` — Sends `EXIT`, closes socket, terminates program.

---







# Constants.java Overview

The `Constants.java` file defines all global constants used across the email system. It centralizes key configuration values for security, file paths, protocol commands, CLI formatting, and environment settings. This helps ensure consistency and maintainability across the project.

### Security and Hashing

| Constant        | Value      | Description                          |
|-----------------|------------|--------------------------------------|
| `HASH_ALGORITHM` | `"SHA-256"` | Algorithm used for password hashing. |
| `SALT_DELIMITER` | `"::"`     | Delimiter between hash and salt.     |
| `SALT_LENGTH`    | `16`        | Number of characters in salt.        |

---

### Server Configuration

| Constant        | Value | Description                                  |
|-----------------|-------|----------------------------------------------|
| `SERVER_PORT`   | `5050` | TCP port on which the server listens.        |
| `MAX_CLIENTS`   | `50`   | Max number of concurrent client connections. |

---

### File Paths

| Constant         | Path                                   | Description                            |
|------------------|----------------------------------------|----------------------------------------|
| `USERS_DB_PATH`  | `"src/main/resources/users.db"`        | File storing user credentials.         |
| `EMAILS_DB_PATH` | `"src/main/resources/emails.db"`       | File storing emails.                   |
| `SERVER_LOG_PATH`| `"src/logs/server.log"`                | Server log file path.                  |

---

### Environment Flags

| Constant       | Default | Description                                               |
|----------------|---------|-----------------------------------------------------------|
| `DEBUG_MODE`   | `true`  | Enables detailed logging output.                          |
| `IS_TEST_ENV`  | `true`  | Enables test-safe configurations and paths.               |
| `USE_TLS`      | `false` | Placeholder flag for future TLS/SSL support.              |

---

### Protocol Commands

| Command Constant | Description                          |
|------------------|--------------------------------------|
| `REGISTER`       | Register a new user.                 |
| `LOGIN`          | Log in an existing user.             |
| `LOGOUT`         | Log out from current session.        |
| `SEND`           | Send an email.                       |
| `LIST`           | List inbox or sent emails.           |
| `READ`           | Read a specific email.               |
| `DELETE`         | Delete an email.                     |
| `SEARCH`         | Search emails by keyword.            |
| `EXIT`           | Terminate the client session.        |

---

### CLI Syntax Templates

| Template                     | Description                           |
|------------------------------|---------------------------------------|
| `REGISTER <email> <pass>`    | Registers a new user.                 |
| `LOGIN <email> <pass>`       | Logs in with credentials.             |
| `SEND <to> <subject> <body>` | Sends a new email.                    |
| `SEARCH <keyword>`           | Searches for emails matching keyword. |
| `LOGOUT`                     | Logs out the current user.            |
| `EXIT`                       | Disconnects the client from server.   |


