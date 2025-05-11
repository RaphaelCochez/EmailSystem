# Unit Testing Overview – Email System

This document provides a concise overview of the unit testing strategy for the multi-client TCP-based email server system. It outlines the testing scope, tools, tested components, and methodology.

## Project Context

This project implements a multi-threaded email server with client connections over TCP sockets. Server functionality includes user registration, authentication, session management, email storage, retrieval, and search operations. Each client is handled in a separate thread, maintaining session state for the duration of the connection.

---

## Testing Goals

The goal of unit testing is to ensure functional correctness, robustness, and fault tolerance of the core server-side logic and utilities:

- Validate input handling and protocol parsing
- Ensure secure authentication flow
- Test session state integrity
- Verify email sending, retrieval, and filtering logic
- Detect protocol or formatting violations
- Confirm correct server response generation for all protocol commands

---

## Frameworks & Tools

| Tool           | Purpose                             |
|----------------|-------------------------------------|
| JUnit 5        | Core unit testing framework         |
| SLF4J (with Lombok) | Structured logging in tests and source |
| Gson           | JSON parsing and serialization      |
| Maven Surefire | Automated test runner and reporting |

---

## Tested Components
> Every unit test in the project is accompanied by **in-depth documentation**, describing the rationale, structure, and edge cases covered in that test class. See the corresponding `.md` files in the `src/test/java/...` directories for more information.


### 1. **Authentication**
- `AuthServiceTest.java`
- Verifies: Registration, Login (success/failure), Logout
- Tests hash validation, duplicate prevention, and malformed input handling

### 2. **Session Management**
- `SessionManagerTest.java`
- Verifies: Session start, end, and state checking under isolated conditions

### 3. **Email Handling**
- `EmailServiceTest.java`
- Verifies: Email send, search, and read operations with ownership and visibility constraints

### 4. **Command Protocol Handling**
- `CommandHandlerTest.java`
- Full simulation of JSON-based commands:
  - REGISTER, LOGIN, SEND_EMAIL, RETRIEVE_EMAILS, SEARCH_EMAIL, READ_EMAIL, LOGOUT, EXIT
- Includes edge cases: invalid sessions, malformed input, unauthorized access

### 5. **Client Handler Thread Simulation**
- `ClientHandlerTest.java`
- Simulates a threaded client session
- Verifies lifecycle from connection to termination with valid and invalid streams

### 6. **Data Storage**
- `FileDatabaseEmailTest.java` and `FileDatabaseUserTest.java`
- Verifies persistent user/email storage logic using test-specific flat files
- Ensures no malformed entries are processed

### 7. **Utilities**
- `LogHandlerTest.java` and `SecurityUtilsTest.java`
- Test logging output behavior and password hashing/verification

--- 

### Why `EmailServer.java` Is Not Unit Tested

`EmailServer.java` is the main entry point of the server application, responsible for accepting incoming client connections via sockets. It is intentionally excluded from unit testing for the following reasons:

- **No Business Logic**  
  `EmailServer.java` primarily delegates tasks to other components such as `ClientHandler`, `CommandHandler`, and various service classes. All core logic is tested at those levels.

- **I/O and Networking Boundaries**  
  The class deals directly with blocking I/O and real socket connections (`ServerSocket`), which are unsuitable for isolated unit testing. Such operations are better verified through integration testing.

- **Threading Complexity**  
  It launches new threads for each client using a thread-per-client model. Verifying thread lifecycle and inter-thread communication is beyond the scope of unit testing and requires system-level testing or mocking at a much broader scale.

Instead, the focus has been placed on unit testing the classes that `EmailServer.java` depends on. This approach ensures logic correctness while keeping the tests fast, isolated, and maintainable.

For full server validation, **integration tests** should be used to simulate client connections and verify end-to-end communication.


---

## Testing Approach

- **Black-box + White-box** methodology  
  Focuses on external behavior with internal logic validation using in-memory test databases.
  
- **Dependency Isolation**  
  `FakeDatabase` and `FakeSessionManager` mock dependencies for all service-layer tests.

- **Stateless Validation**  
  Tests are fully independent and stateless, ensuring predictable outcomes on repeat runs.

- **Assertions**  
  All critical response patterns and outputs are asserted with detailed `PrintWriter` and buffer inspection.

- **Coverage**  
  > 95% logic coverage for server-side functionality, protocol compliance, and error scenarios.

---

## Test Execution

To run all unit tests:

```bash
mvn test
```
Results will be displayed in the terminal and detailed reports will be generated under:
``` bash 
/target/surefire-reports/
``` 