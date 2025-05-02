---

## `src/test/java/client/ConsolePrinterTest.md`

# ConsolePrinter Utility Testing

This document outlines the unit testing strategy for the `ConsolePrinter` class in `client/ConsolePrinter.java`.

---

## Status: Implemented in ConsolePrinterTest.java

---

## Objective

Ensure that the `ConsolePrinter`:
- Outputs messages to the console with correct color formatting
- Displays structured banners and prompts clearly
- Supports visual effects such as timestamped output and progress dots
- Can be tested independently by redirecting and capturing output streams

---

## Tests Implemented

### 1. `testSuccess`
- Call `ConsolePrinter.success("Success message")`
- Assert that message appears in captured stdout

### 2. `testError`
- Call `ConsolePrinter.error("Error message")`
- Assert that message appears in captured stdout

### 3. `testInfo`
- Call `ConsolePrinter.info("Info message")`
- Assert presence of message in output

### 4. `testPrompt`
- Call `ConsolePrinter.prompt("Prompt message")`
- Check output includes prompt

### 5. `testRaw`
- Call `ConsolePrinter.raw("Raw output")`
- Validate plain message is outputted

### 6. `testBanner`
- Call `ConsolePrinter.banner("Title")`
- Assert banner header structure appears

### 7. `testInfoWithTimestamp`
- Call `ConsolePrinter.infoWithTimestamp("Timed info")`
- Assert output includes message and timestamp

### 8. `testProgressDots`
- Call `ConsolePrinter.progressDots("Loading", 3, 10)`
- Validate that final output contains `Loading...`

---

## Sample Assertions (JUnit)

```java
ConsolePrinter.success("Hello!");
assertTrue(cleanOutput().contains("Hello!"));

ConsolePrinter.banner("Banner");
assertTrue(cleanOutput().contains("Banner"));
