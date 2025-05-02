package model;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import utils.Constants;

public class Email {
    private String id;
    private String to;
    private String from;
    private String subject;
    private String body;
    private String timestamp;
    private boolean visible;
    private boolean edited;

    public Email() {
    } // Required for Gson

    public Email(String id, String to, String from, String subject, String body, String timestamp, boolean visible,
            boolean edited) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.body = body;
        this.setTimestamp(timestamp); // validate format
        this.visible = visible;
        this.edited = edited;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIMESTAMP_FORMAT);
            formatter.parse(timestamp);
            this.timestamp = timestamp;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format. Expected: " + Constants.TIMESTAMP_FORMAT);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
