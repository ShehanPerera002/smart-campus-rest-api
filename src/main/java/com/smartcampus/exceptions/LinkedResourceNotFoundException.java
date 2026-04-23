package com.smartcampus.exceptions;

/** HTTP 422 – request body references a resource (roomId) that does not exist. */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) { super(message); }
}
