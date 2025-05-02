# LogHandler Unit Testing

This document outlines the unit testing strategy for `LogHandler.java` located in `utils/`.

---

## Status: Implemented in LogHandlerTest.java

---

## Objective

Ensure that the `LogHandler` class:
- Correctly writes messages to the log file asynchronously
- Supports terminal output using `logAndPrint()`
- Properly flushes and shuts down the logging executor
- Respects a test-specific log path to avoid writing to production logs

---

## Tests Implemented

### 1. `testLogFileCreation`
- Verifies the log file exists after setup
- Ensures the test does not interfere with production logs

### 2. `testSimpleLogEntry`
- Logs a message using `log()`
- Waits for async execution to complete
- Confirms the message appears in the test log file

### 3. `testLogAndPrintEntry`
- Calls `logAndPrint()` with a message
- Asserts the message is written to the file

### 4. `testMultipleLogEntries`
- Logs five different messages
- Confirms all are written correctly after short wait

### 5. `testShutdownFlushesLogs`
- Logs a message and calls `shutdown()`
- Verifies the message was flushed to file before shutdown

---

## Setup Details

- Overrides `Constants.LOG_FILE_PATH` using reflection
- Uses `logs/test-server.log` to isolate test logs
- Calls `LogHandler.resetExecutorForTests()` in `@BeforeAll`

---

## Sample Assertions (JUnit 5)

```java
LogHandler.log("Hello test log!");
TimeUnit.MILLISECONDS.sleep(300);
List<String> lines = Files.readAllLines(Paths.get("logs/test-server.log"));

assertTrue(lines.stream().anyMatch(line -> line.contains("Hello test log!")));
```