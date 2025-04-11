## Project Structure

```pgsql
/EmailSystem
├── /src
│   ├── /client
│   │   ├── EmailClientCLI.java          ← Your CLI client main logic (handles UI, threads)
│   │   ├── ServerListener.java          ← Listens for server messages
│   │   └── CommandHandler.java          ← Optional: handles user command formatting
│   │
│   ├── /server
│   │   ├── EmailServer.java             ← Main server class (entry point)
│   │   ├── ClientHandler.java           ← Thread-per-client logic
│   │   ├── AuthService.java             ← Handles login, register
│   │   ├── EmailService.java            ← Handles send, retrieve, search
│   │   ├── FileDatabase.java            ← Reads/writes users.db and emails.db
│   │   └── SessionManager.java          ← Tracks who is logged in
│   │
│   ├── /model
│   │   ├── User.java                    ← Represents a user object
│   │   └── Email.java                   ← Represents an email object
│   │
│   ├── /utils
│   │   ├── JsonUtils.java               ← Helper for JSON parsing/formatting
│   │   ├── SecurityUtils.java           ← SHA-256 password hashing
│   │   └── Constants.java               ← Any shared strings or config
│
├── /resources
│   ├── users.db                         ← Flat file storing user credentials
│   └── emails.db                        ← Flat file storing all email records
│
├── /logs                                ← (optional) for debugging
│   └── server.log
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

##  Next Steps
- [ ] Implement `CommandFormatter.java` to reduce duplication in request building.
- [ ] Build each CLI handler method.
- [ ] Test login → send → inbox → logout → exit flow.

