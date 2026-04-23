package com.smartcampus.exceptions;

/** HTTP 409 – room still has sensors; cannot be deleted. */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) { super(message); }
}
