package client;

import utils.Constants;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Entry point for the email client CLI.
 * Manages socket connection and user interface flow.
 */
public class EmailClientCLI {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenerThread;

    public static void main(String[] args) {
        new EmailClientCLI().start();
    }

    public void start() {
        try {
            socket = new Socket("localhost", Constants.SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            startListener();

            ConsolePrinter.banner("Connected to Email Server");
            ConsolePrinter.info("Listening on port " + Constants.SERVER_PORT);
            showMenu();

        } catch (IOException e) {
            ConsolePrinter.error("Failed to connect to server: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    ConsolePrinter.infoWithTimestamp("[SERVER]: " + response);
                }
            } catch (IOException e) {
                ConsolePrinter.error("Disconnected from server.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void showMenu() {
        try (Scanner scanner = new Scanner(System.in)) {
            ConsolePrinter.divider();
            ConsolePrinter.headline("Type 'help' for available commands");

            while (true) {
                ConsolePrinter.prompt("Command");
                String line = scanner.nextLine().trim();

                if (line.equalsIgnoreCase("exit")) {
                    out.println("EXIT%%{}");
                    ConsolePrinter.progressDots("Exiting", 3, 300);
                    break;
                } else if (line.equalsIgnoreCase("help")) {
                    ConsolePrinter.raw(Constants.CLIENT_HELP_TEXT);
                } else {
                    out.println(line);
                }
            }
        }
    }

    private void cleanup() {
        try {
            if (listenerThread != null)
                listenerThread.interrupt();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            ConsolePrinter.error("Error during cleanup: " + e.getMessage());
        }
    }
}
