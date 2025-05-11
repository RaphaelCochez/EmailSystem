package utils;

public class LogUtils {
    /**
     * Prints full stack trace only if DEBUG_MODE is enabled.
     */
    public static void printDebugStackTrace(Exception e) {
        if (ServerConstants.DEBUG_MODE) {
            e.printStackTrace();
        }
    }

    private LogUtils() {
        // Prevent instantiation
    }
}
