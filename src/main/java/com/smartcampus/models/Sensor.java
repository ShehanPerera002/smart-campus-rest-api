package com.smartcampus.models;

/**
 * Represents a sensor assigned to a room in the smart campus system.
 * 
 * Each sensor records environmental or usage data (such as temperature
 * or occupancy) and is linked to a specific room using the roomId.
 */
public class Sensor {

    // Unique identifier for the sensor 
    private String id;

    // Type of sensor 
    private String type;

    // Current operational status of the sensor
    private String status;

    // Most recent value recorded by the sensor
    private double currentValue;

    // ID of the room where this sensor is installed
    private String roomId;

    // Default constructor required for JSON deserialization
    public Sensor() {}

    // Constructor to create a sensor with initial values
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    /**
     * Checks whether the sensor is available to accept new readings.
     * Only sensors with ACTIVE status are allowed to receive updates.
     */
    public boolean isAvailable() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}