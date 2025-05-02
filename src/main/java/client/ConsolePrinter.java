package client;

public class ConsolePrinter {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";

    public static void success(String message) {
        System.out.println(GREEN + message + RESET);
    }

    public static void error(String message) {
        System.err.println(RED + message + RESET);
    }

    public static void info(String message) {
        System.out.println(YELLOW + message + RESET);
    }

    public static void prompt(String message) {
        System.out.print(CYAN + "> " + message + RESET);
    }

    public static void raw(String message) {
        System.out.println(message);
    }

    public static void banner(String title) {
        String line = "=".repeat(title.length() + 4);
        System.out.println(MAGENTA + line);
        System.out.println("| " + title + " |");
        System.out.println(line + RESET);
    }

    public static void infoWithTimestamp(String message) {
        String ts = java.time.LocalTime.now().withNano(0).toString();
        System.out.println(YELLOW + "[" + ts + "] " + message + RESET);
    }

    public static void progressDots(String message, int dots, int delayMs) {
        System.out.print(GREEN + message);
        for (int i = 0; i < dots; i++) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {
            }
            System.out.print(".");
        }
        System.out.println(RESET);
    }

    public static void divider() {
        System.out.println(BLUE + "----------------------------------------" + RESET);
    }

    public static void headline(String message) {
        System.out.println(MAGENTA + "\n== " + message.toUpperCase() + " ==\n" + RESET);
    }
}
