package client.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Email;
import utils.ConsoleConstants;
import utils.ConsolePrinter;
import utils.ProtocolConstants;

import java.util.List;

/**
 * Handles server protocol responses and updates session state accordingly.
 */
public class ServerResponseHandler {

    private final SessionState session;
    private final List<Email> receivedEmails;
    private final Gson gson = new Gson();

    public ServerResponseHandler(SessionState session, List<Email> receivedEmails) {
        this.session = session;
        this.receivedEmails = receivedEmails;
    }

    public void handle(String response) {
        // ConsolePrinter.infoWithTimestamp(ConsoleConstants.SERVER_PREFIX + response);

        String[] parts = response.split(ProtocolConstants.DELIMITER, 2);
        String command = parts[0];

        switch (command) {
            case ProtocolConstants.RESPONSE_LOGIN_SUCCESS -> {
                session.setLoggedIn(true);
                ConsolePrinter.success(ConsoleConstants.LOGIN_WELCOME_PREFIX + session.getEmail());
                ConsolePrinter.divider();
                ConsolePrinter.raw(ConsoleConstants.POST_LOGIN_COMMANDS);
            }

            case ProtocolConstants.RESPONSE_LOGIN_FAIL -> {
                session.setLoginFailReason(parts.length > 1 ? parts[1] : "Unknown error");
                ConsolePrinter.error(ConsoleConstants.LOGIN_FAILURE_MSG + session.getLoginFailReason());
            }

            case ProtocolConstants.RESPONSE_REGISTER_SUCCESS ->
                ConsolePrinter.success(ConsoleConstants.REGISTER_SUCCESS_MSG);

            case ProtocolConstants.RESPONSE_REGISTER_FAIL -> {
                ConsolePrinter.error(ConsoleConstants.REGISTER_FAILURE_MSG);
                session.setLoggedIn(false);
            }

            case ProtocolConstants.RESPONSE_SEND_EMAIL_SUCCESS ->
                ConsolePrinter.success(ConsoleConstants.EMAIL_SEND_SUCCESS_MSG);

            case ProtocolConstants.RESPONSE_SEND_EMAIL_FAIL ->
                ConsolePrinter.error(ConsoleConstants.EMAIL_SEND_FAIL_MSG);

            case ProtocolConstants.RESPONSE_LOGOUT_SUCCESS -> {
                ConsolePrinter.info(ConsoleConstants.LOGOUT_SUCCESS_MSG);
                session.reset();
            }

            case ProtocolConstants.RESPONSE_EXIT_SUCCESS -> {
                ConsolePrinter.info(ConsoleConstants.EXIT_SUCCESS_MSG);
                session.reset();
            }

            case ProtocolConstants.RESPONSE_RETRIEVE_EMAILS_SUCCESS -> {
                if (parts.length < 2) {
                    ConsolePrinter.error(ConsoleConstants.EMAIL_RETRIEVE_FAIL_MSG);
                    return;
                }

                List<Email> emails = gson.fromJson(parts[1], new TypeToken<List<Email>>() {
                }.getType());
                receivedEmails.clear();
                receivedEmails.addAll(emails);
                displayEmailList();
            }

            case ProtocolConstants.RESPONSE_SEARCH_EMAIL_SUCCESS -> {
                if (parts.length < 2) {
                    ConsolePrinter.error("Search failed or returned no results.");
                    return;
                }

                List<Email> searchResults = gson.fromJson(parts[1], new TypeToken<List<Email>>() {
                }.getType());
                if (searchResults.isEmpty()) {
                    ConsolePrinter.info("No matching emails found.");
                } else {
                    ConsolePrinter.info("Search results:");
                    for (int i = 0; i < searchResults.size(); i++) {
                        Email email = searchResults.get(i);
                        ConsolePrinter
                                .info((i + 1) + ". Subject: " + email.getSubject() + ", From: " + email.getFrom());
                        ConsolePrinter.info("   Timestamp: " + email.getTimestamp());
                    }
                }
            }

            default -> ConsolePrinter.info(ConsoleConstants.UNHANDLED_RESPONSE_MSG + response);
        }
    }

    private void displayEmailList() {
        if (receivedEmails.isEmpty()) {
            ConsolePrinter.info(ConsoleConstants.EMAIL_LIST_EMPTY_MSG);
        } else {
            ConsolePrinter.info(ConsoleConstants.EMAIL_LIST_HEADER_MSG);
            for (int i = 0; i < receivedEmails.size(); i++) {
                Email email = receivedEmails.get(i);
                ConsolePrinter.info((i + 1) + ". Subject: " + email.getSubject() + ", From: " + email.getFrom());
                ConsolePrinter.info("   Timestamp: " + email.getTimestamp());
            }
        }
    }
}
