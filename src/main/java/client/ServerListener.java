package client;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Listens for server messages and prints them to the console.
 * Runs in its own thread.
 */
public class ServerListener implements Runnable {

    private final BufferedReader in;
    private volatile boolean running = true;

    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                System.out.println("[SERVER] " + line);
                if (line.startsWith("EXIT_SUCCESS")) {
                    System.out.println("[CLIENT] Server requested termination.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[ERROR] Lost connection to server: " + e.getMessage());
        } finally {
            System.out.println("[CLIENT] Listener shutting down.");
        }
    }

    public void stop() {
        running = false;
    }
}
