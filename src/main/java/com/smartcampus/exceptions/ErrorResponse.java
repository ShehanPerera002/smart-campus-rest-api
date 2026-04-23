package com.smartcampus.exceptions;

/**
 * Represents a structured error response returned by the API.
 * 
 * This class ensures that all errors follow a consistent JSON format,
 * making it easier for clients to understand what went wrong.
 * 
 * It also helps prevent sensitive internal details, such as stack traces,
 * from being exposed to the user.
 */
public class ErrorResponse {

    // Indicates that the response is an error
    private final String status  = "error";

    // readable error message
    private final String message;

    // Timestamp showing when the error occurred
    private final long   timestamp;

    /**
     * Creates a new error response with the given message.
     */
    public ErrorResponse(String message) {
        this.message   = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStatus()    { return status; }
    public String getMessage()   { return message; }
    public long   getTimestamp() { return timestamp; }
}