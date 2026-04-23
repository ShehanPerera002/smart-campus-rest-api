package com.smartcampus.exceptions;

/** HTTP 403 – sensor is in MAINTENANCE/OFFLINE state and cannot accept readings. */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) { super(message); }
}
