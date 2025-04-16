# ReadMe.md

## Docs formatter tool
https://www.rich-text-to-markdown.com/

## Project Structure



```pgsql
/EmailSystem
├── /src
│   ├── /client
│   │   ├── EmailClientCLI.java          ← CLI client main logic (handles UI, threads)
│   │   ├── ServerListener.java          ← Listens for server messages
│   │   └── CommandFormatter.java        ← Handles user command formatting
│   │
│   ├── /server
│   │   ├── EmailServer.java             ← Main server class (entry point)
│   │   ├── /handler
│   │   │   ├── ClientHandler.java       ← Thread-per-client handler
│   │   │   └── CommandHandler.java      ← Handles incoming commands from client
│   │   ├── /protocol
│   │   │   └── ProtocolConstants.java   ← Command keywords and parsing logic
│   │   ├── /service
│   │   │   ├── AuthService.java         ← Handles login, registration, logout
│   │   │   ├── EmailService.java        ← Send, read, inbox/sent logic
│   │   │   └── SessionManager.java      ← Tracks active user sessions
│   │   ├── /data
│   │   │   └── FileDatabase.java        ← Read/write operations for users/emails
│
│   ├── /model
│   │   ├── User.java                    ← Represents a user object
│   │   └── Email.java                   ← Represents an email object
│
│   ├── /utils
│   │   ├── Constants.java               ← App-wide flags and config values
│   │   ├── JsonUtils.java               ← JSON serialization/deserialization
│   │   └── SecurityUtils.java           ← Password hashing (SHA-256)
│
├── /resources
│   ├── users.db                         ← Flat file storing user credentials
│   └── emails.db                        ← Flat file storing email records
│
├── /test
│   ├── /java
│   │   ├── /server/data
│   │   │   ├── FileDatabaseEmailTest.java ← Unit test for saving/loading emails
│   │   │   └── FileDatabaseUserTest.java  ← Unit test for user persistence logic
│   │   └── /...                         ← Additional tests go here
│   └── /resources
│       ├── test_users.db               ← Temp user DB for tests
│       └── test_emails.db              ← Temp email DB for tests
│
├── /logs
│   └── server.log                       ← Server log output (if enabled)
│
└── README.md

```

* im searching for good unit test methods in java later, for now i just added a structure that might be good

## Key Principles

| **Folder**   | **Purpose**                                                              |
|--------------|---------------------------------------------------------------------------|
| `client/`    | CLI-related logic (user I/O, command formatting)                          |
| `server/`    | Server-side socket, threads, services                                     |
| `model/`     | Data classes for `User`, `Email`, etc.                                    |
| `utils/`     | Reusable tools (e.g., encryption, JSON, constants)                        |
| `database/`  | Flat-file DB — stores users and emails                                    |
| `logs/`      | Optional but good for debugging (`@Slf4j` output)                         |


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


##  Next Steps
- [ ] Implement `CommandFormatter.java` to reduce duplication in request building.
- [ ] Build each CLI handler method.
- [ ] Test login → send → inbox → logout → exit flow.

