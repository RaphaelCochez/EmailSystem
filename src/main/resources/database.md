# File-Based Database Design
This system uses a simple file-based approach for persistent storage. Two files are used, each located in a ```database/``` directory in the root of the project:

```pgsql 
/resources
├── users.db     ← Stores user credentials  
└── emails.db    ← Stores all sent and received emails 
```
---
## ```users.db``` — User Credentials
### Purpose:
To store registered user credentials (email + hashed password) securely and persistently across sessions.
### Format:
Each line in the file represents one user in JSON format:
```json 
{"email": "user@example.com", "Password": "5e8848..."}
```
### Why?
* Load users when the server starts into memory
* Gracefully persist users in case of crash or shutdown

---

## ```emails.db``` — Emails
### Purpose:
To persistently store all email messages sent within the system, including sender, recipient, content, and metadata.

### Format:
Each line is a complete email object in JSON format:
``` json
{
  "id": "UUID-1234",
  "to": "recipient@example.com",
  "from": "sender@example.com",
  "subject": "Hello",
  "body": "Message content here",
  "timestamp": "2025-04-10T14:00:00Z",
  "visible": true,
  "edited": false
}
``` 
### why
* Load a user's inbox/sent messages during startup into memory
* Save data immediately to prevent loss on exit or unexpected failure

---

# When & How These Files Are Used


| Event                     | Action Taken                                                                 |
|---------------------------|------------------------------------------------------------------------------|
| Server startup            | Load `users.db` and `emails.db` into memory on server startup                |
| Server shutdown    | Ensure memory state is flushed to files on server exit                       |
| Unexpected disconnect     | Files are used for graceful recovery — already-written data is preserved     |
