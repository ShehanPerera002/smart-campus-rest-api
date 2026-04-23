package com.smartcampus.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room within the smart campus system.
 * 
 * Each room can contain multiple sensors, which are stored
 * as a list of sensor IDs. This model is used to transfer
 * room-related data between the API and the client.
 */
public class Room {

    // Unique identifier for the room (e.g., "LIB-301")
    private String id;

    // Descriptive name of the room
    private String name;

    // Maximum number of people allowed in the room
    private int capacity;

    // List of sensor IDs associated with this room
    private List<String> sensorIds = new ArrayList<>();

    // Default constructor required for JSON deserialization
    public Room() {}

    // Constructor to create a room with basic details
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public List<String> getSensorIds() { return sensorIds; }
    public void setSensorIds(List<String> sensorIds) { this.sensorIds = sensorIds; }
}