# File-Based Database Design
This system uses a simple file-based approach for persistent storage. Two files are used, each located in a ```database/``` directory in the root of the project:

```pgsql 
/database
├── users.db     ← Stores user credentials  
└── emails.db    ← Stores all sent and received emails 
```

## ```users.db``` — User Credentials
### Purpose:
To store registered user credentials (email + hashed password) securely and persistently across sessions.
### Format:
Each line in the file represents one user in JSON format:
```json 
{"email": "user@example.com", "hashedPassword": "5e8848..."}
```
### Why?
* Load users when the server starts or during login checks
* Save new users during registration
* Gracefully persist users in case of crash or shutdown

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
* Load a user's inbox/sent messages during login or retrieval commands
* Append emails when sent
* Save data immediately to prevent loss on unexpected exit

# When & How These Files Are Used


| Event                     | Action Taken                                                                 |
|---------------------------|------------------------------------------------------------------------------|
| Server startup            | Load `users.db` and `emails.db` into memory if needed                        |
| User login                | Search `users.db` for matching email + hashed password                        |
| User registration         | Append new user to `users.db`                                                |
| Email sent                | Append new email object to `emails.db`                                       |
| List/search/read emails   | Filter `emails.db` for matching `to`/`from` fields                           |
| Server shutdown/logout    | Ensure memory state is flushed to files, if anything changed                 |
| Unexpected disconnect     | Files are used for graceful recovery — already-written data is preserved     |
