package com.smartcampus.models;

/**
 * Represents a single reading recorded by a sensor.
 * 
 * Each reading stores the value captured by the sensor along with
 * a timestamp indicating when the measurement was taken.
 * This allows the system to maintain a history of sensor data.
 */
public class SensorReading {

    // Unique identifier for the reading event
    private String id;

    // Time at which the reading was recorded 
    private long timestamp;

    // Value captured by the sensor
    private double value;

    // Default constructor required for JSON deserialization
    public SensorReading() {}

    // Constructor to create a new reading with all fields
    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}